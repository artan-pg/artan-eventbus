package ir.artanpg.eventbus.model;

/**
 * Represents a type or category of an event in the system.
 *
 * <p>Event types are used to classify and categorize events for processing,
 * routing, and filtering purposes. Each event type should have a unique and
 * meaningful title that identifies its category.
 *
 * <p>Implementations of this interface should be immutable and thread-safe, as
 * event types may be shared across multiple threads and event instances.
 *
 * @author Mohammad Yazdian
 * @see Event
 */
public interface EventType {

    /**
     * Returns the title of this event type.
     *
     * <p>The title should be a human-readable string that clearly identifies
     * the category or nature events of this type. It is recommended to use
     * consistent naming conventions (e.g., uppercase with underscores)
     * throughout the application.
     *
     * @return the title of this event type, never {@code null} or {@code empty}
     */
    String getTitle();
}
