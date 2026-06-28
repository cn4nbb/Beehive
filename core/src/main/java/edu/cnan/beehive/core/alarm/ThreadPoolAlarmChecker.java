package edu.cnan.beehive.core.alarm;

import cn.hutool.core.date.DateUtil;
import edu.cnan.beehive.core.config.ApplicationProperties;
import edu.cnan.beehive.core.executor.BeehiveExecutor;
import edu.cnan.beehive.core.executor.BeehiveExecutorHolder;
import edu.cnan.beehive.core.executor.BeehiveExecutorProperties;
import edu.cnan.beehive.core.executor.BeehiveExecutorRegistry;
import edu.cnan.beehive.core.notification.dto.ThreadPoolAlarmNotifyDTO;
import edu.cnan.beehive.core.notification.service.NotifyDispatcher;
import edu.cnan.beehive.core.toolkit.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Periodic health checker for all registered dynamic thread pools.
 *
 * <p>Runs every 5 seconds on a dedicated single-thread scheduler.
 * For each pool with alarm enabled, checks three dimensions:
 * <ul>
 *   <li><b>Queue usage</b> — {@code queue.size / queue.capacity ≥ threshold}</li>
 *   <li><b>Active-thread ratio</b> — {@code activeCount / maximumPoolSize ≥ threshold}</li>
 *   <li><b>Reject-count increase</b> — any increment since last check</li>
 * </ul>
 *
 * <p>Lifecycle:
 * {@link #start()} begins the schedule;
 * {@link #stop()} shuts down the scheduler.
 *
 * @author cnan
 */
@Slf4j
@RequiredArgsConstructor
public class ThreadPoolAlarmChecker {
    /** The notification dispatcher (facade over platform services). */
    private final NotifyDispatcher notifyDispatcher;

    /** Single-thread scheduler for periodic checks. */
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
            1,
            ThreadFactoryBuilder.builder()
                    .namePrefix("scheduler_thread-pool_alarm_checker")
                    .build()
    );

    /** Tracks the last known reject count per pool ({@code threadPoolId → count}). */
    private final Map<String ,Long> lastRejectCountMap = new ConcurrentHashMap<>();

    /** Starts the periodic alarm check (every 5 seconds). */
    public void start() {
        scheduler.scheduleWithFixedDelay(this::checkAlarm, 0, 5, TimeUnit.SECONDS);
    }

    /** Shuts down the scheduler. */
    public void stop() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }

    /** Iterates all registered pools and runs enabled checks. */
    private void checkAlarm() {
        Collection<BeehiveExecutorHolder> holders = BeehiveExecutorRegistry.getAllHolders();
        for (BeehiveExecutorHolder holder : holders) {
            if (holder.getExecutorProperties().getAlarm().getEnable()) {
                checkQueueUsage(holder);
                checkActiveRate(holder);
                checkRejectCount(holder);
            }
        }
    }

    private void checkQueueUsage(BeehiveExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        BeehiveExecutorProperties properties = holder.getExecutorProperties();

        BlockingQueue<Runnable> queue = executor.getQueue();
        int queueSize = queue.size();
        int capacity = queueSize + queue.remainingCapacity();

        if (capacity == 0) {
            return;
        }

        int usageRate = (int) Math.round((queueSize * 100.0) / capacity);
        int threshold = properties.getAlarm().getQueueThreshold();

        if (usageRate >= threshold) {
            sendAlarmMessage("Capacity", holder);
        }
    }

    private void checkActiveRate(BeehiveExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        BeehiveExecutorProperties properties = holder.getExecutorProperties();

        int activeCount = executor.getActiveCount();
        int maximumPoolSize = executor.getMaximumPoolSize();

        if (maximumPoolSize == 0) {
            return;
        }

        int activeRate = (int) Math.round((activeCount * 100.0) / maximumPoolSize);
        int threshold = properties.getAlarm().getActiveThreshold();

        if (activeRate >= threshold) {
            sendAlarmMessage("Activity", holder);
        }
    }

    private void checkRejectCount(BeehiveExecutorHolder holder) {
        ThreadPoolExecutor executor = holder.getExecutor();
        String threadPoolId = holder.getThreadPoolId();

        if (!(executor instanceof BeehiveExecutor)) {
            return;
        }

        BeehiveExecutor beehiveExecutor = (BeehiveExecutor) executor;
        long currentRejectCount = beehiveExecutor.getRejectCount().get();
        long lastRejectCount = lastRejectCountMap.getOrDefault(threadPoolId, 0L);

        if (currentRejectCount > lastRejectCount) {
            sendAlarmMessage("Reject", holder);
            lastRejectCountMap.put(threadPoolId, currentRejectCount);
        }
    }

    /**
     * Builds and dispatches an alarm.
     *
     * <p>The supplier is used to defer collection of 15+ runtime
     * metrics until after rate limiting.  See
     * {@link ThreadPoolAlarmNotifyDTO#resolve()}.
     */
    private void sendAlarmMessage(String alarmType, BeehiveExecutorHolder holder) {
        BeehiveExecutorProperties properties = holder.getExecutorProperties();
        String threadPoolId = holder.getThreadPoolId();

        ThreadPoolAlarmNotifyDTO alarm = ThreadPoolAlarmNotifyDTO.builder()
                .alarmType(alarmType)
                .threadPoolId(threadPoolId)
                .interval(properties.getNotify().getInterval())
                .build();

        alarm.setSupplier(() -> {
            try {
                alarm.setIdentify(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException ex) {
                log.warn("Error in obtaining HostAddress", ex);
            }

            ThreadPoolExecutor executor = holder.getExecutor();
            BlockingQueue<Runnable> queue = executor.getQueue();

            int size = queue.size();
            int capacity = queue.remainingCapacity() + size;
            long rejectCount = (executor instanceof BeehiveExecutor) ?
                    ((BeehiveExecutor) executor).getRejectCount().get() :
                    -1L;


            alarm.setCorePoolSize(executor.getCorePoolSize())
                    .setMaximumPoolSize(executor.getMaximumPoolSize())
                    .setActivePoolSize(executor.getActiveCount())
                    .setCurrentPoolSize(executor.getPoolSize())
                    .setCompletedTaskCount(executor.getCompletedTaskCount())
                    .setLargestPoolSize(executor.getLargestPoolSize())
                    .setWorkQueueName(queue.getClass().getSimpleName())
                    .setWorkQueueSize(size)
                    .setWorkQueueRemainingCapacity(capacity - size)
                    .setWorkQueueCapacity(capacity)
                    .setRejectedHandlerName(executor.getRejectedExecutionHandler().toString())
                    .setRejectCount(rejectCount)
                    .setCurrentTime(DateUtil.now())
                    .setApplicationName(ApplicationProperties.getApplicationName())
                    .setActiveProfile(ApplicationProperties.getActiveProfile())
                    .setReceives(properties.getNotify().getReceives());

            return alarm;
        });

        notifyDispatcher.sendAlarmMessage(alarm);
    }
}
