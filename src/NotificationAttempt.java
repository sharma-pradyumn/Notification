import java.time.Instant;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class NotificationAttempt implements Delayed {

    Notification notification;
    CHANNEL_ENUM channelEnum;
    Instant attemptTime;
    int attemptCount;
    ATTEMPT_STATUS_ENUM attemptStatus;
    String notificationAttemptId; // set as "notificationId" + "-" + attemptCount
    Channel channel;
    public NotificationAttempt(Notification notification, CHANNEL_ENUM channelEnum,
                               Instant attemptTime, int attemptCount, Channel channel,
                               ATTEMPT_STATUS_ENUM attemptStatus) {
        this.notification = notification;
        this.attemptTime = attemptTime;
        this.channelEnum = channelEnum;
        this.attemptCount = attemptCount;
        this.attemptStatus = attemptStatus;
        this.notificationAttemptId = notification.notificationId() + "-" +attemptCount;
        this.channel = channel;
    }

    @Override
    public int compareTo(Delayed o) {
        NotificationAttempt other = (NotificationAttempt) o;
        return this.attemptTime.compareTo(other.attemptTime);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(Instant.now().until(attemptTime));
    }

    public Notification getNotification() {
        return notification;
    }

    public String getNotificationAttemptId() {
        return notificationAttemptId;
    }

    public ATTEMPT_STATUS_ENUM getAttemptStatus() {
        return attemptStatus;
    }

    public void setAttemptStatus(ATTEMPT_STATUS_ENUM attemptStatus) {
        this.attemptStatus = attemptStatus;
    }

    public CHANNEL_ENUM getChannelEnum() {
        return channelEnum;
    }

    public Instant getAttemptTime() {
        return attemptTime;
    }

    public void markInProgress() {
        attemptTime = Instant.now();
        attemptStatus = ATTEMPT_STATUS_ENUM.IN_PROGRESS;
    }
}
