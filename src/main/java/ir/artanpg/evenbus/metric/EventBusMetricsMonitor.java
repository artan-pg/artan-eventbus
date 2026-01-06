package ir.artanpg.evenbus.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import ir.artanpg.commons.core.tools.jacoco.Generated;
import ir.artanpg.evenbus.model.Event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A thread-safe metrics monitor for EventBus operations.
 * This class provides monitoring capabilities for tracking event publication,
 * delivery, and dead events in an EventBus system using Micrometer.
 *
 * <p>The monitor uses Micrometer for metrics collection and provides three
 * primary counter types:
 * <ul>
 *   <li><b>Published Event</b>: Count of events published to the EventBus</li>
 *   <li><b>Successful Event</b>: Count of events successfully processed by
 *   listeners</li>
 *   <li><b>Dead Event</b>: Count of events that could not be delivered</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe and uses double-checked
 * locking for singleton initialization, along with concurrent collections for
 * counter caching.
 *
 * <p><b>Default Tags:</b> All metrics automatically include these tags:
 * <ul>
 *   <li>{@code topic}: The event topic/channel</li>
 *   <li>{@code eventType}: Simple class name of the event</li>
 * </ul>
 *
 * @author Mohammad Yazdian
 * @see Counter
 * @see MeterRegistry
 */
public final class EventBusMetricsMonitor {

    /**
     * Metric name for published events counter
     */
    public static final String EVENT_PUBLISHED_NAME = "eventbus.event.published";

    /**
     * Metric name for successfully delivered events counter
     */
    public static final String EVENT_SUCCESS_NAME = "eventbus.event.success";

    /**
     * Metric name for dead events counter
     */
    public static final String EVENT_DEAD_NAME = "eventbus.event.dead";

    /**
     * Thread-safe cache of counter instances keyed by their unique Meter.Id
     * This cache prevents repeated registry lookups and improves performance
     */
    private final Map<Meter.Id, Counter> countersCache = new ConcurrentHashMap<>();

    /**
     * Micrometer registry for metric registration and management
     */
    private final MeterRegistry meterRegistry;

    /**
     * Constructs a new EventBusMetricsMonitor instance with the specified
     * registry.
     *
     * <p>Create one instance per {@link MeterRegistry} you need to monitor.
     *
     * @param meterRegistry the metrics registry to use for all metric operations
     * @throws NullPointerException if {@code meterRegistry} is {@code null}
     */
    @Generated
    private EventBusMetricsMonitor(MeterRegistry meterRegistry) {
        if (meterRegistry == null) throw new IllegalArgumentException("the meterRegistry cannot be null");
        this.meterRegistry = meterRegistry;
    }

    /**
     * Creates a new {@link EventBusMetricsMonitor} instance for the specified
     * meter registry.
     *
     * @param meterRegistry the metrics registry to use for all metric operations
     * @return a new {@code EventBusMetricsMonitor} instance configured with the specified registry
     * @throws NullPointerException if {@code meterRegistry} is {@code null}
     */
    public static EventBusMetricsMonitor of(MeterRegistry meterRegistry) {
        return new EventBusMetricsMonitor(meterRegistry);
    }

    /**
     * Increments the published events counter with default tags.
     *
     * @param topic the event topic
     * @param event the event instance
     * @throws IllegalArgumentException if {@code topic} or {@code event} is {@code null}, or if {@code topic} is empty
     * @see #eventPublishedCounter(String, Event, Tags)
     */
    public void eventPublishedCounter(String topic, Event event) {
        eventPublishedCounter(topic, event, Tags.empty());
    }

    /**
     * Increments the published events counter with custom tags
     * This metric tracks events that have been published to the EventBus
     *
     * @param topic the event topic
     * @param event the event instance
     * @param tags  custom tags to add to the metric
     * @throws IllegalArgumentException if {@code topic} or {@code event} is {@code null}, or if {@code topic} is empty
     */
    public void eventPublishedCounter(String topic, Event event, Tags tags) {
        Tags allTags = mergeTagsWithDefaultTags(topic, event, tags);
        incrementCounter(EVENT_PUBLISHED_NAME, "Total number of events published at the EventBus level", allTags);
    }

    /**
     * Increments the successfully delivered events counter with default tags.
     *
     * @param topic the event topic
     * @param event the event instance
     * @throws IllegalArgumentException if {@code topic} or {@code event} is {@code null}, or if {@code topic} is empty
     * @see #eventDeliveredCounter(String, Event, Tags)
     */
    public void eventDeliveredCounter(String topic, Event event) {
        eventDeliveredCounter(topic, event, Tags.empty());
    }

    /**
     * Increments the successfully delivered events counter with custom tags.
     * This metric tracks events that have been successfully processed by
     * listeners.
     *
     * @param topic the event topic
     * @param event the event instance
     * @param tags  custom tags to add to the metric
     * @throws IllegalArgumentException if {@code topic} or {@code event} is {@code null}, or if {@code topic} is empty
     */
    public void eventDeliveredCounter(String topic, Event event, Tags tags) {
        Tags allTags = mergeTagsWithDefaultTags(topic, event, tags);
        incrementCounter(EVENT_SUCCESS_NAME, "Total number of events processed at the EventBus level", allTags);
    }

    /**
     * Increments the dead events counter with default tags.
     *
     * @param topic the event topic
     * @param event the event instance
     * @throws IllegalArgumentException if {@code topic} or {@code event} is {@code null}, or if {@code topic} is empty
     * @see #eventDeadCounter(String, Event, Tags)
     */
    public void eventDeadCounter(String topic, Event event) {
        eventDeadCounter(topic, event, Tags.empty());
    }

    /**
     * Increments the dead events counter with custom tags.
     * This metric tracks events that could not be delivered to any listener
     * (e.g., no subscribers, delivery failures).
     *
     * @param topic the event topic
     * @param event the event instance
     * @param tags  custom tags to add to the metric
     * @throws IllegalArgumentException if {@code topic} or {@code event} is {@code null}, or if {@code topic} is empty
     */
    public void eventDeadCounter(String topic, Event event, Tags tags) {
        Tags allTags = mergeTagsWithDefaultTags(topic, event, tags);
        incrementCounter(EVENT_DEAD_NAME, "Total number of events dead at the EventBus level", allTags);
    }

    /**
     * Returns all published event counters from the cache.
     * Useful for monitoring or debugging purposes.
     *
     * @return an unmodifiable list of published event counters
     */
    public List<Counter> getPublishedCounters() {
        return countersCache.values()
                .stream()
                .filter(counter -> counter.getId().getName().equals(EVENT_PUBLISHED_NAME))
                .toList();
    }

    /**
     * Returns all successful delivery counters from the cache.
     * Useful for monitoring or debugging purposes.
     *
     * @return an unmodifiable list of successful delivery counters
     */
    public List<Counter> getSuccessCounters() {
        return countersCache.values()
                .stream()
                .filter(counter -> counter.getId().getName().equals(EVENT_SUCCESS_NAME))
                .toList();
    }

    /**
     * Returns all dead counters from the cache.
     * Useful for monitoring or debugging purposes.
     *
     * @return an unmodifiable list of dead counters
     */
    public List<Counter> getDeadCounters() {
        return countersCache.values()
                .stream()
                .filter(counter -> counter.getId().getName().equals(EVENT_DEAD_NAME))
                .toList();
    }

    private Tags mergeTagsWithDefaultTags(String topic, Event event, Tags extraTags) {
        Tags defaultTags = generateDefaultTags(topic, event);
        if (extraTags == null || !extraTags.iterator().hasNext()) return defaultTags;
        return defaultTags.and(extraTags);
    }

    private Tags generateDefaultTags(String topic, Event event) {
        if (topic == null || topic.isBlank()) throw new IllegalArgumentException("The topic cannot be null or blank");
        if (event == null) throw new IllegalArgumentException("The event cannot be null");

        Tag topicTag = Tag.of("topic", topic);
        Tag eventTypeTag = Tag.of("eventType", event.getType().getSimpleName());

        return Tags.of(topicTag, eventTypeTag);
    }

    /**
     * Increments or creates a counter with caching
     * This method ensures that counter instances are reused to improve performance
     *
     * @param name        the metric name
     * @param description the metric description
     * @param tags        the metric tags
     */
    private void incrementCounter(String name, String description, Tags tags) {
        Meter.Id meterId = new Meter.Id(name, tags, null, description, Meter.Type.COUNTER);

        // If this ID already exists, it returns the same one, otherwise it creates a new one.
        Counter counter = countersCache.computeIfAbsent(meterId, id ->
                Counter.builder(name)
                        .description(description)
                        .tags(tags)
                        .register(meterRegistry));

        counter.increment();
    }
}
