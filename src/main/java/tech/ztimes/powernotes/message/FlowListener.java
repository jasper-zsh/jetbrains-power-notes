package tech.ztimes.powernotes.message;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import tech.ztimes.powernotes.entity.Flow;
import tech.ztimes.powernotes.repository.FlowRepository;

public interface FlowListener {
    Topic<FlowListener> TOPIC = Topic.create("PowerNotes-Flow", FlowListener.class, Topic.BroadcastDirection.TO_PARENT);

    default void save(Flow flow) {}
    default void remove(Flow flow) {}
    default void listUpdated() {}
    default void clear() {}
    default void activatedChanged() {}

    static FlowListener publisher(Project project) {
        return project.getMessageBus().syncPublisher(TOPIC);
    }
}
