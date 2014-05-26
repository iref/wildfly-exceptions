package cz.muni.exceptions.source;

import com.google.common.base.Optional;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import com.sun.jdi.event.ExceptionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class translates exception events into reports.
 *
 * @author Jan Ferko
 */
public class DebuggerReferenceTranslator {
    
    private static final Logger LOGGER = Logger.getLogger(DebuggerReferenceTranslator.class.getSimpleName());

    /**
     * Creates new report for given event.
     *
     * @param exceptionEvent event with exception, that should be used to create report
     * @return option with created report or empty option if its not possible to create report
     * @throws java.lang.IllegalArgumentException if event is {@code null}
     */
    public Optional<ExceptionReport> processExceptionEvent(ExceptionEvent exceptionEvent) {
        if (exceptionEvent == null) {
            throw new IllegalArgumentException("[Event] should not be null");
        }

        ObjectReference exception = exceptionEvent.exception();

        return Optional.fromNullable(processObjectReference(exception));
    }

    /**
     * Creates report from given object reference.
     *
     * @param exception reference to exception object on target VM.
     * @return new exception report
     */
    private ExceptionReport processObjectReference(ObjectReference exception) {
        String detailMessage = getDetailMessage(exception);        
        List<StackTraceElement> stackTrace = getStackTrace(exception);
        String exceptionClass = exception.referenceType().name();
        ExceptionReport cause = getCause(exception);
        
        return new ExceptionReport(exceptionClass, detailMessage, stackTrace, cause);
    }

    /**
     * Creates new report for cause of exception.
     *
     * @param exception exception reference, which cause should be created.
     * @return report of exception cause or {@code null} if exception doesn't have cause or its cause is itself
     */
    private ExceptionReport getCause(ObjectReference exception) {
        Field causeField = exception.referenceType().fieldByName("cause");
        ObjectReference causeValue = (ObjectReference) exception.getValue(causeField);
        if (causeValue != null && !exception.equals(causeValue)) {
            return processObjectReference(causeValue);
        }

        return null;
    }

    /**
     * Creates list of stack trace elements of given exception.
     *
     * @param exception exception, which stack trace should be created
     * @return list of exception's stack trace elements or empty list if
     *  it is not possible to access stack trace on target VM
     */
    private List<StackTraceElement> getStackTrace(ObjectReference exception) {
        List<StackTraceElement> stackTraceElements = new ArrayList<>();

        Field stackFramesField = exception.referenceType().fieldByName("stackTrace");
        ArrayReference stackTrace = (ArrayReference) exception.getValue(stackFramesField);
        if (stackTrace == null) {
            return stackTraceElements;
        }

        
        for (Value v : stackTrace.getValues()) {
            ObjectReference stackTraceReference = (ObjectReference) v;            
            
            String declaringClass = getString(stackTraceReference, "declaringClass");                        
            String methodName = getString(stackTraceReference, "methodName");                        
            String fileName = getString(stackTraceReference, "fileName");            
            
            Field lineNumberField = stackTraceReference.referenceType().fieldByName("lineNumber");
            IntegerValue lineNumber = (IntegerValue) stackTraceReference.getValue(lineNumberField);
            
            final StackTraceElement newStackTrace = new StackTraceElement(
                    declaringClass, methodName, 
                    fileName, lineNumber.value());            
            stackTraceElements.add(newStackTrace);
        }        
        
        return stackTraceElements;
    }

    /**
     * Creates detail message of exception.
     *
     * @param exception exception, which detail message should be created
     * @return detail message of exception
     */
    private String getDetailMessage(ObjectReference exception) {
        final String detailMessage = getString(exception, "detailMessage");
        return detailMessage;
    }

    /**
     * Gets string value for field with given {@code fieldName} from object reference.
     *
     * @param object reference of object on target VM
     * @param fieldName name of field
     * @return string value of field in given object.
     */
    private String getString(ObjectReference object, String fieldName) {
        if (object == null || fieldName == null || fieldName.isEmpty()) {
            return null;
        }
        LOGGER.log(Level.FINE, "Parsing string for field {0}", fieldName);
        Field field = object.referenceType().fieldByName(fieldName);        
        if (field == null) {
            return null;
        }

        StringReference fieldValue = (StringReference) object.getValue(field);
        LOGGER.log(Level.FINE, "String field value: {0}", fieldValue);
        return fieldValue == null ? null : fieldValue.value();
    }
}
