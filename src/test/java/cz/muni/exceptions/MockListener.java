
package cz.muni.exceptions;

import cz.muni.exceptions.listener.ExceptionListener;

/**
 *
 * @author Jan Ferko 
 */
public class MockListener implements ExceptionListener {
    private boolean wasNotified = false;
    
    @Override
    public void onThrownException(Throwable throwable) {
        wasNotified = true;
    }
    
    public boolean isNotified() {
        return wasNotified;
    }
}
