package ir.artanpg.evenbus.exception;

import ir.artanpg.evenbus.model.Event;
import ir.artanpg.evenbus.model.EventListener;

/**
 * A handler for managing exceptions that occur during event listener
 * execution.
 *
 * <p>This interface defines a strategy for handling exceptions thrown by
 * {@link EventListener} implementations during event processing. It allows for
 * centralized and consistent exception management across the event-driven
 * system.
 *
 * <p>Exception handlers can implement various strategies such as:
 * <ul>
 *   <li>Logging exceptions with contextual information</li>
 *   <li>Translating exceptions to more appropriate types</li>
 *   <li>Retrying failed operations with backoff strategies</li>
 *   <li>Publishing error events for downstream processing</li>
 *   <li>Circuit breaking to prevent system overload</li>
 *   <li>Graceful degradation or fallback mechanisms</li>
 * </ul>
 *
 * <p><b>Thread Safety:</b> Implementations should be thread-safe, as they may
 * be invoked from multiple threads concurrently during event processing.
 *
 * @author Mohammad Yazdian
 * @see Event
 * @see EventListener
 */
public interface ListenerExceptionHandler {

    /**
     * Handles an exception that occurred during event listener execution.
     *
     * <p>This method is invoked when an {@link EventListener#process(Event)}
     * method throws an exception. The handler receives both the exception and
     * the event that was being processed, allowing for contextual error
     * handling.
     *
     * @param exception the exception that was thrown during event processing
     * @param event     the event that was being processed when the exception occurred
     */
    void handle(Exception exception, Event event);
}
