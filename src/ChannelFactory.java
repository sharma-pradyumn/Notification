import java.util.EnumMap;
import java.util.Map;

public class ChannelFactory {
    // EnumMap is highly optimized for enum keys (backed by an array internally)
    private final Map<CHANNEL_ENUM, Channel> channelRegistry;

    public ChannelFactory() {
        this.channelRegistry = new EnumMap<>(CHANNEL_ENUM.class);

        // Pre-instantiate singletons at startup
        // In a real Spring Boot app, these would be injected, but for LLD, manual registry is perfect.
        channelRegistry.put(CHANNEL_ENUM.SMS, new SMS_Channel());
        channelRegistry.put(CHANNEL_ENUM.EMAIL, new Email_Channel());
        channelRegistry.put(CHANNEL_ENUM.PUSH, new Push_Channel());
    }

    public Channel getChannel(CHANNEL_ENUM channelType) {
        Channel channel = channelRegistry.get(channelType);
        if (channel == null) {
            // Always good practice to handle the edge case where an enum exists but no strategy is mapped
            throw new IllegalArgumentException("Strategy not implemented for channel: " + channelType);
        }
        return channel;
    }
}