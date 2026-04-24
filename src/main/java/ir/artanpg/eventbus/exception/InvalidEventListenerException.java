package ir.artanpg.eventbus.exception;

import ir.artanpg.eventbus.model.EventListener;

import java.io.Serial;

/**
 * Thrown to indicate that an {@link EventListener} provided to an EventBus
 * operation is invalid.
 *
 * <p>This exception is typically thrown when an {@code EventListener} is
 * {@code null} or, in some implementations, when the listener does not meet
 * specific requirements (e.g., missing annotations, invalid method signatures
 * for reflective dispatch).
 *
 * @author Mohammad Yazdian
 * @see EventBusException
 * @see InvalidTopicException
 */
public class InvalidEventListenerException extends EventBusException {
    @Serial
    private static final long serialVersionUID = -6322181965615942129L;

    /**
     * Constructs a new InvalidEventListenerException with {@code null} as its
     * detail message.
     */
    public InvalidEventListenerException() {
        super();
    }

    /**
     * Constructs a new InvalidEventListenerException with the specified detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public InvalidEventListenerException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidEventListenerException with the specified detail
     * message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public InvalidEventListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
