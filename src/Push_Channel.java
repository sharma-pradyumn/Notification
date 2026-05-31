public class Push_Channel implements Channel{
        public NotificationResult sendNotification(NotificationAttempt notificationAttempt) {
            return new NotificationResult(notificationAttempt.notification.notificationId(), notificationAttempt.getNotificationAttemptId(), Math.random() > 0.5 ? RESULT_ENUM.SUCCESS: RESULT_ENUM.FAILED_TEMPORARY);
        }
}
