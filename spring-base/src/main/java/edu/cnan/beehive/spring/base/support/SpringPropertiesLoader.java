package edu.cnan.beehive.spring.base.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import edu.cnan.beehive.core.config.ApplicationProperties;
/**
 * 动态线程池 Spring 配置加载
 * 作者：cnan
 * 开发时间：2026-06-10
 */
public class SpringPropertiesLoader implements InitializingBean {
    @Value("${spring.application.name:UNKOWN}")
    private String applicationName;

    @Value("${spring.profiles.active:UNKOWN}")
    private String activeProfile;

    @Override
    public void afterPropertiesSet() throws Exception {
        ApplicationProperties.setApplicationName(applicationName);
        ApplicationProperties.setActiveProfile(activeProfile);
    }
}
