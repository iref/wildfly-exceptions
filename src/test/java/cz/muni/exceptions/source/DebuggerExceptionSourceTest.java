package cz.muni.exceptions.source;

import cz.muni.exceptions.MockListener;
import cz.muni.exceptions.dispatcher.BasicExceptionDispatcher;
import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import cz.muni.exceptions.listener.ExceptionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Jan Ferko 
 */
public class DebuggerExceptionSourceTest {        
    
    private ExceptionDispatcher mockDispatcher;
    
    private DebuggerExceptionSource source;
    
    @Before
    public void setUp() throws InterruptedException {
        System.out.println("Setting Up Tests");
        mockDispatcher = new BasicExceptionDispatcher();        
        mockDispatcher.registerListener(new MockListener());
        
        source = new DebuggerExceptionSource(mockDispatcher, new DebuggerReferenceTranslator());
        source.start();
        
        Thread.sleep(1000L);        
   }
    
   @After
   public void tearDown() {
       source.stop();
   }
    
   @Test
   public void testFireException() {
       Logger.getAnonymousLogger().log(Level.INFO, "Test thread id:{0}", Thread.currentThread().getId());
       try {
        throwException();
       } catch (IllegalArgumentException ex) {
           // ok
       }
       try {
           Thread.sleep(5000L);
       } catch (InterruptedException exc) {
           Logger.getLogger(DebuggerExceptionSourceTest.class.getName()).log(Level.SEVERE, null, exc);
       }
       Logger.getLogger(getClass().getSimpleName()).info("[IllegalArgumentException] was thrown.");
       for (ExceptionListener listener : mockDispatcher.getListeners()) {
           MockListener mock = (MockListener) listener;
           Assert.assertTrue("MockListener was not notified",mock.isNotified());
       }                       
   }
   
   private void throwException() {
       throw new IllegalArgumentException("Test exception");
   }
}
