package cz.muni.exceptions.dispatcher;

import cz.muni.exceptions.MockListener;
import cz.muni.exceptions.listener.ExceptionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jan Ferko
 */
public class AsyncExceptionDispatcherTest {        
    
    private ThreadFactory threadFactory;
    
    private AsyncExceptionDispatcher dispatcher;
    
    @Before
    public void setUp() {
        threadFactory = Executors.defaultThreadFactory();
        dispatcher = new AsyncExceptionDispatcher(threadFactory);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullThreadFactory() {
        new AsyncExceptionDispatcher(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithListenersAndNullFactory() {
        new AsyncExceptionDispatcher(Collections.<ExceptionListener>emptyList(), null);
    }
    
    @Test
    public void testConstructorIgnoreNullListeners() {
        AsyncExceptionDispatcher dispatcher = new AsyncExceptionDispatcher(null, threadFactory);
        Assert.assertTrue(dispatcher.getListeners().isEmpty());
    }
    
    @Test
    public void testConstructorIgnoreEmptyListeners() {
        final List<ExceptionListener> noListeners = Collections.<ExceptionListener>emptyList();
        AsyncExceptionDispatcher dispatcher = new AsyncExceptionDispatcher(noListeners, threadFactory);
        Assert.assertTrue(dispatcher.getListeners().isEmpty());
    }
    
    @Test
    public void testConstructorIgnoreNullListener() {
        final List<ExceptionListener> listenersWithNullElement = 
                Arrays.<ExceptionListener>asList(new MockListener(), null);
        AsyncExceptionDispatcher dispatcher = new AsyncExceptionDispatcher(
                listenersWithNullElement, threadFactory);
        
        Assert.assertEquals(1, dispatcher.getListeners().size());
        ExceptionListener listener = dispatcher.getListeners().iterator().next();
        Assert.assertNotNull(listener);
    }
    
    @Test
    public void testRegisterNullListener() {
        dispatcher.registerListener(null);        
        Assert.assertTrue(dispatcher.getListeners().isEmpty());
    }
    
    @Test
    public void testRegisterListener() {
        MockListener listener = new MockListener();
        dispatcher.registerListener(listener);
        Assert.assertEquals(1, dispatcher.getListeners().size());
    }
    
    @Test
    public void testWarnListenersOnNullException() throws InterruptedException {
        MockListener listener = new MockListener();
        dispatcher.registerListener(null);
        dispatcher.warnListeners(new IllegalArgumentException());
        
        Thread.sleep(TimeUnit.SECONDS.toSeconds(1));
        
        Assert.assertFalse(listener.isNotified());
    }
    
    @Test
    public void testWarnListeners() throws InterruptedException {
        List<MockListener> listeners = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final MockListener mockListener = new MockListener();
            listeners.add(mockListener);
            dispatcher.registerListener(mockListener);
        }
        
        dispatcher.warnListeners(new IllegalArgumentException());
        
        Thread.sleep(TimeUnit.SECONDS.toSeconds(10));
        
        for (MockListener listener : listeners) {
            Assert.assertTrue(listener.isNotified());
        }
    }
    
    @Test
    public void testGetListeners() {
        Assert.assertTrue(dispatcher.getListeners().isEmpty());
        
        dispatcher.registerListener(new MockListener());
        
        Assert.assertEquals(1, dispatcher.getListeners().size());
    }
            
}
