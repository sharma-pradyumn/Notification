import java.time.Instant;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.DelayQueue;

public class NotificationOrchestrator {
    private final DelayQueue<NotificationAttempt> attemptDelayQueue = new DelayQueue<NotificationAttempt>();
    PreferenceService preferenceService;
    NotificationRepository notificationRepository;
    NotificationExecutor notificationExecutor;
    ChannelFactory channelFactory;
    Thread workerThread;
    volatile boolean isOrchestratorRunning = true;

    public NotificationOrchestrator(PreferenceService p, NotificationRepository nr, NotificationExecutor ne, ChannelFactory cf) {
        preferenceService = p;
        notificationRepository = nr;
        notificationExecutor = ne;
        channelFactory = cf;
        workerThread = new Thread(null, this::orchestrateNotificationExecution,"NotificationOrchestrator-worker");
    }

    public void startOrchestration() {
        isOrchestratorRunning = true;
        workerThread.start();
    }

    public void stopOrchestration() {
        isOrchestratorRunning = false;
        workerThread.interrupt();
    }

    public void sendNotification(Notification notification) {
        notificationRepository.addNotification(notification);
        Set<CHANNEL_ENUM> channels = preferenceService.getUserChannels(notification.userId());
        for(CHANNEL_ENUM channelEnum: channels) {
            Channel channel = channelFactory.getChannel(channelEnum);
            NotificationAttempt notificationAttempt = new NotificationAttempt(notification, channelEnum, Instant.now(), 1, channel, ATTEMPT_STATUS_ENUM.PENDING);
            addNotificationAttempt(notification, channelEnum, Instant.now(), 1, channel);
        }
    }

    private void processNotificationResult(NotificationAttempt notificationAttempt, NotificationResult notificationResult) {
        if(notificationResult.resultEnum() == RESULT_ENUM.FAILED_TEMPORARY) {
            notificationAttempt.attemptStatus = ATTEMPT_STATUS_ENUM.FAILED;
            Instant nextExecutionInstant = notificationAttempt.notification.retryPolicy().getRetryDelay(notificationAttempt.attemptCount, notificationAttempt.attemptTime);
            if (nextExecutionInstant != null) {
                addNotificationAttempt(notificationAttempt.notification, notificationAttempt.channelEnum, nextExecutionInstant, notificationAttempt.attemptCount+1, notificationAttempt.channel);
            }
        }
    }

    private void addNotificationAttempt(Notification notification, CHANNEL_ENUM channelEnum, Instant executionTime, int attemptCount, Channel channel) {
        NotificationAttempt notificationAttempt = new NotificationAttempt(notification, channelEnum, executionTime,  attemptCount, channel, ATTEMPT_STATUS_ENUM.PENDING);
        notificationRepository.addNotificationAttempt(notificationAttempt);
        attemptDelayQueue.add(notificationAttempt);
    }

    private void orchestrateNotificationExecution() {
        try {
            while (isOrchestratorRunning) {
                NotificationAttempt notificationAttempt = attemptDelayQueue.take();
                notificationAttempt.markInProgress();

                // ASYNC HANDOFF: Do not block the while-loop!
                // We pass to the executor, and attach a callback for when it finishes.
                CompletableFuture<NotificationResult> futureResult = notificationExecutor.executeNotificationAttempt(notificationAttempt);
                futureResult.thenAccept(result -> processNotificationResult(notificationAttempt, result));
            }
        } catch (InterruptedException e) {
            isOrchestratorRunning = false;
            Thread.currentThread().interrupt();
        }
    }

}

//NotificationOrchestrator
//Workflow coordinator.
//
//Responsibilities:
//Receive notification request
//↓
//Query PreferenceService
//↓
//Create Notification
//↓
//Create NotificationAttempts
//↓
//Persist
//↓
//Submit attempts to NotificationExecutor
//↓
//Process SendResult
//↓
//Update repository
//↓
//Invoke RetryPolicy if needed
//
//
//High-Level Flow
//
//sendNotification()
//
//NotificationOrchestrator
//        ↓
//PreferenceService
//        ↓
//Enabled Channels
//        ↓
//Create Notification
//        ↓
//Create NotificationAttempts
//        ↓
//NotificationRepository
//        ↓
//NotificationExecutor
//        ↓
//EmailChannel / SmsChannel / PushChannel
//        ↓
//SendResult
//        ↓
//NotificationOrchestrator
//        ↓
//RetryPolicy / Status Update
