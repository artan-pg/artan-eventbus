package ir.artanpg.evenbus;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import ir.artanpg.evenbus.model.Event;
import ir.artanpg.evenbus.model.EventListener;
import ir.artanpg.evenbus.model.EventListenerRecord;
import ir.artanpg.evenbus.subscriber.SubscriberRegistry;

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
 * they're interested in. The "default" topic is used when no specific topic is provided.</p>
 *
 * <p><b>Thread Safety:</b> This implementation is thread-safe. Subscriber registration,
 * removal, and event publication can be safely called from multiple threads concurrently.</p>
 *
 * <h2>Metrics Collected:</h2>
 * <table border="1">
 *   <tr>
 *     <th>Metric Name</th>
 *     <th>Description</th>
 *     <th>Tags</th>
 *   </tr>
 *   <tr>
 *     <td>eventbus.published</td>
 *     <td>Count of published events</td>
 *     <td>topic, type, bus</td>
 *   </tr>
 *   <tr>
 *     <td>eventbus.success</td>
 *     <td>Count of successfully processed events</td>
 *     <td>topic, eventType, identifier, timestamp, source, bus</td>
 *   </tr>
 *   <tr>
 *     <td>eventbus.listener.error</td>
 *     <td>Count of listener processing errors</td>
 *     <td>type, bus</td>
 *   </tr>
 * </table>
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

    private static final String EVENT_BUS_NAME = "simple";

    /**
     * Metrics registry for collecting event bus metrics.
     */
    private final MeterRegistry meterRegistry;

    /**
     * Registry for managing event subscribers and their topics.
     */
    private final SubscriberRegistry subscriberRegistry;

    /**
     * Constructs a DefaultEventBus with the specified dependencies.
     *
     * @param meterRegistry      the metrics registry for collecting event bus metrics
     * @param subscriberRegistry the registry for managing event subscribers
     * @throws IllegalArgumentException if {@code meterRegistry} or {@code subscriberRegistry} is {@code null}
     */
    protected DefaultEventBus(MeterRegistry meterRegistry, SubscriberRegistry subscriberRegistry) {
        super(meterRegistry, subscriberRegistry);

        this.meterRegistry = meterRegistry;
        this.subscriberRegistry = subscriberRegistry;
    }

    @Override
    public void publish(Event event) {
        publish("default", event);
    }

    @Override
    public void publish(String topic, Event event) {
        // Record publication metrics
        Counter.builder("eventbus.published")
                .tag("topic", topic)
                .tag("type", event.getType().getSimpleName())
                .tag("bus", EVENT_BUS_NAME)
                .register(meterRegistry)
                .increment();

        // Get listeners for the topic
        List<EventListenerRecord> eventListenerRecords = subscriberRegistry.get(topic);
        if (eventListenerRecords == null || eventListenerRecords.isEmpty()) {
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("No listeners are registered for event {}", event.getType().getSimpleName());
            }
            return;
        }

        // Create thread-safe copy to avoid concurrent modification issues
        List<EventListenerRecord> eventListenerRecordsCopy = new CopyOnWriteArrayList<>(eventListenerRecords);

        // Process event asynchronously
        getExecutorService().execute(() -> {
            for (EventListenerRecord eventListenerRecord : eventListenerRecordsCopy) {
                try {
                    // Invoke listener
                    eventListenerRecord.listener().process(event);

                    // Record successful processing
                    Counter.builder("eventbus.success")
                            .tag("topic", event.getName())
                            .tag("eventType", event.getEventType().getTitle())
                            .tag("identifier", event.getIdentifier())
                            .tag("timestamp", event.timestamp().toString())
                            .tag("source", event.getSource().toString())
                            .tag("bus", EVENT_BUS_NAME)
                            .register(meterRegistry)
                            .increment();
                } catch (Exception ex) {
                    // Handle listener error
                    getLogger().error("Error invoking handler: {}", eventListenerRecord.listener().getClass(), ex);
                    eventListenerRecord.exceptionHandler().handle(ex, event);

                    // Record error metrics
                    Counter.builder("eventbus.listener.error")
                            .tag("type", event.getType().getSimpleName())
                            .tag("bus", EVENT_BUS_NAME)
                            .register(meterRegistry)
                            .increment();
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
