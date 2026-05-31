import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationRepository {
    private final AtomicLong nextNotificationId = new AtomicLong(0);
    private final ConcurrentHashMap<Long, Notification> notificationMap = new ConcurrentHashMap<>();

    // Primary lookup
    private final ConcurrentHashMap<String, NotificationAttempt> notificationAttemptMap = new ConcurrentHashMap<>();

    // Relational lookup (Notification ID -> List of its Attempts)
    private final ConcurrentHashMap<Long, List<NotificationAttempt>> notificationIdToAttemptMap = new ConcurrentHashMap<>();

    public void addNotification(Notification n) {
        notificationMap.put(n.notificationId(), n);
    }

    public void addNotificationAttempt(NotificationAttempt n) {
        // 1. Put in the main lookup map FIRST.
        notificationAttemptMap.put(n.getNotificationAttemptId(), n);
        // 2. Safely compute the list and add the attempt.
        notificationIdToAttemptMap.compute(n.getNotification().notificationId(), (notificationId, attemptList) -> {
            if (attemptList == null) {
                // Using CopyOnWriteArrayList prevents ConcurrentModificationExceptions
                // if a reader is iterating while we add a retry attempt.
                attemptList = new CopyOnWriteArrayList<>();
            }
            attemptList.add(n);
            return attemptList;
        });
    }

    public Notification getNotification(long id) {
        return notificationMap.get(id);
    }

    public NotificationAttempt getAttempt(String attemptId) {
        return notificationAttemptMap.get(attemptId);
    }

    public List<NotificationAttempt> getAttemptsForNotification(long notificationId) {
        // Return an empty list instead of null to prevent NPEs in the calling layer
        return notificationIdToAttemptMap.getOrDefault(notificationId, List.of());
    }

    public long getNextAvailableNotificationId() {
        return nextNotificationId.getAndIncrement();
    }
}