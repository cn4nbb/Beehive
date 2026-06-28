package edu.cnan.beehive.core.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data carrier for thread-pool configuration-change notifications.
 *
 * <p>The {@link #changes} map holds before/after pairs keyed by
 * property name (e.g. {@code "corePoolSize"}, {@code "maximumPoolSize"}).
 *
 * @author cnan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThreadPoolConfigChangeDTO {

    /**
     * unique thread-pool identifier
     */
    private String threadPoolId;

    /**
     * active Spring profile
     */
    private String activeProfile;

    /**
     * application name
     */
    private String applicationName;

    /**
     * node identifier (host address)
     */
    private String identify;

    /**
     * notification recipients (comma-separated)
     */
    private String receives;

    /**
     * 阻塞队列类型
     */
    private String workQueue;

    /**
     * changed properties
     * Key: property name (corePoolSize)
     * Value: before/after pair
     */
    private Map<String, ChangePair<?>> changes;

    /**
     * change timestamp
     */
    private String updateTime;

    /** A before/after pair for a single changed property. */
    @Data
    @AllArgsConstructor
    public static class ChangePair<T> {
        private T before;
        private T after;
    }
}
