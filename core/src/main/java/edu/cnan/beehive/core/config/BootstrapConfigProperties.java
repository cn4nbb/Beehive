package edu.cnan.beehive.core.config;

import edu.cnan.beehive.core.executor.BeehiveExecutorProperties;
import edu.cnan.beehive.core.parser.ConfigFileTypeEnum;
import lombok.Data;

import java.util.List;

/**
 * Root configuration model bound to the {@code beehive.*} namespace.
 *
 * <p>Maintains a static singleton so that core-layer components
 * (which have no access to the Spring container) can retrieve
 * the configuration globally via {@link #getInstance()}.
 */
@Data
public class BootstrapConfigProperties {

    /**
     * The configuration prefix: {@value}.
     */
    public static final String PREFIX = "beehive";

    /** Master switch; defaults to {@code true}. */
    private Boolean enable = Boolean.TRUE;

    /** Nacos connection settings. */
    private NacosConfig nacos;

    /** Apollo connection settings. */
    private ApolloConfig apollo;

    /** Web-container thread-pool settings. */
    private WebThreadPoolExecutorConfig web;

    /** Remote configuration file format. */
    private ConfigFileTypeEnum configFileType;

    /** Notification platform settings. */
    private NotifyPlatformsConfig notifyPlatforms;

    /** Monitoring configuration. Enabled by default, 10-second interval. */
    private MonitorConfig monitor = new MonitorConfig();

    /** The list of managed thread-pool configurations. */
    private List<BeehiveExecutorProperties> executorProperties;

    /** The singleton instance. */
    private static BootstrapConfigProperties INSTANCE = new BootstrapConfigProperties();

    /** @return the global singleton. */
    public static BootstrapConfigProperties getInstance() {
        return INSTANCE;
    }

    /** Sets the global singleton. */
    public static void setInstance(BootstrapConfigProperties properties) {
        INSTANCE = properties;
    }
}
