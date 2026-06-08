package edu.cnan.beehive.core.executor;

import edu.cnan.beehive.core.executor.support.AlarmConfig;
import edu.cnan.beehive.core.executor.support.NotifyConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Configuration properties for a {@link BeehiveExecutor}.
 *
 * <p>This class models every tunable parameter of a thread pool as a
 * flat data structure suitable for deserialization from external
 * configuration sources.
 *
 * <p>Typical usage with {@code @Builder}:
 * <pre>{@code
 * BeehiveExecutorProperties props = BeehiveExecutorProperties.builder()
 *         .threadPoolId("order-pool")
 *         .corePoolSize(10)
 *         .maximumPoolSize(20)
 *         .queueCapacity(1024)
 *         .workQueue("LinkedBlockingQueue")
 *         .rejectedHandler("AbortPolicy")
 *         .keepAliveTime(30L)
 *         .build();
 * }</pre>
 *
 * @author cnan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class BeehiveExecutorProperties {
    /**
     * The unique identifier for the thread pool.
     */
    private String threadPoolId;

    /**
     * The core number of threads to keep in the pool, even if idle,
     * unless {@code allowCoreThreadTimeOut} is set.
     */
    private Integer corePoolSize;

    /**
     * The maximum allowed number of threads in the pool.
     */
    private Integer maximumPoolSize;

    /**
     * The capacity of the work queue.
     */
    private Integer queueCapacity;

    /**
     * The type name of the blocking queue.
     */
    private String workQueue;

    /**
     * The type name of the rejection policy.
     */
    private String rejectedHandler;

    /**
     * The thread keep-alive time, in seconds.
     */
    private Long keepAliveTime;

    /**
     * Whether core threads may time out and be terminated after
     * {@code keepAliveTime} seconds of idleness.
     * Defaults to {@code false} in the builder when not explicitly
     * configured.
     */
    private Boolean allowCoreThreadTimeOut;

    /**
     * Notification configuration.
     */
    private NotifyConfig notify;

    /**
     * Alarm threshold configuration.
     */
    private AlarmConfig alarm = new AlarmConfig();
}
