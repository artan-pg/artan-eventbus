package ir.artanpg.eventbus;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import ir.artanpg.eventbus.subscriber.SubscriberRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Abstract base implementation of the {@link EventBus} interface.
 * Provides common infrastructure for event bus implementations including:
 * <ul>
 *   <li>Thread pool management with metrics monitoring</li>
 *   <li>Subscriber registration and management</li>
 *   <li>Graceful shutdown mechanism</li>
 *   <li>Logging and metrics integration</li>
 * </ul>
 *
 * <p>This class uses a single-threaded executor by default to ensure
 * sequential event processing, but can be configured with custom executors for
 * different concurrency models.
 *
 * <p>All executor operations are automatically monitored through Micrometer's
 * {@link MeterRegistry} for performance tracking and observability.
 *
 * <h2>Implementation Notes:</h2>
 * <p>Concrete subclasses must implement the event publishing and subscription
 * methods defined in the {@link EventBus} interface. The protected getter methods
 * provide access to the core components for implementing these operations.
 *
 * @author Mohammad Yazdian
 * @see EventBus
 * @see MeterRegistry
 * @see SubscriberRegistry
 */
public abstract class AbstractEventBus implements EventBus {

    /**
     * Logger instance for this class.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Metrics registry for monitoring executor performance.
     */
    private final MeterRegistry meterRegistry;

    /**
     * Registry for managing event subscribers.
     */
    private final SubscriberRegistry subscriberRegistry;

    /**
     * Executor service for processing events.
     * Monitored through {@link #meterRegistry}.
     */
    private ExecutorService executorService;

    /**
     * Constructs an AbstractEventBus with default single-threaded executor.
     *
     * <p>The executor is configured as a daemon thread and monitored through
     * the provided {@link MeterRegistry}.
     *
     * @param meterRegistry      the metrics registry for monitoring executor performance
     * @param subscriberRegistry the registry for managing event subscribers
     * @throws IllegalArgumentException if {@code meterRegistry} or {@code subscriberRegistry} is {@code null}
     */
    protected AbstractEventBus(MeterRegistry meterRegistry, SubscriberRegistry subscriberRegistry) {
        if (meterRegistry == null) throw new IllegalArgumentException("the meterRegistry cannot be null");
        if (subscriberRegistry == null) throw new IllegalArgumentException("the subscriberRegistry cannot be null");

        this.meterRegistry = meterRegistry;
        this.subscriberRegistry = subscriberRegistry;

        ExecutorService fixedThreadPool =
                Executors.newSingleThreadExecutor(threadFactory -> {
                            Thread thread = new Thread(threadFactory, "EventBus-Serial-Thread");
                            thread.setDaemon(true);
                            return thread;
                        }
                );

        this.executorService = ExecutorServiceMetrics.monitor(meterRegistry, fixedThreadPool, "eventbus.executor");
    }

    /**
     * Constructs an AbstractEventBus with a custom executor service.
     *
     * <p>Use this constructor when specific thread pool configuration is
     * required.
     *
     * @param meterRegistry      the metrics registry for monitoring executor performance
     * @param executorService    the executor service to use for event processing
     * @param subscriberRegistry the registry for managing event subscribers
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    protected AbstractEventBus(MeterRegistry meterRegistry,
                               ExecutorService executorService,
                               SubscriberRegistry subscriberRegistry) {
        if (meterRegistry == null) throw new IllegalArgumentException("the meterRegistry cannot be null");
        if (executorService == null) throw new IllegalArgumentException("the executorService cannot be null");
        if (subscriberRegistry == null) throw new IllegalArgumentException("the subscriberRegistry cannot be null");

        this.meterRegistry = meterRegistry;
        this.executorService = ExecutorServiceMetrics.monitor(meterRegistry, executorService, "eventbus.executor");
        this.subscriberRegistry = subscriberRegistry;
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns the logger instance for this class.
     *
     * <p>Subclasses can use this logger for consistent logging.
     *
     * @return the {@link Logger} instance for this class
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * Returns the metrics registry used for monitoring.
     *
     * @return the {@link MeterRegistry} instance
     */
    protected MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    /**
     * Returns the subscriber registry managing event listeners.
     *
     * @return the {@link SubscriberRegistry} instance
     */
    protected SubscriberRegistry getSubscriberRegistry() {
        return subscriberRegistry;
    }

    /**
     * Returns the executor service used for event processing.
     *
     * <p><b>Warning:</b> Direct manipulation of the executor service may
     * interfere with the event bus's normal operation. Use with caution.
     *
     * @return the {@link ExecutorService} instance
     */
    protected ExecutorService getExecutorService() {
        return executorService;
    }
}
