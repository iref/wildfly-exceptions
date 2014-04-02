package cz.muni.exceptions.source;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author johnny
 */
public class ExceptionReport {
    
    private final String message;
    
    private final List<StackTraceElement> stackTrace;
    
    private final ExceptionReport cause;
    
    public ExceptionReport(String message, List<StackTraceElement> stackTrace, ExceptionReport cause) {
        this.message = message;
        this.stackTrace = stackTrace;
        this.cause = cause;
    }

    public String getMessage() {
        return message;
    }

    public List<StackTraceElement> getStackTrace() {
        return stackTrace;
    }

    public ExceptionReport getCause() {
        return cause;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExceptionReport)) {
            return false;
        }
        
        ExceptionReport other = (ExceptionReport) obj;
        return Objects.equals(message, other.message) 
                && Objects.equals(stackTrace, other.stackTrace)
                && Objects.equals(cause, other.cause);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.message);
        hash = 11 * hash + Objects.hashCode(this.stackTrace);
        hash = 11 * hash + Objects.hashCode(this.cause);
        return hash;
    }        
    
}
