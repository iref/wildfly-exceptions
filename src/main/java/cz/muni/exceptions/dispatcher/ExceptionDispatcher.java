
package cz.muni.exceptions.dispatcher;

import cz.muni.exceptions.listener.ExceptionListener;
import cz.muni.exceptions.source.ExceptionReport;
import java.util.Set;

/**
 * Interface provides communication pipeline between exception sources and 
 * listeners, that react to new exception.
 * 
 * @author Jan Ferko 
 */
public interface ExceptionDispatcher {
    
    /**
     * Warns registered listeners, that new report was produced in exception source.
     * If throwable is {@code null}, implementation should ignore it and doesn't
     * warn listeners about new throwable.
     * 
     * @param report report that was produced by source and should be sent to listeners     
     */
    void warnListeners(ExceptionReport report);
    
    /**
     * Adds new listener to source. 
     * New listener is warn about any new throwables that are produced after
     * it was added to source.
     * Method ignores {@code null} values.
     * 
     * @param listener listener, that is registered to source     
     */
    void registerListener(ExceptionListener listener);

    /**
     * Removes listener from dispatcher.
     *
     * @param listener listener, that should be removed
     */
    void unregisterListener(ExceptionListener listener);
    
    /**
     * Returns set of listeners, that are registered to source.     
     * 
     * @return set of registered listeners
     */
    Set<ExceptionListener> getListeners();

    /**
     * Starts dispatching event.
     */
    void start();

    /**
     *  Stops dispatching of new reports.
     */
    void stop();


}
