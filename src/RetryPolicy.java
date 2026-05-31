import java.time.Instant;

public interface RetryPolicy {
    public Instant getRetryDelay(int previousAttemptCount, Instant previousExecutionStart);
}
