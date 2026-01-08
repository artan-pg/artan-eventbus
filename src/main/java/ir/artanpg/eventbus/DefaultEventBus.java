package ir.artanpg.eventbus;

import io.micrometer.core.instrument.MeterRegistry;
import ir.artanpg.eventbus.model.Event;
import ir.artanpg.eventbus.model.EventListener;
import ir.artanpg.eventbus.model.EventListenerRecord;
import ir.artanpg.eventbus.subscriber.SubscriberRegistry;
import ir.artanpg.eventbus.metric.EventBusMetricsMonitor;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of the {@link EventBus} interface.
 * This class provides a production-ready event bus implementation with:
 * <ul>
 *   <li>Topic-based event routing</li>
 *   <li>Comprehensive metrics collection via Micrometer</li>
 *   <li>Thread-safe subscriber management using {@link CopyOnWriteArrayList}</li>
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

    /**
     * Registry for managing event subscribers and their topics.
     */
    private final SubscriberRegistry subscriberRegistry;

    private final EventBusMetricsMonitor eventBusMetricsMonitor;

    /**
     * Constructs a DefaultEventBus with the specified dependencies.
     *
     * @param meterRegistry      the metrics registry for collecting event bus metrics
     * @param subscriberRegistry the registry for managing event subscribers
     * @throws IllegalArgumentException if {@code meterRegistry} or {@code subscriberRegistry} is {@code null}
     */
    protected DefaultEventBus(MeterRegistry meterRegistry, SubscriberRegistry subscriberRegistry) {
        super(meterRegistry, subscriberRegistry);

        this.subscriberRegistry = subscriberRegistry;
        this.eventBusMetricsMonitor = EventBusMetricsMonitor.of(meterRegistry);
    }

    @Override
    public void publish(Event event) {
        publish("default", event);
    }

    @Override
    public void publish(String topic, Event event) {
        // Record publication metrics
        eventBusMetricsMonitor.eventPublishedCounter(topic, event);

        // Get listeners for the topic
        List<EventListenerRecord> eventListenerRecords = subscriberRegistry.get(topic);
        if (eventListenerRecords == null || eventListenerRecords.isEmpty()) {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("No listeners are registered for event {}", event.getType().getSimpleName());
            }
            return;
        }

        // Create a thread-safe copy to avoid concurrent modification issues
        List<EventListenerRecord> eventListenerRecordsCopy = new CopyOnWriteArrayList<>(eventListenerRecords);

        // Process event asynchronously
        getExecutorService().execute(() -> {
            for (EventListenerRecord eventListenerRecord : eventListenerRecordsCopy) {
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
    }

    @Override
    public void registerSubscribe(EventListener eventListener) {
        registerSubscribe("default", eventListener);
    }

    @Override
    public void registerSubscribe(String topic, EventListener eventListener) {
        subscriberRegistry.register(topic, eventListener);
    }

    @Override
    public void removeSubscribe(String topic, EventListener eventListener) {
        subscriberRegistry.remove(topic, eventListener);
    }
}
