package ir.artanpg.eventbus.model;

import ir.artanpg.commons.core.identifier.IdentifierGenerator;
import ir.artanpg.eventbus.exception.ListenerExceptionHandler;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A value-based record that wraps an {@link EventListener} to provide
 * additional functionality and type safety.
 *
 * <p>Implements {@link Comparable} to allow natural ordering based on priority
 * (higher first) and registration number (older first).
 *
 * @param listener           the wrapped event listener
 * @param registrationNumber the unique registration number for ordering purposes
 * @author Mohammad Yazdian
 * @see EventListener
 */
public record EventListenerRecord(EventListener listener,
                                  Long registrationNumber) implements Comparable<EventListenerRecord> {
    private static final AtomicLong globalCounter = new AtomicLong(0);

    /**
     * Canonical constructor that validates the wrapped listener.
     *
     * @param listener the {@link EventListener} to wrap
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    public EventListenerRecord {
        Objects.requireNonNull(listener, "THe EventListener must not be null");
    }

    /**
     * Factory method for creating {@code EventListenerRecord} instances.
     *
     * @param listener the {@link EventListener} to wrap
     * @return a new {@code EventListenerRecord} instance containing the specified listener
     * @throws NullPointerException if {@code listener} is {@code null}
     * @see #EventListenerRecord(EventListener, Long)
     */
    public static EventListenerRecord of(EventListener listener) {
        return new EventListenerRecord(listener, globalCounter.incrementAndGet());
    }

    /**
     * Returns the priority of the wrapped listener.
     *
     * <p>Delegates to the wrapped listener's {@link EventListener#priority()}
     * method.
     *
     * @return the priority value of the wrapped listener
     */
    public int priority() {
        return listener.priority();
    }

    /**
     * Returns the {@link IdentifierGenerator} associated with the wrapped
     * event listener.
     *
     * <p>Delegates to the wrapped listener's
     * {@link EventListener#identifier()} method.
     *
     * @return the {@link IdentifierGenerator} associated with the wrapped listener.
     */
    public IdentifierGenerator identifier() {
        return listener.identifier();
    }

    /**
     * Returns the {@link ListenerExceptionHandler} associated with the wrapped event listener.
     *
     * <p>Delegates to the wrapped listener's
     * {@link EventListener#exceptionHandler()} method.
     *
     * @return the {@link ListenerExceptionHandler} associated with the wrapped listener
     */
    public ListenerExceptionHandler exceptionHandler() {
        return listener.exceptionHandler();
    }

    /**
     * Creates a composed {@code EventListenerRecord} that executes the wrapped
     * listener followed by another listener.
     *
     * <p>This method enables the creation of listener execution chains by
     * composing the current wrapped listener with an additional listener. The
     * resulting {@code EventListenerRecord} will execute both listeners
     * sequentially.
     *
     * @param after the listener to execute after the current listener
     * @return a new {@code EventListenerRecord} representing the sequential
     * execution of the current listener followed by the {@code after} listener
     * @throws NullPointerException if {@code after} is {@code null}
     * @see EventListener#andThen(EventListener)
     * @see EventListenerRecord#EventListenerRecord(EventListener, Long)
     */
    public EventListenerRecord andThen(EventListener after) {
        return new EventListenerRecord(listener.andThen(after), registrationNumber);
    }

    /**
     * Compares this record with another for ordering.
     *
     * <p>Ordering is primarily by priority in <b>descending</b> order
     * (higher priority first), and then by registrationNumber in
     * <b>ascending</b> order (older first).
     *
     * @param other the other record to compare to
     * @return a negative integer, zero, or a positive integer as this record is less than, equal to,
     * or greater than the other.
     */
    @Override
    public int compareTo(EventListenerRecord other) {
        // Higher priority first -> descending
        int priorityCompare = Integer.compare(other.priority(), this.priority());
        if (priorityCompare != 0) return priorityCompare;

        // Lower registration number first -> ascending
        return Long.compare(this.registrationNumber, other.registrationNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventListenerRecord that = (EventListenerRecord) o;

        return Objects.equals(listener, that.listener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listener);
    }
}
