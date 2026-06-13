package edu.cnan.beehive.core.parser;

/**
 * Enumeration of supported remote-configuration file formats.
 *
 * @author cnan
 */
public enum ConfigFileTypeEnum {
    /** {@code .properties} format. */
    PROPERTIES("properties"),

    /** {@code .yml} format. */
    YML("yml"),

    /** {@code .yaml} format. */
    YAML("yaml");

    /** The file extension (without leading dot). */
    private final String value;

    ConfigFileTypeEnum(String value) {
        this.value = value;
    }

    /**
     * Returns the enum constant matching the given value.
     * @param value the file extension, e.g. {@code "yml"}
     * @return the matching constant, defaulting to {@link #PROPERTIES}
     *         if no match is found
     */
    public static ConfigFileTypeEnum of(String value) {
        for (ConfigFileTypeEnum typeEnum : ConfigFileTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return PROPERTIES;
    }
}
