package tech.ztimes.powernotes.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;

public class MessageUtils {
    public static <T> T applicationPublisher(Topic<T> topic) {
        return ApplicationManager.getApplication().getMessageBus().syncPublisher(topic);
    }

    public static <T> T projectPublisher(Project project, Topic<T> topic) {
        return project.getMessageBus().syncPublisher(topic);
    }
}
