import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NotificationExecutor {

    private final ExecutorService executorService;

    public NotificationExecutor(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    public CompletableFuture<NotificationResult> executeNotificationAttempt(NotificationAttempt notificationAttempt){
        return CompletableFuture.supplyAsync(()->sendNotification(notificationAttempt), executorService)// SDE-2 Move: Never let a network call hang indefinitely.
                .completeOnTimeout(
                        new NotificationResult(
                                notificationAttempt.notification.notificationId(),
                                notificationAttempt.getNotificationAttemptId(),
                                RESULT_ENUM.FAILED_TEMPORARY
                        ),
                        3, TimeUnit.SECONDS // Aggressively timeout after 3 seconds
                );
    }

    private NotificationResult sendNotification(NotificationAttempt notificationAttempt) {
        try {
            return notificationAttempt.channel.sendNotification(notificationAttempt);
        } catch (Exception e) {
            // add exception checks here for temp or permanent Failures
            return new NotificationResult(notificationAttempt.getNotification().notificationId(), notificationAttempt.getNotificationAttemptId(), RESULT_ENUM.FAILED_TEMPORARY);
        }
    }

    public void shutdownExecution(long timeoutMillis) {
        executorService.shutdown();
        try{
            if(!executorService.awaitTermination(timeoutMillis, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
