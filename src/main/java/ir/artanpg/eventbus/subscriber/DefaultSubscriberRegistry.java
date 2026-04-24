package ir.artanpg.eventbus.subscriber;

import ir.artanpg.eventbus.exception.InvalidEventListenerException;
import ir.artanpg.eventbus.exception.InvalidTopicException;
import ir.artanpg.eventbus.model.EventListener;
import ir.artanpg.eventbus.model.EventListenerRecord;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Default implementation of the {@link SubscriberRegistry} interface.
 *
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Thread-safe implementation using {@link ConcurrentHashMap} and
 *   {@link ConcurrentSkipListSet}</li>
 *   <li>Listeners are automatically maintained in sorted order by priority
 *   (higher first) and registration order via {@link EventListenerRecord#compareTo}</li>
 *   <li>Automatic cleanup of empty topic entries</li>
 * </ul>
 *
 * @author Mohammad Yazdian
 * @see EventListener
 * @see SubscriberRegistry
 * @see EventListenerRecord
 */
public class DefaultSubscriberRegistry implements SubscriberRegistry {

    private final Map<String, ConcurrentSkipListSet<EventListenerRecord>> subscriptions = new ConcurrentHashMap<>();

    @Override
    public void register(EventListener eventListener) {
        register("default", eventListener);
    }

    @Override
    public void register(String topic, EventListener eventListener) {
        validateParameters(topic, eventListener);

        ConcurrentSkipListSet<EventListenerRecord> listenerSet =
                subscriptions.computeIfAbsent(topic, s -> new ConcurrentSkipListSet<>());

        listenerSet.add(EventListenerRecord.of(eventListener));
    }

    @Override
    public void remove(EventListener eventListener) {
        remove("default", eventListener);
    }

    @Override
    public void remove(String topic, EventListener eventListener) {
        validateParameters(topic, eventListener);

        ConcurrentSkipListSet<EventListenerRecord> listenerSet = subscriptions.get(topic);
        if (listenerSet != null) {
            // Find the record that wraps the given listener
            // Since EventListenerRecord uses listener in compareTo, we need to create a dummy record
            // that matches based on listener identity? Better to iterate and remove by equality of listener.
            // ConcurrentSkipListSet doesn't have removeIf, but we can find and remove.
            // Using iterator is safe because we are not modifying while iterating? The set is concurrent.
            // However, the efficient way: create a dummy record with the same listener?
            // But registrationNumber will be wrong, so compareTo won't match.
            // So we need to remove based on the actual reference.
            // Option: iterate over the set and remove the one with matching listener.
            // Since the set is concurrent, and removal is rare compared to registration, it's acceptable.
            for (EventListenerRecord eventListenerRecord : listenerSet) {
                if (Objects.equals(eventListenerRecord.listener(), eventListener)) {
                    listenerSet.remove(eventListenerRecord);
                    break;
                }
            }
            // Clean up empty set
            if (listenerSet.isEmpty()) subscriptions.remove(topic);
        }
    }

    @Override
    public List<EventListenerRecord> get(String topic) {
        if (topic == null || topic.isBlank()) throw new InvalidTopicException("The topic cannot be null or empty");

        ConcurrentSkipListSet<EventListenerRecord> listenerSet = subscriptions.get(topic);
        if (listenerSet == null || listenerSet.isEmpty()) return List.of();

        // Return an immutable snapshot (copy) to avoid concurrent modification
        return List.copyOf(listenerSet);
    }

    @Override
    public void clear(String topic) {
        if (topic == null || topic.isBlank()) throw new InvalidTopicException("Topic cannot be null or empty");
        subscriptions.remove(topic);
    }

    @Override
    public void clearAll() {
        subscriptions.clear();
    }

    private void validateParameters(String topic, EventListener eventListener) {
        if (topic == null || topic.isBlank()) throw new InvalidTopicException("Topic cannot be null or empty");
        if (eventListener == null) throw new InvalidEventListenerException("EventListener cannot be null");
    }
}
