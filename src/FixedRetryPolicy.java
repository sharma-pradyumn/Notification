import java.time.Duration;
import java.time.Instant;

public record FixedRetryPolicy(int maxRetryCount, Duration retryInterval) implements RetryPolicy
{
    public Instant getRetryDelay(int previousAttemptCount, Instant previousExecutionStart) {
        if(previousAttemptCount == maxRetryCount) {
            return null;
        }
        return previousExecutionStart.plus(retryInterval);
    }
}
