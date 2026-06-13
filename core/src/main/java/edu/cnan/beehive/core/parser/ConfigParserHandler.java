package edu.cnan.beehive.core.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Singleton registry and dispatcher for {@link ConfigParser}
 * implementations.
 *
 * <p>Registered parsers:
 * <ul>
 *   <li>{@link YamlConfigParser} — handles {@code .yml}, {@code .yaml}</li>
 *   <li>{@link PropertiesConfigParser} — handles {@code .properties}</li>
 * </ul>
 *
 * <p>Access via {@link #getInstance()}.
 *
 * @author cnan
 */
public class ConfigParserHandler {
    /** Registered parsers. */
    private final static List<ConfigParser> PARSERS = new ArrayList<>();

    private ConfigParserHandler() {
        PARSERS.add(new YamlConfigParser());
        PARSERS.add(new PropertiesConfigParser());
    }

    /**
     * Parses the content using the first parser that
     * {@linkplain ConfigParser#supports supports} the given type.
     *
     * @return the parsed flat map, or an empty map if no parser matches
     */
    public Map<Object, Object> parseConfig(String content, ConfigFileTypeEnum type) throws IOException {
        for (ConfigParser parser : PARSERS) {
            if (parser.supports(type)) {
                return parser.doParse(content);
            }
        }
        return Collections.emptyMap();
    }

    /** @return the singleton instance. */
    public static ConfigParserHandler getInstance() {
        return ConfigParserHandlerHolder.INSTANCE;
    }

    /** Holder for lazy singleton. */
    private static class ConfigParserHandlerHolder {
        private static final ConfigParserHandler INSTANCE = new ConfigParserHandler();
    }
}
