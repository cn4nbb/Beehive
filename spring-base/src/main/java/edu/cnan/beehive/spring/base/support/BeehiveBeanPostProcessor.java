package edu.cnan.beehive.spring.base.support;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;
import edu.cnan.beehive.core.config.BootstrapConfigProperties;
import edu.cnan.beehive.core.executor.BeehiveExecutor;
import edu.cnan.beehive.core.executor.BeehiveExecutorProperties;
import edu.cnan.beehive.core.executor.BeehiveExecutorRegistry;
import edu.cnan.beehive.core.executor.support.BlockingQueueTypeEnum;
import edu.cnan.beehive.core.executor.support.RejectedPolicyTypeEnum;
import edu.cnan.beehive.spring.base.DynamicThreadPool;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * {@link BeanPostProcessor} that discovers
 * {@link BeehiveExecutor} beans annotated with
 * {@link DynamicThreadPool}, overrides their local configuration
 * with values from the config center, and registers them into
 * {@link BeehiveExecutorRegistry}.
 *
 * @author cnan
 */
@Slf4j
@Data
@RequiredArgsConstructor
public class BeehiveBeanPostProcessor implements BeanPostProcessor {
    /** Remote configuration source from the config center. */
    private final BootstrapConfigProperties properties;

    /**
     * {@inheritDoc}
     *
     * <p>Intercepts {@link BeehiveExecutor} beans marked with
     * {@link DynamicThreadPool} and applies remote configuration
     * before they enter service.
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof BeehiveExecutor) {
            DynamicThreadPool dynamicThreadPool;
            try {
                dynamicThreadPool = ApplicationContextHolder.findAnnotationOnBean(beanName, DynamicThreadPool.class);
                if (Objects.isNull(dynamicThreadPool)) {
                    return bean;
                }
            } catch (Exception ex) {
                log.error("Failed to create dynamic thread pool in annotation mode.", ex);
            }

            BeehiveExecutor beehiveExecutor = (BeehiveExecutor) bean;

            BeehiveExecutorProperties executorProperties = properties.getExecutorProperties()
                    .stream()
                    .filter(each -> Objects.equals(beehiveExecutor.getThreadPoolId(), each.getThreadPoolId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("The thread pool id does not exist in the configuration."));

            overrideLocalThreadPoolConfig(executorProperties, beehiveExecutor);

            BeehiveExecutorRegistry.putHolder(beehiveExecutor.getThreadPoolId(), beehiveExecutor, executorProperties);
        }

        return bean;
    }

    /**
     * Overwrites the executor's local parameters with the given
     * remote configuration.
     *
     * <p>Parameters replaced:
     * <ul>
     *   <li>core pool size and maximum pool size</li>
     *   <li>work queue (via reflection — {@code ThreadPoolExecutor}
     *       has no setter)</li>
     *   <li>keep-alive time and core-thread timeout policy</li>
     *   <li>rejected execution handler</li>
     * </ul>
     */
    private void overrideLocalThreadPoolConfig(BeehiveExecutorProperties properties, BeehiveExecutor executor) {
        Integer remoteCorePoolSize = properties.getCorePoolSize();
        Integer remoteMaximumPoolSize = properties.getMaximumPoolSize();
        Assert.isTrue(remoteCorePoolSize <= remoteMaximumPoolSize, "remoteCorePoolSize must be smaller than remoteMaximumPoolSize.");

        Integer originalMaximumPoolSize = executor.getMaximumPoolSize();

         // ThreadPoolExecutor.setCorePoolSize checks that the new core size does not
         // exceed the *current* maximum.  Likewise, setMaximumPoolSize checks against
         // the *current* core size.  If we set them in the wrong order, the intermediate
         // state may violate the invariant corePoolSize <= maximumPoolSize.
        if (remoteCorePoolSize > originalMaximumPoolSize) {
            executor.setMaximumPoolSize(remoteMaximumPoolSize);
            executor.setCorePoolSize(remoteCorePoolSize);
        } else {
            executor.setCorePoolSize(remoteCorePoolSize);
            executor.setMaximumPoolSize(remoteMaximumPoolSize);
        }

        BlockingQueue blockingQueue = BlockingQueueTypeEnum.createBlockingQueue(properties.getWorkQueue(), properties.getQueueCapacity());
        // ThreadPoolExecutor.workQueue has no public setter and must be
        // assigned via reflection.  On Java 9+ (JPMS), accessing private
        // fields in java.base requires an explicit --add-opens directive:
        //
        //   --add-opens=java.base/java.util.concurrent=ALL-UNNAMED
        //
        // Add this to your IDE run configuration (VM options), Maven
        // Surefire/Failsafe plugin configuration, or the java -jar
        // launch script.
        ReflectUtil.setFieldValue(executor, "workQueue", blockingQueue);

        executor.setKeepAliveTime(properties.getKeepAliveTime(), TimeUnit.SECONDS);
        executor.allowCoreThreadTimeOut(properties.getAllowCoreThreadTimeOut());
        executor.setRejectedExecutionHandler(RejectedPolicyTypeEnum.createPolicy(properties.getRejectedHandler()));
    }
}
