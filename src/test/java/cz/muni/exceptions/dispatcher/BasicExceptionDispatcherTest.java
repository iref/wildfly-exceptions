
package cz.muni.exceptions.dispatcher;

import cz.muni.exceptions.MockListener;
import cz.muni.exceptions.listener.ExceptionListener;
import cz.muni.exceptions.source.ExceptionReport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jan Ferko 
 */
public class BasicExceptionDispatcherTest {
    
    private BasicExceptionDispatcher dispatcher;
    
    private ExceptionReport mockReport = new ExceptionReport("java.lang.IllegalArgumentException",
            "Something terrible happened", Collections.<StackTraceElement>emptyList(), null);
    
    @Before
    public void setUp() {
        this.dispatcher = new BasicExceptionDispatcher(null);
    }
    
    @Test
    public void testConstructorIgnoresNullAndEmptyListeners() {
        BasicExceptionDispatcher nullListeners = new BasicExceptionDispatcher(null);
        assertTrue(nullListeners.getListeners().isEmpty());
        
        final List<ExceptionListener> emptyListenersList = Collections.<ExceptionListener>emptyList();        
        BasicExceptionDispatcher emptyListeners = new BasicExceptionDispatcher(null, emptyListenersList);
        assertTrue(emptyListeners.getListeners().isEmpty());
    }
    
    @Test
    public void testRegisterListener() {
        ExceptionListener mockListener = new MockListener();
        assertFalse(dispatcher.getListeners().contains(mockListener));
        dispatcher.registerListener(mockListener);
        
        final Set<ExceptionListener> actual = dispatcher.getListeners();        
        assertEquals(1, actual.size());
        assertTrue(actual.contains(mockListener));        
    }
    
    @Test
    public void testRegisterNullListener() {
        dispatcher.registerListener(null);        
        assertTrue(dispatcher.getListeners().isEmpty());
    }

    @Test
    public void testUnregisterNullListener() {
        dispatcher.registerListener(new MockListener());
        int size = dispatcher.getListeners().size();
        dispatcher.unregisterListener(null);
        Assert.assertEquals(size, dispatcher.getListeners().size());
    }

    @Test
    public void testUnregisterListener() {
        MockListener listener = new MockListener();
        dispatcher.registerListener(listener);

        dispatcher.unregisterListener(listener);
        Assert.assertFalse(dispatcher.getListeners().contains(listener));
    }

    @Test
    public void testUnregisterNonExistingListener() {
        dispatcher.registerListener(new MockListener());

        int size = dispatcher.getListeners().size();

        MockListener nonRegisteredListener = new MockListener();
        dispatcher.unregisterListener(nonRegisteredListener);
        Assert.assertEquals(size, dispatcher.getListeners().size());
    }
    
    @Test
    public void testWarnEveryListeners() {
        List<ExceptionListener> listeners = createMockListeners();
        
        ExceptionDispatcher newSource = new BasicExceptionDispatcher(null, listeners);
        
        newSource.warnListeners(mockReport);
        
        for (ExceptionListener listener : dispatcher.getListeners()) {
            MockListener mockListener = (MockListener) listener;
            assertTrue(mockListener.isNotified());
        }
    }
    
    @Test
    public void testWarnIgnoresNullThrowables() {
        List<ExceptionListener> listeners = createMockListeners();
        
        ExceptionDispatcher newSource = new BasicExceptionDispatcher(null, listeners);
        newSource.warnListeners(null);
        
        for (ExceptionListener listener : dispatcher.getListeners()) {
            MockListener mockListener = (MockListener) listener;
            assertFalse(mockListener.isNotified());
        }
    }

    @Test
    public void testWarnIgnoresFilteredExceptions() {
        List<ExceptionListener> listeners = createMockListeners();

        ExceptionFilter exceptionFilter = new ExceptionFilter() {
            @Override
            public boolean apply(ExceptionReport exceptionReport) {
                return false;
            }
        };

        ExceptionDispatcher newDispatcher = new BasicExceptionDispatcher(exceptionFilter, listeners);
        newDispatcher.warnListeners(mockReport);
        for (ExceptionListener listener : dispatcher.getListeners()) {
            MockListener mockListener = (MockListener) listener;
            assertFalse(mockListener.isNotified());
        }
    }
    
    @Test
    public void testGetListeners() {
        assertTrue(dispatcher.getListeners().isEmpty());
        dispatcher.registerListener(new MockListener());
        
        final Set<ExceptionListener> actual = dispatcher.getListeners();        
        assertFalse(actual.isEmpty());
        assertNotSame(actual, dispatcher.getListeners());
    }

    private List<ExceptionListener> createMockListeners() {
        List<ExceptionListener> listeners = new ArrayList<ExceptionListener>();
        for (int i = 0; i < 3; i++) {
            listeners.add(new MockListener());
        }
        
        return listeners;
    }    

}
