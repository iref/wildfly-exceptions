
package cz.muni.exceptions.source;

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
public class BasicExceptionSourceTest {
    
    private BasicExceptionSource source;
    
    @Before
    public void setUp() {
        this.source = new BasicExceptionSource();
    }
    
    @Test
    public void testConstructorIgnoresNullAndEmptyListeners() {
        BasicExceptionSource nullListeners = new BasicExceptionSource(null);
        assertTrue(nullListeners.getListeners().isEmpty());
        
        final List<ExceptionListener> emptyListenersList = Collections.<ExceptionListener>emptyList();        
        BasicExceptionSource emptyListeners = new BasicExceptionSource(emptyListenersList);
        assertTrue(emptyListeners.getListeners().isEmpty());
    }
    
    @Test
    public void testRegisterListener() {
        ExceptionListener mockListener = new MockListener();
        assertFalse(source.getListeners().contains(mockListener));
        source.registerListener(mockListener);
        
        final Set<ExceptionListener> actual = source.getListeners();        
        assertEquals(1, actual.size());
        assertTrue(actual.contains(mockListener));        
    }
    
    @Test
    public void testRegisterNullListener() {
        source.registerListener(null);        
        assertTrue(source.getListeners().isEmpty());
    }
    
    @Test
    public void testWarnEveryListeners() {
        List<ExceptionListener> listeners = createMockListeners();
        
        ExceptionSource newSource = new BasicExceptionSource(listeners);
        newSource.warnListeners(new IllegalArgumentException());
        
        for (ExceptionListener listener : source.getListeners()) {
            MockListener mockListener = (MockListener) listener;
            assertTrue(mockListener.wasNotified);
        }
    }
    
    @Test
    public void testWarnIgnoresNullThrowables() {
        List<ExceptionListener> listeners = createMockListeners();
        
        ExceptionSource newSource = new BasicExceptionSource(listeners);
        newSource.warnListeners(null);
        
        for (ExceptionListener listener : source.getListeners()) {
            MockListener mockListener = (MockListener) listener;
            assertFalse(mockListener.wasNotified);
        }
    }
    
    @Test
    public void testGetListeners() {
        assertTrue(source.getListeners().isEmpty());
        source.registerListener(new MockListener());
        
        final Set<ExceptionListener> actual = source.getListeners();        
        assertFalse(actual.isEmpty());
        assertNotSame(actual, source.getListeners());
    }

    private List<ExceptionListener> createMockListeners() {
        List<ExceptionListener> listeners = new ArrayList<ExceptionListener>();
        for (int i = 0; i < 3; i++) {
            listeners.add(new MockListener());
        }
        
        return listeners;
    }
    
    private static class MockListener implements ExceptionListener {

        boolean wasNotified = false;
        
        @Override
        public void onThrownException(Throwable throwable) {
            wasNotified = true;
        }        
    }

}
