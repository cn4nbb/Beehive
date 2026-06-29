package edu.cnan.beehive.core.monitor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson2.JSON;
import edu.cnan.beehive.core.config.ApplicationProperties;
import edu.cnan.beehive.core.config.BootstrapConfigProperties;
import edu.cnan.beehive.core.config.MonitorConfig;
import edu.cnan.beehive.core.executor.BeehiveExecutor;
import edu.cnan.beehive.core.executor.BeehiveExecutorHolder;
import edu.cnan.beehive.core.executor.BeehiveExecutorProperties;
import edu.cnan.beehive.core.executor.BeehiveExecutorRegistry;
import edu.cnan.beehive.core.toolkit.ThreadFactoryBuilder;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Periodic runtime-metric collector for registered dynamic thread pools.
 *
 * <p>On {@link #start()}, reads the {@link MonitorConfig} from
 * {@link BootstrapConfigProperties} and begins a scheduled task
 * that collects a {@link ThreadPoolRuntimeInfo} snapshot from
 * every pool in {@link BeehiveExecutorRegistry} at the configured
 * interval.
 *
 * <p>Two collection modes are supported, selected by
 * {@link MonitorConfig#getCollectType()}:
 * <table>
 *   <tr><th>Value</th><th>Behavior</th></tr>
 *   <tr><td>{@code "log"}</td>
 *       <td>Dumps each snapshot as a JSON line to the log.</td></tr>
 *   <tr><td>{@code "micrometer"}</td>
 *       <td>Registers Gauges in the Micrometer global registry
 *           (accessible via {@code /actuator/prometheus}).</td></tr>
 * </table>
 *
 * <p>Lifecycle: {@link #start()} begins the schedule;
 * {@link #stop()} shuts down the scheduler.
 *
 * @author cnan
 */
@Slf4j
public class ThreadPoolMonitor {
    /** Single-thread scheduler for periodic collection. */
    private ScheduledExecutorService scheduledExecutorService;
    /** Cache: threadPoolId → snapshot (updated in-place each cycle). */
    private Map<String, ThreadPoolRuntimeInfo> micrometerMonitorCache;
    /** Cache: threadPoolId → reject-count delta wrapper. */
    private Map<String, DeltaWrapper> rejectCountDeltaMap;
    /** Cache: threadPoolId → completed-task delta wrapper. */
    private Map<String, DeltaWrapper> completedTaskDeltaMap;

    private static final String METRIC_NAME_PREFIX = "dynamic.thread-pool";
    private static final String DYNAMIC_THREAD_POOL_ID_TAG = METRIC_NAME_PREFIX + ".id";
    private static final String APPLICATION_NAME_TAG = "application.name";

    /**
     * Starts the periodic collection task.
     * Has no effect if monitoring is disabled in configuration.
     */
    public void start() {
        MonitorConfig monitorConfig = BootstrapConfigProperties.getInstance().getMonitor();
        if (!monitorConfig.getEnable()) {
            return;
        }

        micrometerMonitorCache = new ConcurrentHashMap<>();
        rejectCountDeltaMap = new ConcurrentHashMap<>();
        completedTaskDeltaMap = new ConcurrentHashMap<>();

        scheduledExecutorService = Executors.newScheduledThreadPool(
                1,
                ThreadFactoryBuilder.
                        builder()
                        .namePrefix("scheduler_thread-pool_monitor")
                        .build()
        );

        scheduledExecutorService.scheduleWithFixedDelay(() ->{
            Collection<BeehiveExecutorHolder> holders = BeehiveExecutorRegistry.getAllHolders();

            for (BeehiveExecutorHolder holder : holders) {
                ThreadPoolRuntimeInfo runtimeInfo = buildThreadPoolRuntimeInfo(holder);

                if (Objects.equals(monitorConfig.getCollectType(), "log")) {
                    logMonitor(runtimeInfo);
                } else if (Objects.equals(monitorConfig.getCollectType(), "micrometer")){
                    micrometerMonitor(runtimeInfo);
                }
            }
        }, 0, monitorConfig.getCollectInterval(), TimeUnit.SECONDS);
    }

    /** Shuts down the scheduler. */
    public void stop() {
        if (scheduledExecutorService != null && !scheduledExecutorService.isShutdown()) {
            scheduledExecutorService.shutdown();
        }
    }

    /** Logs a snapshot as JSON. */
    private void logMonitor(ThreadPoolRuntimeInfo runtimeInfo) {
        log.info("[ThreadPool Monitor] {} | Content: {}", runtimeInfo.getThreadPoolId(), JSON.toJSON(runtimeInfo));
    }

    /**
     * Registers or updates Micrometer Gauges for the snapshot.
     */
    private void micrometerMonitor(ThreadPoolRuntimeInfo runtimeInfo) {
        String threadPoolId = runtimeInfo.getThreadPoolId();
        ThreadPoolRuntimeInfo existingRuntimeInfo = micrometerMonitorCache.get(threadPoolId);

        if (existingRuntimeInfo == null) {
            Iterable<Tag> tags = CollectionUtil.newArrayList(
                    Tag.of(DYNAMIC_THREAD_POOL_ID_TAG, threadPoolId),
                    Tag.of(APPLICATION_NAME_TAG, ApplicationProperties.getApplicationName())
            );

            ThreadPoolRuntimeInfo registerRuntimeInfo = BeanUtil.toBean(runtimeInfo, ThreadPoolRuntimeInfo.class);
            micrometerMonitorCache.put(threadPoolId, registerRuntimeInfo);

            Metrics.gauge(metricName("core.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getCorePoolSize);
            Metrics.gauge(metricName("maximum.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getMaximumPoolSize);
            Metrics.gauge(metricName("current.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getCurrentPoolSize);
            Metrics.gauge(metricName("largest.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getLargestPoolSize);
            Metrics.gauge(metricName("active.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getActivePoolSize);
            Metrics.gauge(metricName("queue.size"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueSize);
            Metrics.gauge(metricName("queue.capacity"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueCapacity);
            Metrics.gauge(metricName("queue.remaining.capacity"), tags, registerRuntimeInfo, ThreadPoolRuntimeInfo::getWorkQueueRemainingCapacity);

            DeltaWrapper completedTaskDelta = new DeltaWrapper();
            completedTaskDeltaMap.put(threadPoolId, completedTaskDelta);
            Metrics.gauge(metricName("completed.task.count"), tags, completedTaskDelta, DeltaWrapper::getDelta);

            DeltaWrapper rejectCountDelta = new DeltaWrapper();
            rejectCountDeltaMap.put(threadPoolId, rejectCountDelta);
            Metrics.gauge(metricName("reject.count"), tags, rejectCountDelta, DeltaWrapper::getDelta);
        } else {
            BeanUtil.copyProperties(runtimeInfo, existingRuntimeInfo);
        }

        completedTaskDeltaMap.get(threadPoolId).update(runtimeInfo.getCompletedTaskCount());
        rejectCountDeltaMap.get(threadPoolId).update(runtimeInfo.getRejectCount());
    }

    /** Builds a fully-qualified metric name. */
    private String metricName(String name) {
        return String.join(".", METRIC_NAME_PREFIX, name);
    }

    /**
     * Collects a point-in-time snapshot of the pool's state.
     */
    @SneakyThrows
    private ThreadPoolRuntimeInfo buildThreadPoolRuntimeInfo(BeehiveExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        BeehiveExecutorProperties properties = holder.getExecutorProperties();

        BlockingQueue<Runnable> queue = executor.getQueue();
        int size = queue.size();
        int remainingCapacity = queue.remainingCapacity();

        long rejectCount = -1L;
        if (executor instanceof BeehiveExecutor) {
            rejectCount = ((BeehiveExecutor) executor).getRejectCount().get();
        }

        return ThreadPoolRuntimeInfo.builder()
                .threadPoolId(holder.getThreadPoolId())
                .corePoolSize(executor.getCorePoolSize())
                .maximumPoolSize(executor.getMaximumPoolSize())
                .activePoolSize(executor.getActiveCount())
                .currentPoolSize(executor.getPoolSize())
                .completedTaskCount(executor.getCompletedTaskCount())
                .largestPoolSize(executor.getLargestPoolSize())
                .workQueueName(queue.getClass().getSimpleName())
                .workQueueSize(size)
                .workQueueCapacity(size + remainingCapacity)
                .workQueueRemainingCapacity(remainingCapacity)
                .rejectedHandlerName(executor.getRejectedExecutionHandler().toString())
                .rejectCount(rejectCount)
                .build();
    }
}
