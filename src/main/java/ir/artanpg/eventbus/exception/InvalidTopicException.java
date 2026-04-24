package ir.artanpg.eventbus.exception;

import ir.artanpg.eventbus.subscriber.SubscriberRegistry;

import java.io.Serial;

/**
 * Thrown to indicate that a topic name provided to an EventBus operation is
 * invalid.
 *
 * <p>This exception is typically thrown when a topic is {@code null},
 * {@code blank} or otherwise does not meet the requirements defined by the
 * {@link SubscriberRegistry} implementation.
 *
 * @author Mohammad Yazdian
 * @see EventBusException
 * @see InvalidEventListenerException
 */
public class InvalidTopicException extends EventBusException {
    @Serial
    private static final long serialVersionUID = 5857839232308164007L;

    /**
     * Constructs a new InvalidTopicException with {@code null} as its detail
     * message.
     */
    public InvalidTopicException() {
        super();
    }

    /**
     * Constructs a new InvalidTopicException with the specified detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public InvalidTopicException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidTopicException with the specified detail message
     * and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method)
     */
    public InvalidTopicException(String message, Throwable cause) {
        super(message, cause);
    }
}
