package ir.artanpg.evenbus.subscriber;

import ir.artanpg.evenbus.model.EventListener;
import ir.artanpg.evenbus.model.EventListenerRecord;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of the {@link SubscriberRegistry} interface.
 * This class manages event listener subscriptions with topic-based
 * organization, thread-safe operations, and priority-based ordering.
 *
 * <p><b>Key Features:</b></p>
 * <ul>
 *   <li>Thread-safe implementation using {@link ConcurrentHashMap} and
 *   {@link CopyOnWriteArrayList}</li>
 *   <li>Automatic cleanup of empty topic entries</li>
 *   <li>Default topic support for convenience methods</li>
 * </ul>
 *
 * <p><b>Listener Ordering:</b></p>
 * <p>Listeners are ordered primarily by their priority (ascending order,
 * higher values first).
 * For listeners with equal priority, registration order is preserved using a monotonically
 * increasing counter.</p>
 *
 * <p><b>Thread Safety:</b></p>
 * <p>All operations are thread-safe. Multiple threads can register, remove, and
 * retrieve listeners concurrently without external synchronization.</p>
 *
 * <h3>Ordering Example:</h3>
 * <pre>
 * {@code
 * // Assuming all listeners have default priority (0)
 * registry.register("topic", listener1); // Registered first
 * registry.register("topic", listener2); // Registered second
 * registry.register("topic", listener3); // Registered third
 *
 * // Order when retrieved: [listener1, listener2, listener3]
 * }
 * </pre>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * {@code
 * SubscriberRegistry registry = new DefaultSubscriberRegistry();
 *
 * // Register listeners with different priorities
 * EventListener highPriorityListener = new HighPriorityListener();
 * EventListener normalListener = new NormalListener();
 *
 * registry.register("user.events", highPriorityListener);
 * registry.register("user.events", normalListener);
 *
 * // Get ordered listeners for a topic
 * List<EventListenerRecord> listeners = registry.get("user.events");
 * // highPriorityListener comes before normalListener
 * }
 * </pre>
 *
 * @author Mohammad Yazdian
 * @see EventListener
 * @see SubscriberRegistry
 * @see EventListenerRecord
 */
public class DefaultSubscriberRegistry implements SubscriberRegistry {

    private final AtomicLong registrationCounter = new AtomicLong(0);
    private final Map<String, List<EventListenerRecord>> subscriptions = new ConcurrentHashMap<>();

    @Override
    public void register(EventListener eventListener) {
        register("default", eventListener);
    }

    @Override
    public void register(String topic, EventListener eventListener) {
        validateParameters(topic, eventListener);

        List<EventListenerRecord> eventListenerRecords =
                subscriptions.computeIfAbsent(topic, s -> new CopyOnWriteArrayList<>());

        EventListenerRecord listenerRecord = EventListenerRecord.of(eventListener);
        eventListenerRecords.add(listenerRecord);

        eventListenerRecords.sort(Comparator
                .comparingInt((EventListenerRecord eventListenerRecord) -> eventListenerRecord.listener().priority())
                .thenComparingLong(
                        eventListenerRecord -> (eventListenerRecord.equals(listenerRecord)) ?
                                registrationCounter.incrementAndGet() : eventListenerRecord.registrationNumber()));
    }

    @Override
    public void remove(EventListener eventListener) {
        remove("default", eventListener);
    }

    @Override
    public void remove(String topic, EventListener eventListener) {
        validateParameters(topic, eventListener);

        List<EventListenerRecord> eventListenerRecordList = subscriptions.get(topic);
        if (eventListenerRecordList != null) {
            eventListenerRecordList.remove(EventListenerRecord.of(eventListener));
            if (eventListenerRecordList.isEmpty()) {
                subscriptions.remove(topic);
            }
        }
    }

    @Override
    public List<EventListenerRecord> get(String topic) {
        if (topic == null || topic.isBlank()) throw new IllegalArgumentException("The topic cannot be null or empty");

        return subscriptions.getOrDefault(topic, List.of());
    }

    private void validateParameters(String topic, EventListener eventListener) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("Topic cannot be null or empty");
        }
        if (eventListener == null) {
            throw new IllegalArgumentException("EventListener cannot be null");
        }
    }
}
