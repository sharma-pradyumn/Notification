public class SMS_Channel implements Channel{
    public NotificationResult sendNotification(NotificationAttempt notificationAttempt) {
        return new NotificationResult(notificationAttempt.notification.notificationId(), notificationAttempt.getNotificationAttemptId(), RESULT_ENUM.SUCCESS);
    }
}
