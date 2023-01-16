package tech.ztimes.powernotes.message;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFileManager;
import lombok.extern.slf4j.Slf4j;
import tech.ztimes.powernotes.entity.Note;
import tech.ztimes.powernotes.util.EditorUtils;
import tech.ztimes.powernotes.util.ProjectUtils;

import java.nio.file.Paths;

@Slf4j
public class EditorNoteListener implements NoteListener {

    public void save(Note note) {
        var project = ProjectUtils.getProject(note.getProjectName());
        if (project == null) {
            return;
        }
        var file = VirtualFileManager.getInstance().findFileByNioPath(Paths.get(project.getBasePath(), note.getFilePath()));
        if (file == null) return;
        var fileEditors = FileEditorManager.getInstance(project).getAllEditors(file);
        for (var fileEditor : fileEditors) {
            var editor = EditorUtils.getEditor(fileEditor);
            if (editor != null) {
                EditorUtils.addNote(editor, note);
            }
        }
    }

    public void remove(Note note) {
        var project = ProjectUtils.getProject(note.getProjectName());
        if (project == null) {
            return;
        }
        var file = VirtualFileManager.getInstance().findFileByNioPath(Paths.get(project.getBasePath(), note.getFilePath()));
        if (file == null) {
            return;
        }
        var fileEditors = FileEditorManager.getInstance(project).getAllEditors(file);
        for (var fileEditor : fileEditors) {
            var editor = EditorUtils.getEditor(fileEditor);
            if (editor != null) {
                EditorUtils.clearTextAfterLine(editor, note.getLineNumber(), note.getEndLineNumber());
            }
        }
    }
}
