package edu.cnan.beehive.core.parser;

/**
 * Skeletal {@link ConfigParser} implementation.
 * Subclasses need only implement {@link #doParse(String)}
 * and {@link #getConfigFileTypes()}.
 *
 * @author cnan
 */
public abstract class AbstractConfigParser implements ConfigParser{
    @Override
    public boolean supports(ConfigFileTypeEnum type) {
        return getConfigFileTypes().contains(type);
    }
}
