package ir.artanpg.eventbus.subscriber;

import ir.artanpg.eventbus.exception.InvalidEventListenerException;
import ir.artanpg.eventbus.exception.InvalidTopicException;
import ir.artanpg.eventbus.model.EventListener;
import ir.artanpg.eventbus.model.EventListenerRecord;

import java.util.HashMap;
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
            boolean removed = listenerSet.removeIf(o -> Objects.equals(o.listener(), eventListener));
            if (removed && listenerSet.isEmpty()) subscriptions.remove(topic);
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
    public Map<String, List<EventListenerRecord>> getAll() {
        Map<String, List<EventListenerRecord>> snapshot = new HashMap<>();
        for (Map.Entry<String, ConcurrentSkipListSet<EventListenerRecord>> entry : subscriptions.entrySet()) {
            snapshot.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(snapshot);
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
