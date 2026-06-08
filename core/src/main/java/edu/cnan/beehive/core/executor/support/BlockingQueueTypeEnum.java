package edu.cnan.beehive.core.executor.support;

import edu.cnan.beehive.core.executor.BeehiveExecutor;
import edu.cnan.beehive.core.executor.BeehiveExecutorProperties;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Enumeration of supported blocking queue types for
 * {@link BeehiveExecutor}.
 *
 * <p>Each constant is a factory that produces the corresponding
 * {@link BlockingQueue} implementation.  Constants that ignore
 * the capacity parameter (e.g. {@link #SYNCHRONOUS_QUEUE},
 * {@link #LINKED_TRANSFER_QUEUE}) return the same instance
 * regardless of the argument.
 *
 * <p>The canonical entry point is
 * {@link #createBlockingQueue(String, Integer)}, which resolves
 * a string name — typically from
 * {@link BeehiveExecutorProperties#getWorkQueue()} — to a
 * concrete queue instance.
 *
 * @author cnan
 */
public enum BlockingQueueTypeEnum {
    /** {@link ArrayBlockingQueue} */
    ARRAY_BLOCKING_QUEUE("ArrayBlockingQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new ArrayBlockingQueue<>(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new ArrayBlockingQueue<>(DEFAULT_CAPACITY);
        }
    },

    /** {@link LinkedBlockingQueue} */
    LINKED_BLOCKING_QUEUE("LinkedBlockingQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new LinkedBlockingQueue<>(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
        }
    },

    /** {@link LinkedBlockingDeque} */
    LINKED_BLOCKING_DEQUE("LinkedBlockingDeque") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new LinkedBlockingDeque<>(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new LinkedBlockingDeque<>(DEFAULT_CAPACITY);
        }
    },

    /** {@link SynchronousQueue} */
    SYNCHRONOUS_QUEUE("SynchronousQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new SynchronousQueue<>();
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new SynchronousQueue<>();
        }
    },

    /** {@link LinkedTransferQueue} */
    LINKED_TRANSFER_QUEUE("LinkedTransferQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new LinkedTransferQueue<>();
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new LinkedTransferQueue<>();
        }
    },

    /** {@link  PriorityBlockingQueue} */
    PRIORITY_BLOCKING_QUEUE("PriorityBlockingQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new PriorityBlockingQueue<>(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new PriorityBlockingQueue<>();
        }
    },

    /** {@link ResizableCapacityLinkedBlockingQueue} */
    RESIZABLE_CAPACITY_LINKED_BLOCKING_QUEUE("ResizableCapacityLinkedBlockingQueue") {
        @Override
        <T> BlockingQueue<T> of(Integer capacity) {
            return new ResizableCapacityLinkedBlockingQueue(capacity);
        }

        @Override
        <T> BlockingQueue<T> of() {
            return new ResizableCapacityLinkedBlockingQueue<>();
        }
    };

    /** The string name used in configuration files. */
    @Getter
    private final String name;

    private static final int DEFAULT_CAPACITY = 4096;

    private static final Map<String, BlockingQueueTypeEnum> NAME_TO_MAP;

    BlockingQueueTypeEnum(String name) {
        this.name = name;
    }

    /**
     * Creates a queue with the given capacity.
     * @param capacity the capacity, never {@code null}
     */
    abstract <T> BlockingQueue<T> of (Integer capacity);

    /**
     * Creates a queue with the default capacity ({@value #DEFAULT_CAPACITY}).
     */
    abstract <T> BlockingQueue<T> of ();

    static {
        final BlockingQueueTypeEnum[] values = BlockingQueueTypeEnum.values();
        NAME_TO_MAP = new HashMap<>(values.length);
        for (BlockingQueueTypeEnum value : values) {
            NAME_TO_MAP.put(value.name, value);
        }
    }

    /**
     * Looks up the type by name and creates a queue.
     *
     * @param blockingQueueName the type name (e.g. {@code "LinkedBlockingQueue"})
     * @param capacity          the capacity, may be {@code null} to use default
     * @param <T>               the element type
     * @return a new queue instance, or {@code null} if the name is unrecognized
     */
    private static <T> BlockingQueue<T> of(String blockingQueueName, Integer capacity) {
        final BlockingQueueTypeEnum typeEnum = NAME_TO_MAP.get(blockingQueueName);
        if (typeEnum == null) {
            return null;
        }
        return Objects.isNull(capacity) ? typeEnum.of() : typeEnum.of(capacity);
    }

    /**
     * Looks up the type by name and creates a queue.
     *
     * @param blockingQueueName the type name (e.g. {@code "LinkedBlockingQueue"})
     * @param capacity          the capacity, may be {@code null} to use default
     * @param <T>               the element type
     * @return a new queue instance
     * @throws IllegalArgumentException if the name does not match any
     *         declared constant
     */
    public static <T> BlockingQueue<T> createBlockingQueue(String blockingQueueName, Integer capacity) {
        final BlockingQueue<T> blockingQueue = of(blockingQueueName, capacity);
        if (blockingQueue != null) {
            return blockingQueue;
        }
        throw new IllegalArgumentException("No mathcing type of blocking queue was fount: " + blockingQueueName);
    }
}
