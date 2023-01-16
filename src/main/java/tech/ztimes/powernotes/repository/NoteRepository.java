package tech.ztimes.powernotes.repository;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.intellij.openapi.application.ApplicationManager;
import tech.ztimes.powernotes.entity.Note;
import tech.ztimes.powernotes.message.NoteListener;

import java.util.List;
import java.util.stream.Collectors;

import static com.googlecode.cqengine.query.QueryFactory.attribute;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.and;

public class NoteRepository {
    protected final static IndexedCollection<Note> repo = new ConcurrentIndexedCollection<>();
    public final static Attribute<Note, Long> ID = attribute(Note.class, Long.class, "id", Note::getId);
    public final static Attribute<Note, String> PROJECT_NAME = attribute(Note.class, String.class, "projectName", Note::getProjectName);
    public final static Attribute<Note, String> FILE_NAME = attribute(Note.class, String.class, "fileName", Note::getFileName);
    public final static Attribute<Note, String> FILE_PATH = attribute(Note.class, String.class, "filePath", Note::getFilePath);
    public final static Attribute<Note, Integer> LINE_NUMBER = attribute(Note.class, Integer.class, "lineNumber", Note::getLineNumber);
    public final static Attribute<Note, Integer> END_LINE_NUMBER = attribute(Note.class, Integer.class, "endLineNumber", Note::getEndLineNumber);

    public NoteRepository() {
        var app = ApplicationManager.getApplication();
        app.getMessageBus().connect().subscribe(NoteListener.TOPIC, new NoteListener() {
            @Override
            public void save(Note note) {
                NoteRepository.this.save(note);
            }

            @Override
            public void remove(Note note) {
                NoteRepository.this.remove(note);
            }
        });
    }

    public static NoteRepository getInstance() {
        return ApplicationManager.getApplication().getService(NoteRepository.class);
    }

    public List<Note> list(String projectName, String filePath) {
        var query = and(
                equal(PROJECT_NAME, projectName),
                equal(FILE_PATH, filePath)
        );

        try (var resultSet = repo.retrieve(query)) {
            return resultSet.stream()
                    .collect(Collectors.toList());
        }
    }

    public Note get(String projectName, String filePath, int lineNumber, int endLineNumber) {
        var query = and(
                equal(PROJECT_NAME, projectName),
                equal(FILE_PATH, filePath),
                equal(LINE_NUMBER, lineNumber),
                equal(END_LINE_NUMBER, endLineNumber)
        );

        try (var resultSet = repo.retrieve(query)) {
            return resultSet.stream().findFirst().orElse(null);
        }
    }

    public Note get(Long id) {
        var query = equal(ID, id);

        try (var resultSet = repo.retrieve(query)) {
            return resultSet.stream().findFirst().orElse(null);
        }
    }

    public void save(Note note) {
        remove(note);
        repo.add(note);
    }

    public void remove(Note note) {
        var query = equal(ID, note.getId());

        try (var resultSet = repo.retrieve(query)) {
            resultSet.stream().forEach(repo::remove);
        }
    }
}
