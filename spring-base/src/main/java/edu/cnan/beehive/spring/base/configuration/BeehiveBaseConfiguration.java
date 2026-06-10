package edu.cnan.beehive.spring.base.configuration;

import edu.cnan.beehive.core.config.BootstrapConfigProperties;
import edu.cnan.beehive.spring.base.support.ApplicationContextHolder;
import edu.cnan.beehive.spring.base.support.BeehiveBeanPostProcessor;
import edu.cnan.beehive.spring.base.support.SpringPropertiesLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 作者：cnan
 * 开发时间：2026-06-10
 */
@Configuration
public class BeehiveBaseConfiguration {

    @Bean
    public ApplicationContextHolder applicationContextHolder() {
        return new ApplicationContextHolder();
    }

    @Bean
    public SpringPropertiesLoader springPropertiesLoader() {
        return new SpringPropertiesLoader();
    }

    // @Bean
    @DependsOn("applicationContextHolder")
    public BeehiveBeanPostProcessor beehiveBeanPostProcessor(BootstrapConfigProperties properties) {
        return new BeehiveBeanPostProcessor(properties);
    }

    // TODO: 以下 Bean 依赖 core/alarm、core/monitor、core/notification 模块，
    // 待这些模块完成后取消注释。
    //
    // @Bean
    // public NotifierDispatcher notifierDispatcher() { ... }
    //
    // @Bean(initMethod = "start", destroyMethod = "stop")
    // public ThreadPoolAlarmChecker threadPoolAlarmChecker(...) { ... }
    //
    // @Bean(initMethod = "start", destroyMethod = "stop")
    // public ThreadPoolMonitor threadPoolMonitor() { ... }
}
