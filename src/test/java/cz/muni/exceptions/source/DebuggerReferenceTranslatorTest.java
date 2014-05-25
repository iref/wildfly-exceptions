package cz.muni.exceptions.source;

import com.google.common.base.Optional;
import com.sun.jdi.*;
import com.sun.jdi.event.ExceptionEvent;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jan Ferko
 */
public class DebuggerReferenceTranslatorTest {

    private DebuggerReferenceTranslator translator = new DebuggerReferenceTranslator();

    @Test(expected = IllegalArgumentException.class)
    public void testProcessNullExceptionEvent() {
        translator.processExceptionEvent(null);
    }

    @Test
    public void testProcessRootExceptionWithMessage() {
        Exception exception = new IllegalArgumentException("Something went wrong");
        ExceptionEvent mockEvent = mock(ExceptionEvent.class);
        ObjectReference exceptionReference = prepareExceptionReference(exception);
        when(mockEvent.exception()).thenReturn(exceptionReference);

        Optional<ExceptionReport> actualReport = translator.processExceptionEvent(mockEvent);

        compareExceptionAndReport(exception, actualReport.get());
    }

    @Test
    public void testProcessNestedException() {
        Exception root = new IllegalArgumentException("Something went wrong");
        Exception nested = new IllegalStateException("Something else went wrong", root);

        ExceptionEvent mockEvent = mock(ExceptionEvent.class);
        ObjectReference exceptionReference = prepareExceptionReference(nested);
        when(mockEvent.exception()).thenReturn(exceptionReference);

        Optional<ExceptionReport> actualReport = translator.processExceptionEvent(mockEvent);

        compareExceptionAndReport(nested, actualReport.get());
    }

    @Test
    public void testProcessExceptionWithoutMessage() {
        Exception root = new IllegalArgumentException();

        ExceptionEvent mockEvent = mock(ExceptionEvent.class);
        ObjectReference exceptionReference = prepareExceptionReference(root);
        when(mockEvent.exception()).thenReturn(exceptionReference);

        Optional<ExceptionReport> actualReport = translator.processExceptionEvent(mockEvent);

        compareExceptionAndReport(root, actualReport.get());
    }

    private ObjectReference prepareExceptionReference(Exception toCreate) {
        Deque<Throwable> exceptionStack = new LinkedList<>();
        exceptionStack.push(toCreate);

        ObjectReference rootExceptionReference = null;
        ObjectReference previous = null;
        while(!exceptionStack.isEmpty()) {
            Throwable toPrepare = exceptionStack.pop();

            ObjectReference exception = mock(ObjectReference.class);
            ReferenceType exceptionType = mock(ReferenceType.class);
            when(exception.referenceType()).thenReturn(exceptionType);

            exception = createStringReference(exception, "detailMessage", toPrepare.getMessage());

            Field stackTraceField = mock(Field.class);
            ArrayReference stackTraceValue = mock(ArrayReference.class);
            when(exceptionType.fieldByName("stackTrace")).thenReturn(stackTraceField);
            when(exception.getValue(stackTraceField)).thenReturn(stackTraceValue);

            List<Value> stackTrace = new ArrayList<>();
            for (StackTraceElement elem : toPrepare.getStackTrace()) {
                ObjectReference stackTraceElem1 = createStackTraceReference(elem.getMethodName(), elem.getClassName(),
                        elem.getFileName(), elem.getLineNumber());
                stackTrace.add(stackTraceElem1);
            }
            when(stackTraceValue.getValues()).thenReturn(stackTrace);

            if (toPrepare.getCause() != null) {
                exceptionStack.push(toPrepare.getCause());
            }

            if (rootExceptionReference == null) {
                rootExceptionReference = exception;
            } else {
                Field causeValue = mock(Field.class);
                when(previous.referenceType().fieldByName("cause")).thenReturn(causeValue);
                when(previous.getValue(causeValue)).thenReturn(exception);

            }
            previous = exception;
        }
        return rootExceptionReference;
    }

    private ObjectReference createStackTraceReference(String methodName, String className, String fileName,
            int lineNumber) {
        ObjectReference stackTraceElement = mock(ObjectReference.class);
        ReferenceType stackTraceType = mock(ReferenceType.class);
        when(stackTraceElement.referenceType()).thenReturn(stackTraceType);

        stackTraceElement = createStringReference(stackTraceElement, "methodName", methodName);
        stackTraceElement = createStringReference(stackTraceElement, "declaringClass", className);
        stackTraceElement = createStringReference(stackTraceElement, "fileName", fileName);

        Field lineNumberField = mock(Field.class);
        IntegerValue lineNumberValue = mock(IntegerValue.class);
        when(lineNumberValue.value()).thenReturn(lineNumber);
        when(stackTraceType.fieldByName("lineNumber")).thenReturn(lineNumberField);
        when(stackTraceElement.getValue(lineNumberField)).thenReturn(lineNumberValue);

        return stackTraceElement;
    }

    private ObjectReference createStringReference(ObjectReference parentObject, String fieldName, String value) {
        Field fileNameField = mock(Field.class);
        StringReference fileNameValue = mock(StringReference.class);
        when(fileNameValue.value()).thenReturn(value);
        when(parentObject.getValue(fileNameField)).thenReturn(fileNameValue);
        when(parentObject.referenceType().fieldByName(fieldName)).thenReturn(fileNameField);
        return parentObject;
    }

    private void compareExceptionAndReport(Throwable processed, ExceptionReport actualReport) {
        Assert.assertEquals(processed.getMessage(), actualReport.getMessage());
        Assert.assertEquals(processed.getStackTrace().length, actualReport.getStackTrace().size());

        for (int i = 0; i < processed.getStackTrace().length; i++) {
            StackTraceElement expected = processed.getStackTrace()[i];
            StackTraceElement actual = actualReport.getStackTrace().get(i);

            Assert.assertEquals(expected.getClassName(),  actual.getClassName());
            Assert.assertEquals(expected.getFileName(), actual.getFileName());
            Assert.assertEquals(expected.getMethodName(), actual.getMethodName());
            Assert.assertEquals(expected.getLineNumber(), actual.getLineNumber());
        }

        if (processed.getCause() != null) {
            Assert.assertNotNull(actualReport.getCause());
            compareExceptionAndReport(processed.getCause(), actualReport.getCause());
        } else {
            Assert.assertNull(actualReport.getCause());
        }
    }
}
