package edu.cnan.beehive.core.executor.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Notification recipient and rate-limiting configuration for
 * thread-pool alarms.
 *
 * <p>When an alarm is triggered (e.g. queue usage exceeds
 * {@link AlarmConfig#getQueueThreshold()}), the notification
 * is sent to the configured {@code receives} list; subsequent
 * alarms within the same {@code interval} window are suppressed
 * to avoid flooding.
 *
 * <p>If this configuration is {@code null} on the parent
 * {@code BeehiveExecutorProperties}, alarms are logged but
 * not dispatched to any external channel.
 *
 * @author cnan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyConfig {
    /**
     * Comma-separated list of notification recipients.
     * May be {@code null} or empty if no recipients are configured.
     */
    private String receives;

    /**
     * Minimum interval between successive alarm notifications,
     * in minutes.
     */
    private Integer interval = 5;
}
