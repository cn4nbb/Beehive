package edu.cnan.beehive.core.executor.support;

import edu.cnan.beehive.core.executor.BeehiveExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Enumeration of {@link RejectedExecutionHandler} policies
 * supported by {@link BeehiveExecutor}.
 *
 * <p>Each constant holds a singleton handler instance of the
 * corresponding JDK built-in policy.  All four standard policies
 * are covered:
 *
 * <table>
 *   <caption>Rejection policies</caption>
 *   <tr><th>Constant</th>               <th>JDK class</th>                <th>Behavior on rejection</th></tr>
 *   <tr><td>{@link #ABORT_POLICY}</td>       <td>{@link ThreadPoolExecutor.AbortPolicy}</td>       <td>Throws {@link java.util.concurrent.RejectedExecutionException}</td></tr>
 *   <tr><td>{@link #CALLER_RUNS_POLICY}</td> <td>{@link ThreadPoolExecutor.CallerRunsPolicy}</td> <td>Executes the task in the caller's thread</td></tr>
 *   <tr><td>{@link #DISCARD_POLICY}</td>     <td>{@link ThreadPoolExecutor.DiscardPolicy}</td>    <td>Silently discards the rejected task</td></tr>
 *   <tr><td>{@link #DISCARD_OLDEST_POLICY}</td><td>{@link ThreadPoolExecutor.DiscardOldestPolicy}</td><td>Discards the oldest queued task, then retries</td></tr>
 * </table>
 *
 * @author cnan
 */

public enum RejectedPolicyTypeEnum {
    /** {@link ThreadPoolExecutor.CallerRunsPolicy} */
    CALLER_RUNS_POLICY("CallerRunsPolicy", new ThreadPoolExecutor.CallerRunsPolicy()),

    /** {@link ThreadPoolExecutor.AbortPolicy} */
    ABORT_POLICY("AbortPolicy", new ThreadPoolExecutor.AbortPolicy()),

    /** {@link ThreadPoolExecutor.DiscardPolicy} */
    DISCARD_POLICY("DiscardPolicy", new ThreadPoolExecutor.DiscardPolicy()),

    /** {@link ThreadPoolExecutor.DiscardOldestPolicy} */
    DISCARD_OLDEST_POLICY("DiscardOldestPolicy", new ThreadPoolExecutor.DiscardOldestPolicy());

    /** The string name used in configuration files. */
    private String name;

    /** The shared singleton handler instance. */
    private RejectedExecutionHandler rejectedHandler;

    /** Lookup map keyed by {@link #name}. */
    private static final Map<String, RejectedPolicyTypeEnum> NAME_TO_ENUM_MAP;

    RejectedPolicyTypeEnum(String name, RejectedExecutionHandler rejectedHandler) {
        this.name = name;
        this.rejectedHandler = rejectedHandler;
    }

    static {
        final RejectedPolicyTypeEnum[] values = RejectedPolicyTypeEnum.values();
        NAME_TO_ENUM_MAP = new HashMap<>(values.length);
        for (RejectedPolicyTypeEnum value : values) {
            NAME_TO_ENUM_MAP.put(value.name, value);
        }
    }

    /**
     * Returns the handler instance for the given policy name.
     *
     * @param policyName the policy name (e.g. {@code "AbortPolicy"})
     * @return the shared handler instance
     * @throws IllegalArgumentException if the name does not match any
     *         declared constant
     */
    public static RejectedExecutionHandler createPolicy(String policyName) {
        RejectedPolicyTypeEnum typeEnum = NAME_TO_ENUM_MAP.get(policyName);
        if (typeEnum != null) {
            return typeEnum.rejectedHandler;
        }

        throw new IllegalArgumentException("No mathcing type of rejected execution policy was found: " + policyName);
    }
}
