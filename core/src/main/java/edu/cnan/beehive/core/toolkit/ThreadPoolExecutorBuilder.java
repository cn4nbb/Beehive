package edu.cnan.beehive.core.toolkit;

import cn.hutool.core.lang.Assert;
import edu.cnan.beehive.core.executor.BeehiveExecutor;
import edu.cnan.beehive.core.executor.support.BlockingQueueTypeEnum;

import java.util.concurrent.*;

/**
 * Builder for {@link ThreadPoolExecutor} and
 * {@link BeehiveExecutor} instances.
 *
 * <p>When {@link #dynamicPool()} is invoked, {@link #build()}
 * returns a {@code BeehiveExecutor} with rejection counting and
 * graceful shutdown; otherwise a standard {@code ThreadPoolExecutor}.
 *
 * @author cnan
 */
public class ThreadPoolExecutorBuilder {

    /** Unique thread-pool id; used only in dynamic mode. */
    private String threadPoolId;

    /** Core pool size; defaults to {@link Runtime#availableProcessors()}. */
    private int corePoolSize = Runtime.getRuntime().availableProcessors();

    /** Maximum pool size; defaults to 1.5 × core. */
    private int maximumPoolSize = corePoolSize + (corePoolSize >> 1);

    /** Keep-alive time in seconds; defaults to 30000. */
    private long keepAliveTime = 30000L;

    /** Work queue type; defaults to {@code LinkedBlockingQueue}. */
    private BlockingQueueTypeEnum workQueueType = BlockingQueueTypeEnum.LINKED_BLOCKING_QUEUE;

    /** Work queue capacity; defaults to 4096. */
    private int workQueueCapacity = 4096;

    /** Rejection policy; defaults to {@code AbortPolicy}. */
    private RejectedExecutionHandler rejectedHandler = new ThreadPoolExecutor.AbortPolicy();

    /** Thread factory; must be set before {@link #build()}. */
    private ThreadFactory threadFactory;

    /** Whether core threads may time out; defaults to {@code false}. */
    private boolean allowCoreThreadTimeOut = false;

    /** Whether to build a {@link BeehiveExecutor} rather than a plain pool. */
    private boolean dynamicPool = false;

    /** Shutdown wait timeout in milliseconds; used only in dynamic mode. */
    private long awaitTerminationMillis = 0L;

    public static ThreadPoolExecutorBuilder builder() {
        return new ThreadPoolExecutorBuilder();
    }

    public ThreadPoolExecutorBuilder threadPoolId(String threadPoolId) {
        this.threadPoolId = threadPoolId;
        return this;
    }

    public ThreadPoolExecutorBuilder dynamicPool() {
        this.dynamicPool = true;
        return this;
    }

    public ThreadPoolExecutorBuilder corePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public ThreadPoolExecutorBuilder maximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    public ThreadPoolExecutorBuilder workQueueType(BlockingQueueTypeEnum workQueueType) {
        this.workQueueType = workQueueType;
        return this;
    }

    public ThreadPoolExecutorBuilder workQueueCapacity(int workQueueCapacity) {
        this.workQueueCapacity = workQueueCapacity;
        return this;
    }

    public ThreadPoolExecutorBuilder keepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    public ThreadPoolExecutorBuilder rejectedExecutionHandler(RejectedExecutionHandler rejectedHandler) {
        this.rejectedHandler = rejectedHandler;
        return this;
    }

    public ThreadPoolExecutorBuilder allowCoreThreadTimeOut() {
        this.allowCoreThreadTimeOut = true;
        return this;
    }

    public ThreadPoolExecutorBuilder awaitTerminationMillis(long awaitTerminationMillis) {
        this.awaitTerminationMillis = awaitTerminationMillis;
        return this;
    }

    public ThreadPoolExecutorBuilder threadFactory(String namePrefix) {
        this.threadFactory = ThreadFactoryBuilder
                .builder()
                .namePrefix(namePrefix)
                .build();
        return this;
    }

    public ThreadPoolExecutorBuilder threadFactory(String namePrefix, boolean daemon) {
        this.threadFactory = ThreadFactoryBuilder
                .builder()
                .namePrefix(namePrefix)
                .daemon(daemon)
                .build();
        return this;
    }

    public ThreadPoolExecutorBuilder threadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * Builds the thread-pool instance.
     *
     * @return a {@link BeehiveExecutor} if {@link #dynamicPool()}
     *         was called, otherwise a plain {@link ThreadPoolExecutor}
     * @throws NullPointerException if {@code threadFactory} has not been set
     */
    public ThreadPoolExecutor build() {
        BlockingQueue<Runnable> blockingQueue = BlockingQueueTypeEnum.createBlockingQueue(workQueueType.getName(), workQueueCapacity);
        RejectedExecutionHandler rejectedExecutionHandler = this.rejectedHandler;
        Assert.notNull(threadFactory, "The thread factory cannot be null.");

        ThreadPoolExecutor threadPoolExecutor;
        if (dynamicPool) {
            threadPoolExecutor = new BeehiveExecutor(
                    threadPoolId,
                    corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    TimeUnit.SECONDS,
                    blockingQueue,
                    threadFactory,
                    rejectedExecutionHandler,
                    awaitTerminationMillis
            );
        } else {
            threadPoolExecutor = new ThreadPoolExecutor(
                    corePoolSize,
                    maximumPoolSize,
                    keepAliveTime,
                    TimeUnit.SECONDS,
                    blockingQueue,
                    threadFactory,
                    rejectedExecutionHandler
            );
        }

        threadPoolExecutor.allowCoreThreadTimeOut(allowCoreThreadTimeOut);
        return threadPoolExecutor;
    }
}
