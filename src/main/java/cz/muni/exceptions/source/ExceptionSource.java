
package cz.muni.exceptions.source;

import cz.muni.exceptions.listener.ExceptionListener;
import java.util.Set;

/**
 * Interface provides access to exceptions, that were thrown and than handled by 
 * different sources.
 * 
 * @author Jan Ferko 
 */
public interface ExceptionSource {
    
    /**
     * Warns registered listeners, that new throwable was produced in exception source.
     * If throwable is {@code null}, implementation should ignore it and doesn't
     * warn listeners about new throwable.
     * 
     * @param throwable throwable that was produced by source and should be sent to listeners     
     */
    void warnListeners(Throwable throwable);
    
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
     * Returns set of listeners, that are registered to source.     
     * 
     * @return set of registered listeners
     */
    Set<ExceptionListener> getListeners();
}
