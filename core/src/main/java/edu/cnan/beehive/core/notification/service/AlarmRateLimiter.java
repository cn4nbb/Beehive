package edu.cnan.beehive.core.notification.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sliding-window rate limiter for thread-pool alarms.
 *
 * <p>Ensures that alarms of the same type for the same thread pool
 * are not sent more than once within the configured interval.
 *
 * <p>Uses {@link ConcurrentHashMap#compute} for an atomic
 * check-and-update without explicit locking:
 * <pre>{@code
 * compute(key, (k, lastTime) ->
 *     (lastTime == null || now - lastTime > interval) ? now : lastTime
 * ) == now;
 * }</pre>
 * If the remapping function returns {@code now}, the alarm is allowed;
 * if it returns {@code lastTime} (unchanged), the alarm is suppressed.
 *
 * <p>Key format: {@code "threadPoolId|alarmType"} (e.g.
 * {@code "order-pool|Capacity"}).  Different alarm types for the
 * same pool are rate-limited independently.
 *
 * @author cnan
 */
public class AlarmRateLimiter {
    /** Alarm timestamp cache: {@code "threadPoolId|alarmType" → lastSentMillis}. */
    private static final Map<String, Long> ALARM_RECORD = new ConcurrentHashMap<>();

    /** Builds the cache key. */
    private static String buildKey(String threadPoolId, String alarmType) {
        return threadPoolId + "|" + alarmType;
    }

    /**
     * Checks whether an alarm should be allowed.
     *
     * @param threadPoolId    the thread-pool id
     * @param alarmType       the alarm type (Capacity / Activity / Reject)
     * @param intervalMinutes minimum minutes between successive alarms
     * @return {@code true} if the alarm may be sent
     */
    public static boolean allowAlarm(String threadPoolId, String alarmType, int intervalMinutes) {
        String key = buildKey(threadPoolId, alarmType);
        long currentTime = System.currentTimeMillis();

        return ALARM_RECORD.compute(key, (k, lastTime) -> {
            if (lastTime == null || currentTime - lastTime > intervalMinutes * 60 * 1000L) {
                return currentTime;
            }
            return lastTime;
        }) == currentTime;
    }
}
