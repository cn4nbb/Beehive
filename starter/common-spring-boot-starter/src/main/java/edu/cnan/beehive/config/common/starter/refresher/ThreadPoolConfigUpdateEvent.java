package edu.cnan.beehive.config.common.starter.refresher;

import edu.cnan.beehive.core.config.BootstrapConfigProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * Spring application event published when the config center
 * pushes updated thread-pool properties.
 *
 * <p>Consumed by {@link DynamicThreadPoolRefreshListener}.
 *
 * @author cnan
 */
public class ThreadPoolConfigUpdateEvent extends ApplicationEvent {
    /** The refreshed configuration (same instance as the live bean,
     *  with field values updated in-place). */
    @Getter
    @Setter
    private BootstrapConfigProperties properties;

    public ThreadPoolConfigUpdateEvent(Object source, BootstrapConfigProperties properties) {
        super(source);
        this.properties = properties;
    }
}
