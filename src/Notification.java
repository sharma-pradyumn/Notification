import java.time.LocalDateTime;
import java.util.EnumSet;

/**
 * @param priority -> higher is more imp
 */
public record Notification(long notificationId, String userId, String messageContent,
                           EnumSet<CHANNEL_ENUM> targetChannels, int priority, LocalDateTime createdTime, RetryPolicy retryPolicy) {
}
