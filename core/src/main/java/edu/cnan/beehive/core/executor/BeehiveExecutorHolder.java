package edu.cnan.beehive.core.executor;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * An aggregate that binds a {@link ThreadPoolExecutor} to its
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
    private ThreadPoolExecutor executor;

    /**
     * Configuration properties for the {@code executor}.
     */
    private BeehiveExecutorProperties executorProperties;
}
