package edu.cnan.beehive.core.executor.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Health-check threshold configuration for thread-pool monitoring.
 *
 * <p>Two independent thresholds are evaluated periodically:
 * <ul>
 *   <li><b>Queue threshold</b> — the ratio of
 *       {@code queue.size() / queue.capacity()}, expressed as
 *       a percentage.</li>
 *   <li><b>Active-thread threshold</b> — the ratio of
 *       {@code activeCount / maximumPoolSize}, expressed as
 *       a percentage.</li>
 * </ul>
 *
 * <p>When either ratio equals or exceeds its configured threshold,
 * an alarm is raised.
 *
 * <p>An instance with alarm enabled, 80% queue threshold,
 * and 80% active-thread threshold is used as the default
 * when no explicit alarm configuration is supplied.
 *
 * @author cnan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmConfig {
    /**
     * Whether the alarm mechanism is enabled.
     * Defaults to {@code true}.
     */
    private Boolean enable = Boolean.TRUE;

    /**
     * Queue usage percentage threshold, in the range 0–100.
     * An alarm is triggered when
     * {@code queue.size() * 100 / queue.capacity() >= queueThreshold}.
     * Defaults to {@code 80}.
     */
    private Integer queueThreshold = 80;

    /**
     * Active-thread percentage threshold, in the range 0–100.
     * An alarm is triggered when
     * {@code activeCount * 100 / maximumPoolSize >= activeThreshold}.
     * Defaults to {@code 80}.
     */
    private Integer activeThreshold = 80;
}
