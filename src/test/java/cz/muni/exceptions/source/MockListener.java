
package cz.muni.exceptions.source;

import cz.muni.exceptions.listener.ExceptionListener;

/**
 *
 * @author Jan Ferko 
 */
public class MockListener implements ExceptionListener {
    boolean wasNotified = false;
    
    @Override
    public void onThrownException(Throwable throwable) {
        wasNotified = true;
    }
}
