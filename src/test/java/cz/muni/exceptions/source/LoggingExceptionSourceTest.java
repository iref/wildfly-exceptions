
package cz.muni.exceptions.source;

import com.google.common.base.Optional;
import cz.muni.exceptions.MockListener;
import cz.muni.exceptions.dispatcher.BasicExceptionDispatcher;
import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import cz.muni.exceptions.listener.DatabaseExceptionListener;
import cz.muni.exceptions.listener.ExceptionListener;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import cz.muni.exceptions.listener.classifier.ExceptionReportClassifier;
import cz.muni.exceptions.listener.db.JPATicketRepository;
import cz.muni.exceptions.listener.db.PersistenceUnitCreator;
import cz.muni.exceptions.listener.db.model.Ticket;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.security.RunAs;
import javax.transaction.UserTransaction;

/**
 *
 * @author Jan Ferko 
 */
public class LoggingExceptionSourceTest {
    
    private LoggingExceptionSource loggingSource;
    private BasicExceptionDispatcher mockDispatcher;
    
    @Before
    public void setUp() {
        MockListener listener = new MockListener();
        this.mockDispatcher = new BasicExceptionDispatcher(null,
                Arrays.<ExceptionListener>asList(listener));
        this.loggingSource = new LoggingExceptionSource(mockDispatcher);
    }
    
    @Test
    public void testPublishNullRecord() {
        this.loggingSource.publish(null);
        
        assertReported(false);
    }
    
    @Test
    public void testPublishLowerLevelRecord() {
        this.loggingSource.setLevel(Level.SEVERE);
        LogRecord infoRecord = new LogRecord(Level.INFO, "Info level log record");
        infoRecord.setThrown(new IllegalArgumentException("This exception should not be reported"));
        
        this.loggingSource.publish(infoRecord);
        
        assertReported(false);
    }
    
    @Test
    public void testPublishHigherLevelRecord() {
        this.loggingSource.setLevel(Level.FINE);
        LogRecord finerRecord = new LogRecord(Level.INFO, "Finer level log record");
        finerRecord.setThrown(new IllegalArgumentException("This exception should be reported"));
        
        this.loggingSource.publish(finerRecord);
        
        assertReported(true);
    }
    
    @Test
    public void testPublishRecordWithoutThrowable() {
        LogRecord record = new LogRecord(Level.INFO, "Info log record");
        
        this.loggingSource.publish(record);
        
        assertReported(false);
    }
    
    @Test
    public void testPublishRecordWithCause() {
        IllegalArgumentException nested = new IllegalArgumentException();
        RuntimeException actual = new RuntimeException("Something terrible happened", nested);
        
        LogRecord finerRecord = new LogRecord(Level.FINER, "Runtime test error");
        finerRecord.setThrown(actual);
        
        this.loggingSource.publish(finerRecord);
        
        assertReported(true);
    }

    private void assertReported(boolean shouldBeReported) {
        for (ExceptionListener listener : mockDispatcher.getListeners()) {
            MockListener mockListener = (MockListener) listener;
            Assert.assertEquals(shouldBeReported, mockListener.isNotified());
        }
    }

}
