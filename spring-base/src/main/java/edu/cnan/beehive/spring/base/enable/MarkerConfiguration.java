package edu.cnan.beehive.spring.base.enable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers a marker bean used by {@code CommonAutoConfiguration}
 * to determine whether Beehive should be activated.
 *
 * <p>This class is not intended to be used directly — use
 * {@code @EnableBeehive} on the application entry point instead.
 *
 * @author cnan
 */
@Configuration
public class MarkerConfiguration {

    @Bean
    public Marker dynamicThreadPoolMarkerBean() {
        return new Marker();
    }

    public class Marker {

    }
}
