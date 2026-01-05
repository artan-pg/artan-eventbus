package ir.artanpg.evenbus.model;

import java.io.Serializable;

/**
 * The root interface from which all event state objects shall be implemented.
 *
 * <p>All Events are constructed with a reference to the object, the "source",
 * that is logically deemed to be the object upon which the Event in question
 * initially occurred upon.
 *
 * @author Mohammad Yazdian
 * @see EventType
 * @see EventListener
 */
public interface Event extends Serializable {

    /**
     * Returns the name of this event.
     *
     * <p>The name should be a human-readable string that identifies the event
     * meaningfully.
     *
     * @return the name of the event, never {@code null}
     */
    String getName();

    /**
     * Returns the type of this event.
     *
     * <p>The event type categorizes the event and can be used for event
     * routing and filtering.
     *
     * @return the event type, never {@code null}
     */
    EventType getEventType();

    /**
     * Returns a unique identifier for this event instance.
     *
     * <p>The identifier should be unique across all event instances and can be
     * used for tracking, deduplication, and correlation purposes.
     *
     * @return the unique identifier for this event, never {@code null}
     */
    String getIdentifier();

    /**
     * Returns the timestamp when this event was created.
     *
     * <p>The timestamp has represented the number of milliseconds since the
     * Unix epoch.
     *
     * @return the creation timestamp in milliseconds
     */
    Long timestamp();

    /**
     * Returns the source object that originally generated this event.
     *
     * <p>The source is the object upon which the event initially occurred.
     *
     * @return the source object that generated this event, may be {@code null}
     * if the source is unknown or not applicable
     */
    Object getSource();

    /**
     * Returns the concrete class type of this event.
     *
     * <p>This method provides runtime type information about the event
     * implementation.
     *
     * @return the concrete class of this event, never {@code null}
     */
    Class<? extends Event> getType();
}
