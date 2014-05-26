package cz.muni.exceptions.source;

import java.util.List;
import java.util.Objects;

/**
 * Class, that represents exception produced by one of the sources.
 *
 * @author Jan Ferko
 */
public class ExceptionReport {

    /** Detail message of exception. */
    private final String message;

    /** Class of exception. */
    private final String exceptionClass;

    /** List of stack traces. */
    private final List<StackTraceElement> stackTrace;

    /** Cause of this exception. */
    private final ExceptionReport cause;

    /**
     * Creates new instance of report.
     *
     * @param exceptionClass fully qualified name of exceptions
     * @param message message describing cause of exceptions
     * @param stackTrace list of stack trace elements
     * @param cause exception that caused this exception
     */
    public ExceptionReport(String exceptionClass, String message, List<StackTraceElement> stackTrace, ExceptionReport cause) {
        this.message = message;
        this.stackTrace = stackTrace;
        this.cause = cause;
        this.exceptionClass = exceptionClass;
    }

    /**
     * Returns fully classified name of exception.
     *
     * @return fully qualified name of exception
     */
    public String getExceptionClass() {
        return exceptionClass;
    }

    /**
     * Returns detail message of exception.
     *
     * @return detail message of exception
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns list of exception stack trace elements
     * @return immutable list of exceptions stack trace elements
     */
    public List<StackTraceElement> getStackTrace() {
        return stackTrace;
    }

    /**
     * Returns report of exception, that caused this one.
     *
     * @return cause of exceptions
     */
    public ExceptionReport getCause() {
        return cause;
    }

    /**
     * Compares instance with given object on equality based on following attributes:
     * <ul>
     *     <li>{@link #getExceptionClass()}</li>
     *     <li>{@link #getMessage()}</li>
     *     <li>{@link #getStackTrace()}</li>
     *     <li>{@link #getCause()}</li>
     * </ul>
     * @param obj object, that should be compared with instance
     * @return {@code true} if object is equal to this instance, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ExceptionReport)) {
            return false;
        }
        
        ExceptionReport other = (ExceptionReport) obj;
        return Objects.equals(exceptionClass, other.exceptionClass)
                && Objects.equals(message, other.message)
                && Objects.equals(stackTrace, other.stackTrace)
                && Objects.equals(cause, other.cause);
    }

    /**
     * Computes hash code of instance based on following attributes:
     * <ul>
     *     <li>{@link #getExceptionClass()}</li>
     *     <li>{@link #getMessage()}</li>
     *     <li>{@link #getStackTrace()}</li>
     *     <li>{@link #getCause()}</li>
     * </ul>
     * @return hash code of instance.
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.exceptionClass);
        hash = 11 * hash + Objects.hashCode(this.message);
        hash = 11 * hash + Objects.hashCode(this.stackTrace);
        hash = 11 * hash + Objects.hashCode(this.cause);
        return hash;
    }        
    
}
