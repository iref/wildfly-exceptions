
package cz.muni.exceptions;

import cz.muni.exceptions.listener.ExceptionListener;
import cz.muni.exceptions.source.ExceptionReport;

/**
 *
 * @author Jan Ferko 
 */
public class MockListener implements ExceptionListener {
    private boolean wasNotified = false;
    
    @Override
    public void onThrownException(ExceptionReport report) {
        wasNotified = true;
    }
    
    public boolean isNotified() {
        return wasNotified;
    }
}
