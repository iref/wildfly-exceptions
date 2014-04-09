package cz.muni.exceptions.source;

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
    
    private static final Logger LOGGER = Logger.getLogger(DebuggerExceptionSource.class.getSimpleName());
    
    public ExceptionReport processExceptionEvent(ExceptionEvent exceptionEvent) {
        if (exceptionEvent == null) {
            throw new IllegalArgumentException("[Event] should not be null");
        }

        ObjectReference exception = exceptionEvent.exception();                

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.log(Level.INFO, exception.referenceType().name());
        }
        
        return processObjectReference(exception);
    }
    
    private ExceptionReport processObjectReference(ObjectReference exception) {
        String detailMessage = getDetailMessage(exception);
        List<StackTraceElement> stackTrace = getStackTrace(exception);
        ExceptionReport cause = getCause(exception);
        
        return new ExceptionReport(detailMessage, stackTrace, cause);
    }

    private ExceptionReport getCause(ObjectReference exception) {
        Field causeField = exception.referenceType().fieldByName("cause");
        ObjectReference causeValue = (ObjectReference) exception.getValue(causeField);
        LOGGER.log(Level.INFO, "Exception cause: {0}", causeValue);
        
        return causeValue != null && !"null".equals(causeValue.referenceType().name()) ?
            processObjectReference(causeValue) : null;
    }

    private List<StackTraceElement> getStackTrace(ObjectReference exception) {
        Field stackFramesField = exception.referenceType().fieldByName("stackTrace");
        ArrayReference stackTrace = (ArrayReference) exception.getValue(stackFramesField);
        LOGGER.log(Level.INFO, "Stacktraces: {0}", stackTrace.getValues());
        
        List<StackTraceElement> stackTraceElements = new ArrayList<>();
        
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
          
        Field field = object.referenceType().fieldByName(fieldName);
        if (field == null) {
            return null;
        }
        StringReference fieldValue = (StringReference) object.getValue(field);
        return fieldValue.value();
    }
}
