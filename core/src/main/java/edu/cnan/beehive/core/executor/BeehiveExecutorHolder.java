package edu.cnan.beehive.core.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An aggregate that binds a {@link BeehiveExecutor} to its
 * identifier and configuration properties.
 *
 * @author cnan
 */
@Data
@AllArgsConstructor
public class BeehiveExecutorHolder {
    /**
     * The unique identifier for the thread pool.
     */
    private String threadPoolId;

    /**
     * The executor instance.
     */
    private BeehiveExecutor executor;

    /**
     * Configuration properties for the {@code executor}.
     */
    private BeehiveExecutorProperties executorProperties;
}
