import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PreferenceService {
    Map<String, Set<CHANNEL_ENUM>> userPreferenceMap = new ConcurrentHashMap<>();

    public void addChannelForUser(String userId, CHANNEL_ENUM channel) {
        userPreferenceMap.compute(userId, (k, oldValue) -> {
            Set<CHANNEL_ENUM> newValue = oldValue == null ? EnumSet.noneOf(CHANNEL_ENUM.class) : EnumSet.copyOf(oldValue);
            // IMP -> fresh copy prevents write after read modifying same variable
            newValue.add(channel);
            return newValue;
        });
    }

    // if no preference saved, we send in all. Opt-out
    public Set<CHANNEL_ENUM> getUserChannels(String userId) {
        return userPreferenceMap.getOrDefault(userId, EnumSet.allOf(CHANNEL_ENUM.class));
    }
}
