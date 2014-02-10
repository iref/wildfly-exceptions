
package cz.muni.exceptions.dispatcher;

import cz.muni.exceptions.MockListener;
import cz.muni.exceptions.dispatcher.BasicExceptionDispatcher;
import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import cz.muni.exceptions.listener.ExceptionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jan Ferko 
 */
public class BasicExceptionDispatcherTest {
    
    private BasicExceptionDispatcher dispatcher;
    
    @Before
    public void setUp() {
        this.dispatcher = new BasicExceptionDispatcher();
    }
    
    @Test
    public void testConstructorIgnoresNullAndEmptyListeners() {
        BasicExceptionDispatcher nullListeners = new BasicExceptionDispatcher(null);
        assertTrue(nullListeners.getListeners().isEmpty());
        
        final List<ExceptionListener> emptyListenersList = Collections.<ExceptionListener>emptyList();        
        BasicExceptionDispatcher emptyListeners = new BasicExceptionDispatcher(emptyListenersList);
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
    public void testWarnEveryListeners() {
        List<ExceptionListener> listeners = createMockListeners();
        
        ExceptionDispatcher newSource = new BasicExceptionDispatcher(listeners);
        newSource.warnListeners(new IllegalArgumentException());
        
        for (ExceptionListener listener : dispatcher.getListeners()) {
            MockListener mockListener = (MockListener) listener;
            assertTrue(mockListener.isNotified());
        }
    }
    
    @Test
    public void testWarnIgnoresNullThrowables() {
        List<ExceptionListener> listeners = createMockListeners();
        
        ExceptionDispatcher newSource = new BasicExceptionDispatcher(listeners);
        newSource.warnListeners(null);
        
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
