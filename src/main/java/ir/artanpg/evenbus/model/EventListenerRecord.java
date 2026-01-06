package ir.artanpg.evenbus.model;

import ir.artanpg.commons.core.identifier.IdentifierGenerator;
import ir.artanpg.evenbus.exception.ListenerExceptionHandler;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A value-based record that wraps an {@link EventListener} to provide
 * additional functionality and type safety.
 *
 * <p>This record acts as a wrapper or container for {@link EventListener}
 * instances, allowing them to be treated as first-class objects with enhanced
 * capabilities.
 *
 * @author Mohammad Yazdian
 * @see EventListener
 */
public record EventListenerRecord(EventListener listener, Long registrationNumber) {
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
}
