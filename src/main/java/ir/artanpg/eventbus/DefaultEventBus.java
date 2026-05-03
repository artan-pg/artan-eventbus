package ir.artanpg.eventbus;

import io.micrometer.core.instrument.MeterRegistry;
import ir.artanpg.eventbus.metric.EventBusMetricsMonitor;
import ir.artanpg.eventbus.model.Event;
import ir.artanpg.eventbus.model.EventListener;
import ir.artanpg.eventbus.model.EventListenerRecord;
import ir.artanpg.eventbus.subscriber.SubscriberRegistry;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

/**
 * Default implementation of the {@link EventBus} interface.
 * This class provides a production-ready event bus implementation with:
 * <ul>
 *   <li>Topic-based event routing</li>
 *   <li>Comprehensive metrics collection via Micrometer</li>
 *   <li>Thread-safe subscriber management using {@link ConcurrentSkipListSet}</li>
 *   <li>Error handling with configurable exception handlers</li>
 * </ul>
 *
 * <p>Events are published to specific topics, and listeners subscribe to topics
 * they're interested in. The "default" topic is used when no specific topic is
 * provided.
 *
 * <p><b>Thread Safety:</b> This implementation is thread-safe. Subscriber
 * registration, removal, and event publication can be safely called from
 * multiple threads concurrently.
 *
 * <h2>Usage Example:</h2>
 * <pre>
 * {@code
 * // Create event bus
 * MeterRegistry meterRegistry = new SimpleMeterRegistry();
 * SubscriberRegistry registry = new DefaultSubscriberRegistry();
 * EventBus eventBus = new DefaultEventBus(meterRegistry, registry);
 *
 * // Register listener
 * EventListener listener = event -> System.out.println("Received: " + event);
 * eventBus.registerSubscribe("user-events", listener);
 *
 * // Publish event
 * Event userEvent = new UserCreatedEvent("user123");
 * eventBus.publish("user-events", userEvent);
 *
 * // Shutdown when done
 * eventBus.shutdown();
 * }
 * </pre>
 *
 * @author Mohammad Yazdian
 * @see Event
 * @see EventListener
 * @see AbstractEventBus
 * @see EventListenerRecord
 * @see SubscriberRegistry
 */
public class DefaultEventBus extends AbstractEventBus {

    private final EventBusMetricsMonitor eventBusMetricsMonitor;

    /**
     * Constructs a DefaultEventBus with the specified dependencies.
     *
     * @param meterRegistry      the metrics registry for collecting event bus metrics
     * @param subscriberRegistry the registry for managing event subscribers
     * @throws IllegalArgumentException if {@code meterRegistry} or {@code subscriberRegistry} is {@code null}
     */
    public DefaultEventBus(MeterRegistry meterRegistry, SubscriberRegistry subscriberRegistry) {
        super(meterRegistry, subscriberRegistry);

        this.eventBusMetricsMonitor = EventBusMetricsMonitor.of(meterRegistry);
    }

    /**
     * Constructs a DefaultEventBus with a custom executor service.
     *
     * <p>This constructor allows full control over the threading model used for
     * event processing. It is primarily used by {@link EventBusBuilder}.
     *
     * @param meterRegistry      the metrics registry for collecting event bus metrics
     * @param executorService    the executor service to use for asynchronous event processing
     * @param subscriberRegistry the registry for managing event subscribers
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public DefaultEventBus(MeterRegistry meterRegistry,
                           ExecutorService executorService,
                           SubscriberRegistry subscriberRegistry) {
        super(meterRegistry, executorService, subscriberRegistry);
        this.eventBusMetricsMonitor = EventBusMetricsMonitor.of(meterRegistry);
    }

    @Override
    public void publish(Event event) {
        publish("default", event);
    }

    @Override
    public void publish(String topic, Event event) {
        checkNotShutdown();

        // Record publication metrics
        eventBusMetricsMonitor.eventPublishedCounter(topic, event);

        // Get listeners for the topic
        List<EventListenerRecord> eventListenerRecords = getSubscriberRegistry().get(topic);
        if (eventListenerRecords == null || eventListenerRecords.isEmpty()) {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("No listeners are registered for event {}", event.getType().getSimpleName());
            }
            return;
        }

        // Process event asynchronously
        try {
            getExecutorService().execute(() -> {
                for (EventListenerRecord eventListenerRecord : eventListenerRecords) {
                    try {
                        // Invoke listener
                        eventListenerRecord.listener().process(event);

                        // Record successful processing
                        eventBusMetricsMonitor.eventDeliveredCounter(topic, event);
                    } catch (Exception ex) {
                        // Handle listener error
                        getLogger().error("Error invoking handler: {}", eventListenerRecord.listener().getClass(), ex);
                        eventListenerRecord.exceptionHandler().handle(ex, event);

                        // Record error metrics
                        eventBusMetricsMonitor.eventDeadCounter(topic, event);
                    }
                }
            });
        } catch (RejectedExecutionException e) {
            getLogger().error("Event rejected by executor service: topic={}, event={}",
                    topic, event.getType().getSimpleName(), e);
            eventBusMetricsMonitor.eventDeadCounter(topic, event);
        }
    }

    @Override
    public void registerSubscribe(EventListener eventListener) {
        registerSubscribe("default", eventListener);
    }

    @Override
    public void registerSubscribe(String topic, EventListener eventListener) {
        checkNotShutdown();
        getSubscriberRegistry().register(topic, eventListener);
    }

    @Override
    public void removeSubscribe(String topic, EventListener eventListener) {
        checkNotShutdown();
        getSubscriberRegistry().remove(topic, eventListener);
    }
}
