package edu.cnan.beehive.config.common.starter.refresher;

import edu.cnan.beehive.core.config.BootstrapConfigProperties;
import edu.cnan.beehive.core.parser.ConfigParserHandler;
import edu.cnan.beehive.spring.base.support.ApplicationContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

/**
 * Template-method base class for config-center refresh handlers.
 *
 * <p>Implements {@link ApplicationRunner} so that
 * {@link #registerListener()} is called automatically after the
 * application context is ready.
 *
 * <p>Subclasses must implement {@link #registerListener()} to
 * subscribe to their specific config center (Nacos, Apollo, etc.).
 *
 * @author cnan
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractDynamicThreadPoolRefresher implements ApplicationRunner {
    /** The live configuration instance (updated in-place on refresh). */
    protected final BootstrapConfigProperties properties;

    /**
     * Subclasses register their config-center listener here.
     * Called by {@link #run} after the context is ready.
     */
    protected abstract void registerListener() throws Exception;

    /** Hook called before {@link #registerListener()}. */
    protected void beforeRegister() {

    }

    /** Hook called after {@link #registerListener()}. */
    protected void afterRegister() {

    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        beforeRegister();
        registerListener();
        afterRegister();
    }

    /**
     * Parses the raw config string and updates
     * {@link #properties} in-place, then publishes a
     * {@link ThreadPoolConfigUpdateEvent}.
     */
    @SneakyThrows
    public void refreshThreadPoolProperties(String configInfo) {
        Map<Object, Object> configInfoMap = ConfigParserHandler.getInstance().parseConfig(configInfo, properties.getConfigFileType());
        ConfigurationPropertySource sources = new MapConfigurationPropertySource(configInfoMap);
        Binder binder = new Binder(sources);

        BootstrapConfigProperties refresherProperties = binder.bind(BootstrapConfigProperties.PREFIX, Bindable.ofInstance(properties)).get();

        ApplicationContextHolder.getInstance().publishEvent(new ThreadPoolConfigUpdateEvent(this, refresherProperties));
    }
}
