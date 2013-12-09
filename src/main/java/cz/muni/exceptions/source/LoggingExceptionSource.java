
package cz.muni.exceptions.source;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Jan Ferko 
 */
public class LoggingExceptionSource extends Handler {
    
    private final ExceptionDispatcher dispatcher;

    public LoggingExceptionSource(ExceptionDispatcher dispatcher) {
        if (dispatcher == null) {
            throw new IllegalArgumentException("[Dispatcher] is required and must not be null.");
        }
        this.dispatcher = dispatcher;
    }        

    @Override
    public void publish(LogRecord record) {
        if (record == null || !isLoggable(record)) {
            return;
        }
        
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            dispatcher.warnListeners(thrown);
        }
    }

    @Override
    public void flush() {
        // nothing to do here right now!
    }

    @Override
    public void close() throws SecurityException {
        // switch handler off        
        setLevel(Level.OFF);
        // clear dispatcher for this source, so no more throwables are propagated
    }
                
}
