package tech.ztimes.powernotes.message;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.Topic;
import tech.ztimes.powernotes.entity.FlowNoteRelation;

public interface FlowNoteRelationListener {
    Topic<FlowNoteRelationListener> TOPIC = Topic.create("PowerNotes-FlowNoteRelation", FlowNoteRelationListener.class);

    default void save(FlowNoteRelation relation) {}
    default void remove(FlowNoteRelation relation) {}

    static FlowNoteRelationListener publisher() {
        return ApplicationManager.getApplication().getMessageBus().syncPublisher(TOPIC);
    }
}
