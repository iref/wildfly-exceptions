
package cz.muni.exceptions.listener;

/**
 * Interface provides method, that process thrown exception.
 * 
 * @author Jan Ferko 
 */
public interface ExceptionListener {
    
    /**
     * Processes throwable, that caused ExceptionSource to fire event.
     * 
     * @param throwable throwable, that caused source to fire event.
     */
    void onThrownException(Throwable throwable);

}
