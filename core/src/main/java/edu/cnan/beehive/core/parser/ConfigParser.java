package edu.cnan.beehive.core.parser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Strategy interface for parsing configuration content strings
 * into a flat key-value map.
 *
 * @author cnan
 */
public interface ConfigParser {
    /** @return {@code true} if this parser supports the given file type. */
    boolean supports(ConfigFileTypeEnum type);

    /**
     * Parses the given content string.
     * @return a flat map, never {@code null}
     */
    Map<Object, Object> doParse(String content) throws IOException;

    /** @return the file types this parser handles. */
    List<ConfigFileTypeEnum> getConfigFileTypes();
}
