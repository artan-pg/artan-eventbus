package ir.artanpg.eventbus.exception;

import java.io.Serial;

/**
 * Base exception for all EventBus related errors.
 *
 * <p>This unchecked exception serves as the root of the EventBus exception
 * hierarchy, allowing clients to catch all EventBus-specific errors in a
 * single catch block without interfering with other
 * {@link RuntimeException runtime exceptions}.
 *
 * @author Mohammad Yazdian
 * @see InvalidTopicException
 * @see InvalidEventListenerException
 */
public class EventBusException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -6550466706122747599L;

    /**
     * Constructs a new EventBusException with {@code null} as its detail
     * message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public EventBusException() {
        super();
    }

    /**
     * Constructs a new EventBusException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public EventBusException(String message) {
        super(message);
    }

    /**
     * Constructs a new EventBusException with the specified detail message and
     * cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public EventBusException(String message, Throwable cause) {
        super(message, cause);
    }
}
