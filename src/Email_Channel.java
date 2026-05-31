public class Email_Channel implements Channel{
        public NotificationResult sendNotification(NotificationAttempt notificationAttempt) {
            return new NotificationResult(notificationAttempt.notification.notificationId(), notificationAttempt.getNotificationAttemptId(), RESULT_ENUM.FAILED_PERMANENT);
        }
}
