package edu.cnan.beehive.core.parser;

import cn.hutool.core.collection.CollectionUtil;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Parses {@code .properties}-format content into a flat key-value map.
 *
 * <p>Delegates to {@link java.util.Properties#load(java.io.Reader)}.
 * The returned map is the {@code Properties} instance itself
 * (which extends {@link java.util.Hashtable Hashtable&lt;Object,Object&gt;}).
 *
 * @author cnan
 */
public class PropertiesConfigParser extends AbstractConfigParser{
    @Override
    public Map<Object, Object> doParse(String content) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        return properties;
    }

    @Override
    public List<ConfigFileTypeEnum> getConfigFileTypes() {
        return CollectionUtil.newArrayList(ConfigFileTypeEnum.PROPERTIES);
    }
}
