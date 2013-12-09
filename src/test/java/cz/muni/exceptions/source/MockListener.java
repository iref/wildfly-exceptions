
package cz.muni.exceptions.source;

import cz.muni.exceptions.listener.ExceptionListener;

/**
 *
 * @author Jan Ferko
 * @sa.date 2013-12-09T13:46:23+0100
 */
public class MockListener implements ExceptionListener {
    boolean wasNotified = false;
        
    @Override
    public void onThrownException(Throwable throwable) {
        wasNotified = true;
    }
}
