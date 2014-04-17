
package cz.muni.exceptions.source;

import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import java.util.Arrays;
import java.util.List;
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
            ExceptionReport report = createReport(thrown);
            dispatcher.warnListeners(report);
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
    
    /**
     * Method creates Exception report for given throwable.
     * 
     * @param throwable throwable, which report should be created for
     * @return report for given throwable or {@code null} if throwable is {@code null}
     */
    private ExceptionReport createReport(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        
        ExceptionReport cause = createReport(throwable.getCause());
        List<StackTraceElement> stackTrace = Arrays.asList(throwable.getStackTrace());
        ExceptionReport report = new ExceptionReport(throwable.getClass().getCanonicalName(), throwable.getMessage(), stackTrace, cause);
        
        return report;
    }
                
}
