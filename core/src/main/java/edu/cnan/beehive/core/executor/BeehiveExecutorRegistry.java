package edu.cnan.beehive.core.executor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry for {@link BeehiveExecutorHolder} instances.
 *
 * <p>Provides static methods to register, look up, and enumerate
 * managed thread pools.
 *
 * @author cnan
 */
public class BeehiveExecutorRegistry {
    /**
     * The backing map, key is {@code threadPoolId},
     * value is {@code BeehiveExecutorHolder}.
     */
    private static final Map<String, BeehiveExecutorHolder> HOLDER_MAP = new ConcurrentHashMap<>();

    /**
     * Registers a thread pool under the given id.
     *
     * @param threadPoolId the unique identifier
     * @param executor     the executor instance
     * @param properties   the configuration properties
     */
    public static void putHolder(String threadPoolId, BeehiveExecutor executor, BeehiveExecutorProperties properties) {
        BeehiveExecutorHolder executorHolder = new BeehiveExecutorHolder(threadPoolId, executor, properties);
        HOLDER_MAP.put(threadPoolId, executorHolder);
    }

    /**
     * Returns the holder for the given id, or {@code null} if
     * no pool is registered under that id.
     *
     * @param threadPoolId the unique identifier
     * @return the holder, or {@code null}
     */
    public static BeehiveExecutorHolder getHolder(String threadPoolId) {
        return HOLDER_MAP.get(threadPoolId);
    }

    /**
     * Returns a snapshot view of all registered holders.
     * Modifications to the returned collection do not affect
     * the registry.
     *
     * @return all holders
     */
    public static Collection<BeehiveExecutorHolder> getAllHolders() {
        return HOLDER_MAP.values();
    }
}
