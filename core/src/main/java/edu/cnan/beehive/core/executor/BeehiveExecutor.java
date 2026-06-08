package edu.cnan.beehive.core.executor;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An {@link ThreadPoolExecutor} that provides enhanced monitoring,
 * diagnostic, and graceful-shutdown capabilities on top of the
 * standard JDK thread pool.
 *
 * <p>In addition to the standard {@code ThreadPoolExecutor}
 * behavior, this implementation provides:
 *
 * <ul>
 *   <li>A unique {@linkplain #getThreadPoolId() thread-pool id} for
 *       runtime identification and dynamic configuration changes.</li>
 *   <li>A {@linkplain #getRejectCount() rejection counter} that tracks
 *       how many tasks have been rejected.  The counter is incremented
 *       transparently by a wrapper around the configured
 *       {@link RejectedExecutionHandler}, so no change to the
 *       original rejection policy is required.</li>
 *   <li>A graceful {@link #shutdown()} that, when configured with a
 *       positive {@code awaitTerminationMillis}, blocks for up to the
 *       specified timeout waiting for all tasks to complete, logging
 *       the outcome for diagnostic purposes.</li>
 * </ul>
 *
 * <p>Instances of this class are typically created through
 * {@code ThreadPoolExecutorBuilder} rather than directly invoked,
 * so that queue type, rejection policy, and thread factory are
 * assembled from external configuration.
 *
 * @author cnan
 */
@Slf4j
public class BeehiveExecutor extends ThreadPoolExecutor {
    /**
     * The unique identifier for this thread pool.
     */
    @Getter
    private final String threadPoolId;

    /**
     * The total number of tasks that have been rejected by this
     * executor.
     */
    @Getter
    private final AtomicLong rejectCount = new AtomicLong();

    /**
     * The maximum time to wait (in milliseconds) for task
     * completion during {@link #shutdown()}.
     * A non-positive value means no wait.
     */
    private long awaitTerminationMillis;

    /**
     * Creates a new {@code BeehiveExecutor} with the given initial
     * parameters.
     *
     * @param threadPoolId the unique identifier for this thread pool;
     *        must not be {@code null}
     * @param corePoolSize the number of threads to keep in the pool,
     *        even if they are idle, unless
     *        {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow
     *        in the pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle
     *        threads will wait for new tasks before terminating
     * @param unit the time unit for the {@code keepAliveTime}
     *        argument; must not be {@code null}
     * @param workQueue the queue to use for holding tasks before they
     *        are executed.  This queue will hold only the
     *        {@code Runnable} tasks submitted by the
     *        {@code execute} method; must not be {@code null}
     * @param threadFactory the factory to use when the executor
     *        creates a new thread; must not be {@code null}
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are
     *        reached; must not be {@code null}
     * @param awaitTerminationMillis the maximum time to wait (in
     *        milliseconds) for task completion during
     *        {@link #shutdown()}; non-positive means no wait
     * @throws IllegalArgumentException if one of the following
     *         holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code threadPoolId},
     *         {@code unit}, {@code workQueue},
     *         {@code threadFactory}, or {@code handler} is
     *         {@code null}
     */
    public BeehiveExecutor(
            @NonNull String threadPoolId,
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            @NonNull TimeUnit unit,
            @NonNull BlockingQueue<Runnable> workQueue,
            @NonNull ThreadFactory threadFactory,
            @NonNull RejectedExecutionHandler handler,
            long awaitTerminationMillis) {

        super(corePoolSize, maximumPoolSize, keepAliveTime, unit,
                workQueue, threadFactory, handler);

        setRejectedExecutionHandler(handler);

        this.threadPoolId = threadPoolId;
        this.awaitTerminationMillis = awaitTerminationMillis;
    }

    /**
     * Sets the handler that is invoked when this executor cannot
     * accept a task.
     *
     * @implSpec
     * The supplied {@code handler} is wrapped so that
     * {@link #getRejectCount() rejectCount} is atomically
     * incremented before delegation.  The wrapper's
     * {@link Object#toString() toString()} method returns the
     * simple class name of the original handler for display
     * purposes.
     *
     * @param handler the handler to wrap; must not be {@code null}
     * @throws NullPointerException if {@code handler} is {@code null}
     * @see RejectedExecutionHandler
     * @see java.util.concurrent.ThreadPoolExecutor.AbortPolicy
     * @see java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy
     * @see java.util.concurrent.ThreadPoolExecutor.DiscardPolicy
     * @see java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy
     */
    @Override
    public void setRejectedExecutionHandler(
            RejectedExecutionHandler handler) {

        RejectedExecutionHandler handlerWrapper =
                new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                rejectCount.incrementAndGet();
                handler.rejectedExecution(r, executor);
            }

            @Override
            public String toString() {
                return handler.getClass().getSimpleName();
            }
        };

        super.setRejectedExecutionHandler(handlerWrapper);
    }

    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted.
     * Invocation has no additional effect if already shut down.
     *
     * <p>This method does not wait for all tasks to complete
     * execution.  Use {@link #awaitTermination awaitTermination}
     * to do that, or specify a non-positive
     * {@code awaitTerminationMillis} in the constructor to return
     * immediately.
     *
     * @implSpec
     * If {@code awaitTerminationMillis > 0}, this method blocks
     * until all tasks have completed execution, or the timeout
     * occurs, or the current thread is interrupted, whichever
     * happens first.  The thread-pool id {@code threadPoolId} is
     * recorded in the log for diagnostic purposes.
     *
     * @throws SecurityException
     *         if a security manager exists and shutting down this
     *         ExecutorService may manipulate threads that the
     *         caller is not permitted to modify because it does
     *         not hold
     *         {@link RuntimePermission}{@code ("modifyThread")},
     *         or the security manager's {@code checkAccess} method
     *         denies access
     */
    @Override
    public void shutdown() {
        if (isShutdown()) {
            return;
        }

        super.shutdown();

        if (awaitTerminationMillis <= 0) {
            return;
        }

        log.info("Before shutting down ExecutorService {}",
                threadPoolId);
        try {
            boolean isTerminated = this.awaitTermination(
                    awaitTerminationMillis, TimeUnit.MILLISECONDS);
            if (isTerminated) {
                log.info("ExecutorService {} has been shutdown.",
                        threadPoolId);
            } else {
                log.warn("Timed out while waiting for executor {} "
                        + "to terminate.", threadPoolId);
            }
        } catch (InterruptedException ex) {
            log.warn("Interrupted while waiting for executor {} "
                    + "to terminate.", threadPoolId);
            Thread.currentThread().interrupt();
        }
    }
}
