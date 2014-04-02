
package cz.muni.exceptions.listener;

import cz.muni.exceptions.source.ExceptionReport;

/**
 * Interface provides method, that process thrown exception.
 * 
 * @author Jan Ferko 
 */
public interface ExceptionListener {
    
    /**
     * Processes report about exception, which caused ExceptionSource to fire event.
     * 
     * @param report report about exception, that caused source to fire event.
     */
    void onThrownException(ExceptionReport report);

}
