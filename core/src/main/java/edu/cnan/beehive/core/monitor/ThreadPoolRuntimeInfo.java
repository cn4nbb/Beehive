package edu.cnan.beehive.core.monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Snapshot of a thread pool's runtime state, collected by
 * {@link ThreadPoolMonitor} on each cycle.
 *
 * @author cnan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadPoolRuntimeInfo {
    private String threadPoolId;

    private Integer corePoolSize;

    private Integer maximumPoolSize;

    private Integer currentPoolSize;

    private Integer activePoolSize;

    private Integer largestPoolSize;

    private Long completedTaskCount;

    private String workQueueName;

    private Integer workQueueCapacity;

    private Integer workQueueSize;

    private Integer workQueueRemainingCapacity;

    private String rejectedHandlerName;

    private Long rejectCount;
}
