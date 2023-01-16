package tech.ztimes.powernotes.message;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.Topic;
import tech.ztimes.powernotes.entity.Note;

public interface NoteListener {
    Topic<NoteListener> TOPIC = Topic.create("PowerNotes", NoteListener.class);

    default void save(Note note) {}
    default void remove(Note note) {}

    static NoteListener publisher() {
        return ApplicationManager.getApplication().getMessageBus().syncPublisher(NoteListener.TOPIC);
    }
}
