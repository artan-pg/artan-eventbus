package ir.artanpg.eventbus.exception;

import ir.artanpg.eventbus.EventBus;

import java.io.Serial;

/**
 * Thrown to indicate that an operation has been attempted on an EventBus
 * instance that has already been shut down.
 *
 * <p>Once an EventBus is shut down, it cannot accept new events, new
 * subscriber registrations, or unsubscriptions. Any attempt to publish events,
 * register, or remove listeners after shutdown will result in this exception.
 *
 * @author Mohammad Yazdian
 * @see EventBusException
 * @see EventBus#shutdown()
 */
public class EventBusShutdownException extends EventBusException {
    @Serial
    private static final long serialVersionUID = 1783913882797232271L;

    /**
     * Constructs a new EventBusShutdownException with {@code null} as its
     * detail message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public EventBusShutdownException() {
        super();
    }

    /**
     * Constructs a new EventBusShutdownException with the specified detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public EventBusShutdownException(String message) {
        super(message);
    }

    /**
     * Constructs a new EventBusShutdownException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public EventBusShutdownException(String message, Throwable cause) {
        super(message, cause);
    }
}
