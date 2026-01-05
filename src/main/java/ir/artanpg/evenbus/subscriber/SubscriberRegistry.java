package ir.artanpg.evenbus.subscriber;

import ir.artanpg.evenbus.model.EventListener;
import ir.artanpg.evenbus.model.EventListenerRecord;

import java.util.List;

/**
 * Registry for managing event listener subscriptions organized by topics.
 * This interface defines a mechanism to register, remove, and retrieve event
 * listeners for specific topics in an event-driven architecture.
 *
 * <p>Implementations of this interface are responsible for maintaining the
 * association between topics and their corresponding event listeners, ensuring
 * thread-safe operations for concurrent access patterns typical in event bus
 * systems.
 *
 * <h2>Key Responsibilities:</h2>
 * <ul>
 *   <li>Store and organize event listeners by topic</li>
 *   <li>Provide thread-safe registration and removal operations</li>
 *   <li>Enable efficient retrieval of listeners for a given topic</li>
 *   <li>Handle potential concurrency issues in multithreaded environments</li>
 * </ul>
 *
 * <h2>Thread Safety:</h2>
 * <p>All implementations must be thread-safe, supporting concurrent
 * registration, removal, and retrieval operations from multiple threads.
 *
 * <h2>Topic Semantics:</h2>
 * <p>Topics are case-sensitive string identifiers that categorize events.
 *
 * @author Mohammad Yazdian
 * @see EventListener
 * @see EventListenerRecord
 */
public interface SubscriberRegistry {

    /**
     * Registers an event listener to receive events from the default topic.
     * The default topic is used when events are published without specifying a
     * topic.
     *
     * <p>This is equivalent to calling
     * {@code register("default", eventListener)}.
     * Listeners registered through this method will only receive events
     * published to the default topic, not all topics.
     *
     * @param eventListener the listener to register for the default topic
     * @throws IllegalArgumentException if {@code eventListener} is {@code null}
     * @see #register(String, EventListener)
     */
    void register(EventListener eventListener);

    /**
     * Registers an event listener for a specific topic.
     * After registration, the listener will receive events published to this
     * topic.
     *
     * <p><b>Implementation Notes:</b></p>
     * <ul>
     *   <li>If the same listener is registered multiple times for the same
     *   topic, implementations may choose to ignore duplicates or register
     *   multiple instances.</li>
     *   <li>The behavior for null parameters is implementation-defined but
     *   should typically throw {@link IllegalArgumentException}.</li>
     *   <li>Registration should be an atomic operation to ensure thread
     *   safety.</li>
     * </ul>
     *
     * @param topic         the topic to subscribe to
     * @param eventListener the listener to register
     * @throws IllegalArgumentException if {@code topic} is {@code null} or {@code blank}
     * @throws IllegalArgumentException if {@code eventListener}is {@code null}
     * @see #remove(String, EventListener)
     * @see EventListenerRecord
     */
    void register(String topic, EventListener eventListener);

    /**
     * Removes an event listener from the default topic.
     * After removal, the listener will no longer receive events published to
     * the default topic.
     *
     * <p>This is equivalent to calling
     * {@code remove("default", eventListener)}.
     *
     * @param eventListener the listener to remove from the default topic
     * @throws IllegalArgumentException if {@code eventListener} is {@code null}
     * @see #remove(String, EventListener)
     */
    void remove(EventListener eventListener);

    /**
     * Removes an event listener from a specific topic.
     * After removal, the listener will no longer receive events published to
     * this topic.
     *
     * <p><b>Implementation Notes:</b></p>
     * <ul>
     *   <li>If the listener is not registered for the given topic, this method
     *   should have no effect (idempotent operation).</li>
     *   <li>If the same listener was registered multiple times,
     *   implementations should define whether all instances are removed or
     *   just one.</li>
     *   <li>Removal should be an atomic operation to ensure thread
     *   safety.</li>
     * </ul>
     *
     * @param topic         the topic to unsubscribe from
     * @param eventListener the listener to remove
     * @throws IllegalArgumentException if {@code topic} is {@code null} or {@code blank}
     * @throws IllegalArgumentException if {@code eventListener}is {@code null}
     * @see #register(String, EventListener)
     * @see EventListenerRecord
     */
    void remove(String topic, EventListener eventListener);

    /**
     * Retrieves all registered event listeners for a specific topic.
     *
     * <p><b>Implementation Notes:</b></p>
     * <ul>
     *   <li>The returned list should be a snapshot or a thread-safe view of
     *   the current listeners to avoid concurrent modification issues.</li>
     *   <li>Modifications to the returned list should not affect the internal
     *   registry state.</li>
     *   <li>If no listeners are registered for the topic, implementations
     *   should return an empty list rather than {@code null}.</li>
     *   <li>The order of listeners in the returned list may be significant
     *   (e.g., registration order) depending on the implementation.</li>
     * </ul>
     *
     * @param topic the topic to query
     * @return an immutable or thread-safe list of {@link EventListenerRecord}
     * objects for the specified topic. returns empty list if no listeners are
     * registered.
     * @throws IllegalArgumentException if {@code topic} is {@code null} or {@code blank}
     * @see EventListenerRecord
     */
    List<EventListenerRecord> get(String topic);
}
