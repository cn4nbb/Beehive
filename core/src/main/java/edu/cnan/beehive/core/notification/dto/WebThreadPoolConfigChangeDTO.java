package edu.cnan.beehive.core.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data carrier for web-container thread-pool configuration-change
 * notifications (Tomcat / Jetty / Undertow).
 *
 * <p>Differs from {@link ThreadPoolConfigChangeDTO} by including
 * {@code webContainerName} rather than {@code threadPoolId}.
 *
 * @author cnan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebThreadPoolConfigChangeDTO {
    /**
     * web container name (Tomcat / Jetty / Undertow)
     */
    private String webContainerName;

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
     * changed properties
     * Key: property name (corePoolSize)
     * Value: before/after pair
     */
    private Map<String, ChangePair<?>> changes;

    /**
     * change timestamp
     */
    private String updateTime;

    @Data
    @AllArgsConstructor
    public static class ChangePair<T> {
        private T before;
        private T after;
    }
}
