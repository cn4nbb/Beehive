package edu.cnan.beehive.config.common.starter.refresher;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import edu.cnan.beehive.core.config.BootstrapConfigProperties;
import edu.cnan.beehive.core.executor.BeehiveExecutor;
import edu.cnan.beehive.core.executor.BeehiveExecutorHolder;
import edu.cnan.beehive.core.executor.BeehiveExecutorProperties;
import edu.cnan.beehive.core.executor.BeehiveExecutorRegistry;
import edu.cnan.beehive.core.executor.support.BlockingQueueTypeEnum;
import edu.cnan.beehive.core.executor.support.RejectedPolicyTypeEnum;
import edu.cnan.beehive.core.executor.support.ResizableCapacityLinkedBlockingQueue;
import edu.cnan.beehive.spring.base.support.ApplicationContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static edu.cnan.beehive.core.constant.Constants.CHANGE_DELIMITER;
import static edu.cnan.beehive.core.constant.Constants.CHANGE_THREAD_POOL_TEXT;

/**
 * 作者：cnan
 * 开发时间：2026-06-13
 */
@Slf4j
@RequiredArgsConstructor
public class DynamicThreadPoolRefreshListener implements ApplicationListener<ThreadPoolConfigUpdateEvent> {
    // TODO  core.notification.service.NotifierDispatcher
    // private final NotifierDispatcher notifierDispatcher;

    @Override
    public void onApplicationEvent(ThreadPoolConfigUpdateEvent event) {
        BootstrapConfigProperties refresherProperties = event.getProperties();

        if (CollUtil.isEmpty(refresherProperties.getExecutorProperties())) {
            return;
        }

        for (BeehiveExecutorProperties remoteProperties : refresherProperties.getExecutorProperties()) {
            String threadPoolId = remoteProperties.getThreadPoolId();

            synchronized (threadPoolId.intern()) {
                boolean changed = hasThreadPoolConfigChanged(remoteProperties);

                if (!changed) {
                    continue;
                }

                updateThreadPoolFromRemoteConfig(remoteProperties);

                BeehiveExecutorHolder holder = BeehiveExecutorRegistry.getHolder(threadPoolId);
                BeehiveExecutorProperties originalProperties = holder.getExecutorProperties();
                holder.setExecutorProperties(remoteProperties);

                // TODO sendThreadPoolConfigChangeMessage(originalProperties, remoteProperties)
                // sendThreadPoolConfigChangeMessage(originalProperties, remoteProperties);

                log.info(CHANGE_THREAD_POOL_TEXT,
                        threadPoolId,
                        String.format(CHANGE_DELIMITER, originalProperties.getCorePoolSize(), remoteProperties.getCorePoolSize()),
                        String.format(CHANGE_DELIMITER, originalProperties.getMaximumPoolSize(), remoteProperties.getMaximumPoolSize()),
                        String.format(CHANGE_DELIMITER, originalProperties.getQueueCapacity(), remoteProperties.getQueueCapacity()),
                        String.format(CHANGE_DELIMITER, originalProperties.getKeepAliveTime(), remoteProperties.getKeepAliveTime()),
                        String.format(CHANGE_DELIMITER, originalProperties.getRejectedHandler(), remoteProperties.getRejectedHandler()),
                        String.format(CHANGE_DELIMITER, originalProperties.getAllowCoreThreadTimeOut(), remoteProperties.getAllowCoreThreadTimeOut())
                );
            }
        }
    }

    private boolean hasThreadPoolConfigChanged(BeehiveExecutorProperties remoteProperties) {
        String threadPoolId = remoteProperties.getThreadPoolId();
        BeehiveExecutorHolder holder = BeehiveExecutorRegistry.getHolder(threadPoolId);

        if (holder == null) {
            log.warn("No thread pool found for id :{}", threadPoolId);
            return false;
        }

        BeehiveExecutorProperties originalProperties = holder.getExecutorProperties();
        ThreadPoolExecutor executor = holder.getExecutor();

        return hasDifference(remoteProperties, originalProperties, executor);
    }

    private boolean hasDifference(BeehiveExecutorProperties remoteProperties,
                                  BeehiveExecutorProperties originalProperties,
                                  ThreadPoolExecutor executor) {
        return isChanged(remoteProperties.getCorePoolSize(), originalProperties.getCorePoolSize()) ||
                isChanged(remoteProperties.getMaximumPoolSize(), originalProperties.getMaximumPoolSize()) ||
                isChanged(remoteProperties.getKeepAliveTime(), originalProperties.getKeepAliveTime()) ||
                isChanged(remoteProperties.getAllowCoreThreadTimeOut(), originalProperties.getAllowCoreThreadTimeOut()) ||
                isChanged(remoteProperties.getRejectedHandler(), originalProperties.getRejectedHandler()) ||
                isQueueCapacityChanged(remoteProperties, originalProperties, executor);
    }

    private <T> boolean isChanged(T after, T before) {
        return after != null && !Objects.equals(after, before);
    }

    private boolean isQueueCapacityChanged(BeehiveExecutorProperties remoteProperties,
                                           BeehiveExecutorProperties originalProperties,
                                           ThreadPoolExecutor executor) {
        Integer remoteCapacity = remoteProperties.getQueueCapacity();
        Integer originalCapacity = originalProperties.getQueueCapacity();
        BlockingQueue<Runnable> queue = executor.getQueue();

        return remoteCapacity != null &&
                !Objects.equals(remoteCapacity, originalCapacity) &&
                Objects.equals(BlockingQueueTypeEnum.RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE.getName(), queue.getClass().getSimpleName());
    }

    private void updateThreadPoolFromRemoteConfig(BeehiveExecutorProperties remoteProperties) {
        String threadPoolId = remoteProperties.getThreadPoolId();
        BeehiveExecutorHolder holder = BeehiveExecutorRegistry.getHolder(threadPoolId);
        BeehiveExecutorProperties originalProperties = holder.getExecutorProperties();
        ThreadPoolExecutor executor = holder.getExecutor();

        Integer remoteCoreSize = remoteProperties.getCorePoolSize();
        Integer remoteMaximumSize = remoteProperties.getMaximumPoolSize();

        if (remoteCoreSize != null && remoteMaximumSize != null) {
            if (remoteCoreSize > originalProperties.getMaximumPoolSize()) {
                executor.setMaximumPoolSize(remoteMaximumSize);
                executor.setCorePoolSize(remoteCoreSize);
            } else {
                executor.setCorePoolSize(remoteCoreSize);
                executor.setMaximumPoolSize(remoteMaximumSize);
            }
        } else{
            if (remoteCoreSize != null) {
                executor.setCorePoolSize(remoteCoreSize);
            }
            if (remoteMaximumSize != null) {
                executor.setMaximumPoolSize(remoteMaximumSize);
            }
        }

        if (remoteProperties.getAllowCoreThreadTimeOut() != null &&
            !Objects.equals(remoteProperties.getAllowCoreThreadTimeOut(), originalProperties.getAllowCoreThreadTimeOut())) {
            executor.allowCoreThreadTimeOut(remoteProperties.getAllowCoreThreadTimeOut());
        }

        if (remoteProperties.getKeepAliveTime() != null &&
            !Objects.equals(remoteProperties.getKeepAliveTime(), originalProperties.getKeepAliveTime())) {
            executor.setKeepAliveTime(remoteProperties.getKeepAliveTime(), TimeUnit.SECONDS);
        }

        if (remoteProperties.getRejectedHandler() != null &&
            !Objects.equals(remoteProperties.getRejectedHandler(), originalProperties.getRejectedHandler())) {
            executor.setRejectedExecutionHandler(RejectedPolicyTypeEnum.createPolicy(remoteProperties.getRejectedHandler()));
        }

        if (isQueueCapacityChanged(remoteProperties, originalProperties, executor)) {
            BlockingQueue<Runnable> queue = executor.getQueue();
            ResizableCapacityLinkedBlockingQueue<?> resizableCapacityQueue = (ResizableCapacityLinkedBlockingQueue<?>) queue;
            resizableCapacityQueue.setCapacity(remoteProperties.getQueueCapacity());
        }
    }

}
