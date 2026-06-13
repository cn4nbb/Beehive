package edu.cnan.beehive.config.common.starter.configuration;

import edu.cnan.beehive.core.config.BootstrapConfigProperties;
import edu.cnan.beehive.spring.base.configuration.BeehiveBaseConfiguration;
import edu.cnan.beehive.spring.base.enable.MarkerConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

/**
 * Autoconfiguration entry point for the Beehive common starter.
 *
 * <p>Activated when {@code @EnableBeehive} is placed on the
 * application and {@code beehive.enable} is {@code true} (or absent).
 * Binds the {@code beehive.*} namespace and imports
 * {@link BeehiveBaseConfiguration} for bean registration.
 *
 * @author cnan
 */
@ConditionalOnBean(MarkerConfiguration.Marker.class)
@ConditionalOnProperty(prefix = BootstrapConfigProperties.PREFIX,
        name = "enable", matchIfMissing = true, havingValue = "true")
@Import(BeehiveBaseConfiguration.class)
@AutoConfigureAfter(BeehiveBaseConfiguration.class)
@Configuration
public class CommonAutoConfiguration {

    @Bean
    public BootstrapConfigProperties bootstrapConfigProperties(
            Environment environment) {
        BootstrapConfigProperties props = Binder.get(environment)
                .bind(BootstrapConfigProperties.PREFIX,
                      Bindable.of(BootstrapConfigProperties.class))
                .get();
        BootstrapConfigProperties.setInstance(props);
        return props;
    }
}
