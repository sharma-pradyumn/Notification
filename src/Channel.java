
public interface Channel {
    // should it be void and accept a callback method for success/fail ?
    public NotificationResult sendNotification(NotificationAttempt notificationAttempt);
}
