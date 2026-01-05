package ir.artanpg.evenbus.model;

import ir.artanpg.commons.core.identifier.IdentifierGenerator;
import ir.artanpg.evenbus.exception.ListenerExceptionHandler;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

/**
 * A functional interface that defines a handler for processing events in an
 * event-driven architecture.
 *
 * <p>Event listeners are the core components that react to events dispatched
 * within the system. They encapsulate the business logic that should be
 * executed when specific events occur.
 *
 * <p><b>Thread Safety Note:</b> Implementations should be thread-safe if they
 * are intended to be used in concurrent environments, as events may be
 * dispatched from multiple threads.
 *
 * @author Mohammad Yazdian
 * @see Event
 * @see ListenerExceptionHandler
 */
@FunctionalInterface
public interface EventListener {

    /**
     * Processes the given event.
     *
     * <p>This is the primary method that contains the logic to execute when an
     * event is dispatched to this listener. Implementations should perform the
     * necessary actions in response to the event, such as:
     * <ul>
     *   <li>Updating application state</li>
     *   <li>Triggering side effects or workflows</li>
     *   <li>Logging or auditing the event occurrence</li>
     * </ul>
     *
     * <p><b>Implementation Guidelines:</b>
     * <ul>
     *   <li>The method should complete execution in a reasonable time to avoid
     *       blocking the event dispatch thread.</li>
     *   <li>If the processing is time-consuming, consider delegating to an
     *       asynchronous executor.</li>
     *   <li>Any exceptions thrown may be handled by the configured
     *       {@link #exceptionHandler()}.</li>
     *   <li>The method should not modify the source or other properties of the
     *       {@link Event} unless explicitly allowed by the event contract.</li>
     * </ul>
     * </p>
     *
     * @param event the event to process, must not be {@code null}
     */
    void process(Event event);

    /**
     * Returns the execution priority of this listener relative to other
     * listeners.
     *
     * <p>When multiple listeners are registered for the same event type, they
     * are typically executed in order of their priority. Higher numerical
     * values indicate higher priority (executed earlier).
     *
     * @return the priority value
     */
    default int priority() {
        return 0;
    }

    /**
     * Provides an identifier generator for this listener.
     *
     * <p>The identifier generator is used to create unique identifiers for
     * events processed by this listener, or for the listener itself. This can
     * be useful for:
     * <ul>
     *   <li>Correlating related events in distributed systems</li>
     *   <li>Tracking event processing through multiple stages</li>
     *   <li>Auditing and debugging event flows</li>
     *   <li>Ensuring idempotency in event processing</li>
     * </ul>
     *
     * @return an {@link IdentifierGenerator} instance
     * @see IdentifierGenerator
     */
    default IdentifierGenerator identifier() {
        return UUID::randomUUID;
    }

    /**
     * Returns the exception handler for managing errors during event
     * processing.
     *
     * <p>If the {@link #process(Event)} method throws an exception, the
     * returned exception handler will be invoked to manage the error. This
     * mechanism allows for graceful error recovery, logging, or custom error
     * handling strategies without interrupting the entire event processing
     * pipeline.
     *
     * @return the {@link ListenerExceptionHandler} for this listener
     * @see ListenerExceptionHandler
     */
    default ListenerExceptionHandler exceptionHandler() {
        return (exception, event) ->
                LoggerFactory
                        .getLogger(ListenerExceptionHandler.class)
                        .error("Error invoking in the EventListener handler: the Event is: {}",
                                event.getClass().getSimpleName(), exception);
    }

    /**
     * Creates a composed listener that sequentially executes this listener
     * followed by another.
     *
     * <p>This method enables functional composition of listeners, allowing the
     * creation of processing pipelines where multiple listeners execute in a
     * defined order.
     * The composed listener will:
     * <ol>
     *   <li>Execute this listener's {@link #process(Event)} method</li>
     *   <li>Execute the {@code after} listener's {@code process} method</li>
     * </ol>
     *
     * <p><b>Composition Characteristics:</b>
     * <ul>
     *   <li>If this listener throws an exception, the {@code after} listener will not be executed.</li>
     *   <li>The priority of the composed listener is determined by the first listener in the chain.</li>
     *   <li>The exception handler of the composed listener is that of the first listener.</li>
     * </ul>
     *
     * @param after the listener to execute after this listener, must not be {@code null}
     * @return a new {@link EventListener} that represents the sequential execution
     * of this listener followed by {@code after}
     */
    default EventListener andThen(EventListener after) {
        Objects.requireNonNull(after);
        return (Event event) -> {
            process(event);
            after.process(event);
        };
    }
}
