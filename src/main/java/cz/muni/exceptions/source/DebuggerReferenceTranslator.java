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
 *
 * @author johnny
 */
public class DebuggerReferenceTranslator {
    
    private static final Logger LOGGER = Logger.getLogger(DebuggerReferenceTranslator.class.getSimpleName());
    
    public Optional<ExceptionReport> processExceptionEvent(ExceptionEvent exceptionEvent) {
        if (exceptionEvent == null) {
            throw new IllegalArgumentException("[Event] should not be null");
        }

        Optional<ExceptionReport> result = Optional.absent();

        ObjectReference exception = exceptionEvent.exception();

        result = Optional.fromNullable(processObjectReference(exception));
        return result;
    }
    
    private ExceptionReport processObjectReference(ObjectReference exception) {
        String detailMessage = getDetailMessage(exception);        
        List<StackTraceElement> stackTrace = getStackTrace(exception);
        String exceptionClass = exception.referenceType().name();
        ExceptionReport cause = getCause(exception);
        
        return new ExceptionReport(exceptionClass, detailMessage, stackTrace, cause);
    }

    private ExceptionReport getCause(ObjectReference exception) {
        Field causeField = exception.referenceType().fieldByName("cause");
        ObjectReference causeValue = (ObjectReference) exception.getValue(causeField);
        if (causeValue != null && !exception.equals(causeValue)) {
            return processObjectReference(causeValue);
        }

        return null;
    }

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

    private String getDetailMessage(ObjectReference exception) {
        final String detailMessage = getString(exception, "detailMessage");
        return detailMessage;
    }    
    
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
