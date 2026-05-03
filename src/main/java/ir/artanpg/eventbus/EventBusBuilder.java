package ir.artanpg.eventbus;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import ir.artanpg.eventbus.subscriber.DefaultSubscriberRegistry;
import ir.artanpg.eventbus.subscriber.SubscriberRegistry;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Fluent builder for creating {@link EventBus} instances with rich
 * configuration options.
 *
 * <p>This builder provides a clean and flexible way to configure different
 * aspects of the EventBus including metrics, executor strategy, and subscriber
 * registry.
 *
 * @author Mohammad Yazdian
 */
public final class EventBusBuilder {

    private MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private SubscriberRegistry subscriberRegistry = new DefaultSubscriberRegistry();
    private ExecutorService executorService;
    private ExecutorType executorType = ExecutorType.SERIAL;

    /**
     * Enumeration of supported executor strategies for event processing.
     */
    public enum ExecutorType {
        SERIAL,           // Single thread - default (sequential processing)
        PARALLEL,         // Fixed thread pool
        VIRTUAL,          // Java 21+ Virtual Threads
        CUSTOM            // User provided ExecutorService
    }

    private EventBusBuilder() {
    }

    /**
     * Creates a new {@link EventBusBuilder} instance.
     *
     * @return a new builder instance
     */
    public static EventBusBuilder create() {
        return new EventBusBuilder();
    }

    /**
     * Sets the {@link MeterRegistry} to be used for collecting EventBus
     * metrics.
     *
     * @param meterRegistry the metrics registry
     * @return this builder instance for method chaining
     * @throws NullPointerException if meterRegistry is {@code null}
     */
    public EventBusBuilder meterRegistry(MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry);
        return this;
    }

    /**
     * Sets a custom {@link SubscriberRegistry} for managing event listeners.
     *
     * @param subscriberRegistry the subscriber registry implementation
     * @return this builder instance for method chaining
     * @throws NullPointerException if subscriberRegistry is {@code null}
     */
    public EventBusBuilder subscriberRegistry(SubscriberRegistry subscriberRegistry) {
        this.subscriberRegistry = Objects.requireNonNull(subscriberRegistry);
        return this;
    }

    /**
     * Configures the EventBus to use a single-threaded serial executor. This
     * is the default behavior and guarantees that events are processed
     * sequentially.
     *
     * @return this builder instance for method chaining
     */
    public EventBusBuilder serialExecutor() {
        this.executorType = ExecutorType.SERIAL;
        this.executorService = null;
        return this;
    }

    /**
     * Configures the EventBus to use a fixed thread pool for parallel event
     * processing.
     *
     * @param threadCount number of threads in the pool
     * @return this builder instance for method chaining
     * @throws IllegalArgumentException if threadCount is less than 1
     */
    public EventBusBuilder parallelExecutor(int threadCount) {
        this.executorType = ExecutorType.PARALLEL;
        this.executorService = Executors.newFixedThreadPool(threadCount, namedThreadFactory());
        return this;
    }

    /**
     * Configures the EventBus to use virtual threads. This provides excellent
     * scalability with very low overhead.
     *
     * @return this builder instance for method chaining
     */
    public EventBusBuilder virtualThreadsExecutor() {
        this.executorType = ExecutorType.VIRTUAL;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        return this;
    }

    /**
     * Sets a custom {@link ExecutorService} for event processing.
     *
     * @param executorService the custom executor service
     * @return this builder instance for method chaining
     * @throws NullPointerException if executorService is {@code null}
     */
    public EventBusBuilder executor(ExecutorService executorService) {
        this.executorType = ExecutorType.CUSTOM;
        this.executorService = Objects.requireNonNull(executorService);
        return this;
    }

    public EventBus build() {
        ExecutorService finalExecutor = switch (executorType) {
            case SERIAL -> createSerialExecutor();
            case PARALLEL, CUSTOM, VIRTUAL -> executorService;
        };

        return new DefaultEventBus(meterRegistry, finalExecutor, subscriberRegistry);
    }

    private ExecutorService createSerialExecutor() {
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "EventBus-Serial-Thread");
            t.setDaemon(true);
            return t;
        };
        return Executors.newSingleThreadExecutor(factory);
    }

    private static ThreadFactory namedThreadFactory() {
        return r -> {
            Thread t = new Thread(r);
            t.setName("EventBus-Parallel" + "-" + System.nanoTime() % 10000);
            t.setDaemon(true);
            return t;
        };
    }
}
