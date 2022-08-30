package io.thundra.merloc.intellij.util;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

/**
 * @author serkan
 */
public final class NotificationUtils {

    private static final String NOTIFICATIONS_GROUP_NAME = "merlocNotifications";
    private static final String STICKY_NOTIFICATIONS_GROUP_NAME = "merlocStickyNotifications";

    private NotificationUtils() {
    }

    public static void showInfoMessage(String message, Project project) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATIONS_GROUP_NAME)
                .createNotification(message, NotificationType.INFORMATION)
                .notify(project);
    }

    public static void showStickyInfoMessage(String message, Project project) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup(STICKY_NOTIFICATIONS_GROUP_NAME)
                .createNotification(message, NotificationType.INFORMATION)
                .notify(project);
    }

    public static void showWarningMessage(String message, Project project) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATIONS_GROUP_NAME)
                .createNotification(message, NotificationType.WARNING)
                .notify(project);
    }

    public static void showStickyWarningMessage(String message, Project project) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup(STICKY_NOTIFICATIONS_GROUP_NAME)
                .createNotification(message, NotificationType.WARNING)
                .notify(project);
    }

    public static void showErrorMessage(String message, Project project) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup(NOTIFICATIONS_GROUP_NAME)
                .createNotification(message, NotificationType.ERROR)
                .notify(project);
    }

    public static void showStickyErrorMessage(String message, Project project) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup(STICKY_NOTIFICATIONS_GROUP_NAME)
                .createNotification(message, NotificationType.ERROR)
                .notify(project);
    }

}
