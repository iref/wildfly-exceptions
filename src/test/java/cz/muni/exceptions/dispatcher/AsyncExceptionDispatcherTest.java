package cz.muni.exceptions.dispatcher;

import cz.muni.exceptions.MockListener;
import cz.muni.exceptions.listener.ExceptionListener;
import cz.muni.exceptions.source.ExceptionReport;
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

import static org.junit.Assert.assertFalse;

/**
 *
 * @author Jan Ferko
 */
public class AsyncExceptionDispatcherTest {        
    
    private ThreadFactory threadFactory;
    
    private AsyncExceptionDispatcher dispatcher;
    
    private ExceptionReport mockReport = new ExceptionReport("java.lang.IllegalArgumentException",
            "Something terrible happened", Collections.<StackTraceElement>emptyList(), null);
    
    @Before
    public void setUp() {
        threadFactory = Executors.defaultThreadFactory();
        dispatcher = new AsyncExceptionDispatcher(threadFactory, ExceptionFilters.ALWAYS_PASSES);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullThreadFactory() {
        new AsyncExceptionDispatcher(null, ExceptionFilters.ALWAYS_PASSES);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithListenersAndNullFactory() {
        new AsyncExceptionDispatcher(null, ExceptionFilters.ALWAYS_PASSES, Collections.<ExceptionListener>emptyList());
    }
    
    @Test
    public void testConstructorIgnoreNullListeners() {
        AsyncExceptionDispatcher dispatcher = new AsyncExceptionDispatcher(threadFactory, null);
        Assert.assertTrue(dispatcher.getListeners().isEmpty());
    }
    
    @Test
    public void testConstructorIgnoreEmptyListeners() {
        final List<ExceptionListener> noListeners = Collections.<ExceptionListener>emptyList();
        AsyncExceptionDispatcher dispatcher = new AsyncExceptionDispatcher(threadFactory, ExceptionFilters.ALWAYS_PASSES, noListeners);
        Assert.assertTrue(dispatcher.getListeners().isEmpty());
    }
    
    @Test
    public void testConstructorIgnoreNullListener() {
        final List<ExceptionListener> listenersWithNullElement = 
                Arrays.<ExceptionListener>asList(new MockListener(), null);
        AsyncExceptionDispatcher dispatcher = new AsyncExceptionDispatcher(threadFactory, ExceptionFilters.ALWAYS_PASSES,
                listenersWithNullElement);
        
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
    public void testWarnListenersOnNullException() throws InterruptedException {
        MockListener listener = new MockListener();
        dispatcher.registerListener(listener);        
        dispatcher.warnListeners(null);
        
        Thread.sleep(TimeUnit.SECONDS.toSeconds(1));
        
        Assert.assertFalse(listener.isNotified());
    }

    @Test
    public void testWarnIgnoresFilteredExceptions() {
        List<ExceptionListener> listeners = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final MockListener mockListener = new MockListener();
            listeners.add(mockListener);
        }

        ExceptionFilter exceptionFilter = ExceptionFilters.ALWAYS_FILTERED;
        AsyncExceptionDispatcher newDispatcher = new AsyncExceptionDispatcher(threadFactory, exceptionFilter, listeners);

        newDispatcher.warnListeners(mockReport);
        for (ExceptionListener listener : listeners) {
            MockListener mockListener = (MockListener) listener;
            assertFalse(mockListener.isNotified());
        }
    }
    
    @Test
    public void testWarnListeners() throws InterruptedException {
        List<MockListener> listeners = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final MockListener mockListener = new MockListener();
            listeners.add(mockListener);
            dispatcher.registerListener(mockListener);
        }
        
        dispatcher.warnListeners(mockReport);
        
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
