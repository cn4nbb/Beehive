package edu.cnan.beehive.core.parser;

/**
 * 作者：cnan
 * 开发时间：2026-06-08
 */
public enum ConfigFileTypeEnum {
    /**
     * PROPERTIES
     */
    PROPERTIES("properties"),

    /**
     * YML
     */
    YML("yml"),

    /**
     * YAML
     */
    YAML("yaml");

    private final String value;

    ConfigFileTypeEnum(String value) {
        this.value = value;
    }

    public static ConfigFileTypeEnum of(String value) {
        for (ConfigFileTypeEnum typeEnum : ConfigFileTypeEnum.values()) {
            if (typeEnum.value.equals(value)) {
                return typeEnum;
            }
        }
        return PROPERTIES;
    }
}
