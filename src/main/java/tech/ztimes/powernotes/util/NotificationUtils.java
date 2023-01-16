package tech.ztimes.powernotes.util;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.ProjectManager;

public class NotificationUtils {
    public static void fire(NotificationType type, String text) {
        var projects = ProjectManager.getInstance().getOpenProjects();
        var notification = NotificationGroupManager.getInstance().getNotificationGroup("PowerNotes")
                .createNotification(text, type);
        for (var project : projects) {
            notification.notify(project);
        }
    }
}
