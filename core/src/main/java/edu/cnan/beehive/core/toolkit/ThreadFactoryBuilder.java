package edu.cnan.beehive.core.toolkit;

import cn.hutool.core.lang.Assert;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Builder for {@link ThreadFactory} instances with customizable
 * thread name, daemon flag, priority, and uncaught-exception handler.
 *
 * <p>Typical usage:
 * <pre>{@code
 * ThreadFactory factory = ThreadFactoryBuilder.builder()
 *         .namePrefix("order-pool-")
 *         .daemon(false)
 *         .build();
 * }</pre>
 *
 * @author cnan
 */
public class ThreadFactoryBuilder {
    /** Fallback factory; defaults to {@link Executors#defaultThreadFactory()}. */
    private ThreadFactory backingThreadFactory;

    /** Required prefix for thread names (e.g. {@code "order-pool-"} → {@code order-pool-0}). */
    private String namePrefix;

    /** Thread priority (1–10); {@code null} means unchanged. */
    private Integer priority;

    /** Daemon flag; {@code null} means unchanged. */
    private Boolean daemon;

    /** Handler for uncaught exceptions; {@code null} means no handler. */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    /** Creates a new builder instance. */
    public static ThreadFactoryBuilder builder() {
        return new ThreadFactoryBuilder();
    }

    public ThreadFactoryBuilder threadFactory(ThreadFactory threadFactory) {
        this.backingThreadFactory = threadFactory;
        return this;
    }

    public ThreadFactoryBuilder namePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
        return this;
    }

    public ThreadFactoryBuilder priority(int priority) {
        if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("The thread priority must be between 1 and 10.");
        }
        this.priority = priority;
        return this;
    }

    public ThreadFactoryBuilder daemon(Boolean daemon) {
        this.daemon = daemon;
        return this;
    }

    public ThreadFactoryBuilder uncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler) {
        this.uncaughtExceptionHandler = handler;
        return this;
    }

    /**
     * Builds the {@link ThreadFactory}.
     *
     * @return a new {@code ThreadFactory} that delegates thread
     *         creation to the backing factory and applies the
     *         configured name, daemon, priority, and exception
     *         handler
     * @throws IllegalArgumentException if {@code namePrefix}
     *         is {@code null} or empty
     */
    public ThreadFactory build() {
        final ThreadFactory factory = (this.backingThreadFactory != null) ? this.backingThreadFactory : Executors.defaultThreadFactory();
        Assert.notEmpty(this.namePrefix, "The thread name prefix cannot be empty or an empty string.");
        final AtomicInteger count = (this.namePrefix != null) ? new AtomicInteger() : null;

        return runnable -> {
            Thread thread = factory.newThread(runnable);

            if (count != null) {
                thread.setName(namePrefix + count.getAndIncrement());
            }

            if (daemon != null) {
                thread.setDaemon(daemon);
            }

            if (priority != null) {
                thread.setPriority(priority);
            }

            if (uncaughtExceptionHandler != null) {
                thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            }

            return thread;
        };
    }
}
