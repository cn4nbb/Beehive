package edu.cnan.beehive.core.notification.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

/**
 * Data carrier for thread-pool runtime alarm notifications.
 *
 * <p>Call {@link #resolve()} to trigger the supplier; if no supplier
 * is set (e.g. after deserialization), the DTO is returned as-is.
 *
 * @author cnan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ThreadPoolAlarmNotifyDTO {

    /**
     * unique thread-pool identifier
     */
    private String threadPoolId;

    /**
     * application name
     */
    private String applicationName;

    /**
     * active Spring profile
     */
    private String activeProfile;

    /**
     * node identifier (host address)
     */
    private String identify;

    /**
     * notification recipients (comma-separated)
     */
    private String receives;

    /**
     * alarm type: Capacity / Activity / Reject
     */
    private String alarmType;

    private Integer corePoolSize;

    private Integer maximumPoolSize;

    private Integer currentPoolSize;

    private Integer activePoolSize;

    private Integer largestPoolSize;

    /**
     * total completed task count
     */
    private Long completedTaskCount;

    private String workQueueName;

    private Integer workQueueCapacity;

    private Integer workQueueSize;

    private Integer workQueueRemainingCapacity;

    private String rejectedHandlerName;

    private Long rejectCount;

    /**
     * alarm timestamp
     */
    private String currentTime;

    /**
     * alarm interval in minutes
     */
    private Integer interval;

    /**
     * Lazy context filler.  Excluded from {@code toString()}
     * and serialization.
     */
    @ToString.Exclude
    private transient Supplier<ThreadPoolAlarmNotifyDTO> supplier;

    /**
     * Resolves the DTO by invoking the supplier if present.
     * @return the filled DTO, or {@code this} if no supplier is set
     */
    public ThreadPoolAlarmNotifyDTO resolve() {
        return supplier != null ? supplier.get() : this;
    }
}
