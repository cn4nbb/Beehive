package edu.cnan.beehive.core.config;

import lombok.Getter;
import lombok.Setter;

/**
 * Static holder for application metadata obtained from the Spring
 * environment.
 *
 * @author cnan
 */
public class ApplicationProperties {
    /** The application name (e.g. {@code order-service}). */
    @Getter
    @Setter
    private static String applicationName;

    /** The active Spring profile (e.g. {@code dev}, {@code prod}). */
    @Getter
    @Setter
    private static String activeProfile;

}
