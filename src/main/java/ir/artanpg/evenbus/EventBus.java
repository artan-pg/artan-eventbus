package ir.artanpg.evenbus;

import ir.artanpg.evenbus.model.Event;
import ir.artanpg.evenbus.model.EventListener;
import ir.artanpg.evenbus.subscriber.SubscriberRegistry;

/**
 * Interface for implementing the Event Bus pattern.
 *
 * <p>This pattern enables loosely coupled communication between event
 * publishers and subscribers through a centralized event distribution
 * mechanism.
 *
 * <p>The Event Bus acts as a mediator that manages event publication and
 * subscription, supporting both synchronous and asynchronous event processing
 * models.
 *
 * <p>Implementations of this interface may use single-threaded, multithreaded,
 * or distributed architectures for event handling.
 *
 * @author Mohammad Yazdian
 * @see Event
 * @see EventListener
 * @see SubscriberRegistry
 */
public interface EventBus {

    /**
     * Publishes an event to all default topics.
     *
     * @param event the event to publish
     */
    void publish(Event event);

    /**
     * Publishes an event to a specific topic.
     *
     * @param topic the topic name to publish to
     * @param event the event to publish
     */
    void publish(String topic, Event event);

    /**
     * Registers an event listener to receive all events from all default
     * topics
     *
     * @param eventListener the event listener to register
     */
    void registerSubscribe(EventListener eventListener);

    /**
     * Registers an event listener to receive events from a specific topic.
     *
     * @param topic         the topic name to subscribe to
     * @param eventListener the event listener to register for the specified topic
     */
    void registerSubscribe(String topic, EventListener eventListener);

    /**
     * Removes an event listener from a specific topic.
     *
     * <p>After calling this method, the listener will no longer receive events
     * from the specified topic.
     *
     * @param topic         the topic name to unsubscribe from
     * @param eventListener the event listener to remove from the specified topic
     */
    void removeSubscribe(String topic, EventListener eventListener);

    /**
     * Gracefully shuts down the EventBus.
     *
     * <p>After calling this method:
     * <ul>
     *   <li>No new events will be accepted</li>
     *   <li>All pending events will be processed</li>
     *   <li>All system resources will be released</li>
     *   <li>All listeners will be automatically unsubscribed</li>
     * </ul>
     */
    void shutdown();
}
