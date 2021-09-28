package cn.huoxian.dongtai.plugin.notify;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;


public class DongTaiNotifier {

    /**
     * 危险通知
     */
    public static void notificationError(@Nullable Project project, String content) {
        NotificationGroupManager.getInstance().getNotificationGroup("DongTai Notification")
                .createNotification(content, NotificationType.ERROR).setTitle("DongTai Error").notify(project);
    }

    /**
     * 警告通知
     */
    public static void notificationWarning(@Nullable Project project, String content) {
        NotificationGroupManager.getInstance().getNotificationGroup("DongTai Notification")
                .createNotification(content, NotificationType.WARNING).setTitle("DongTai Warning").notify(project);
    }

    /**
     * 普通通知
     */
    public static void notificationInfo(@Nullable Project project, String content) {
        NotificationGroupManager.getInstance().getNotificationGroup("DongTai Notification")
                .createNotification(content, NotificationType.INFORMATION).setTitle("DongTai Info").notify(project);
    }
}
