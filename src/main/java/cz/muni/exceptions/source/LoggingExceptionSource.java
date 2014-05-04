
package cz.muni.exceptions.source;

import com.google.common.base.Optional;
import cz.muni.exceptions.dispatcher.*;
import cz.muni.exceptions.listener.DatabaseExceptionListener;
import cz.muni.exceptions.listener.ExceptionListener;
import cz.muni.exceptions.listener.classifier.ExceptionReportClassifier;
import cz.muni.exceptions.listener.classifier.Node;
import cz.muni.exceptions.listener.classifier.PackageTreeSearcher;
import cz.muni.exceptions.listener.classifier.StaxPackageDataParser;
import cz.muni.exceptions.listener.db.JPATicketRepository;
import cz.muni.exceptions.listener.db.PersistenceUnitCreator;
import cz.muni.exceptions.listener.db.TicketRepository;
import cz.muni.exceptions.listener.duplication.LevenshteinSimilarityChecker;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.transaction.TransactionManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Jan Ferko 
 */
public class LoggingExceptionSource extends Handler {

    private static final String TRANSACTION_MANAGER_JNDI = "java:jboss/TransactionManager";
    
    private ExceptionDispatcher dispatcher;

    private boolean async = true;
    private boolean isJta = true;
    private String blacklist;
    private boolean databaseListenerEnabled;
    private String dataSourceJNDI;

    private BufferedWriter writer;

    public LoggingExceptionSource() {
    }

    public LoggingExceptionSource(ExceptionDispatcher dispatcher) {
        super();
        if (dispatcher == null) {
            throw new IllegalArgumentException("[Dispatcher] is required and must not be null.");
        }
        this.dispatcher = dispatcher;
    }

    private synchronized boolean initialize() throws IOException {
        if (dispatcher != null) {
            return true;
        }

        FileWriter fileWriter = new FileWriter("/tmp/logging.log", true);
        writer = new BufferedWriter(fileWriter);

        List<ExceptionListener> listeners = new ArrayList<>();
        if (databaseListenerEnabled) {
            listeners.add(createDatabaseListener());
        }

        List<String> blacklistItems = Arrays.asList(blacklist.split("."));
        ExceptionFilter blacklistFilter = new BlacklistFilter(blacklistItems);

        this.dispatcher = createDispatcher(listeners, blacklistFilter);

        return true;
    }



    @Override
    public void publish(LogRecord record) {
        try {
            if (!initialize()) {
                return;
            }
        } catch (IOException e) {
            // ok
        }
        if (record == null || !isLoggable(record)) {
            return;
        }
        
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            ExceptionReport report = createReport(thrown);
            dispatcher.warnListeners(report);
        }
    }

    @Override
    public void flush() {
        // nothing to do here right now!
    }

    @Override
    public void close() throws SecurityException {
        // switch handler off        
        setLevel(Level.OFF);
        // clear dispatcher for this source, so no more throwables are propagated
    }

    public void setAsync(String async) {
        this.async = parseBoolean(async, true);
    }

    public void setBlacklist(String blacklist) {
        this.blacklist = blacklist;
    }

    public void setDatabaseListenerEnabled(String databaseListenerEnabled) {
        this.databaseListenerEnabled = parseBoolean(databaseListenerEnabled, true);
    }

    public void setDataSourceJNDI(String dataSourceJNDI) {
        this.dataSourceJNDI = dataSourceJNDI;
    }

    public void setJta(String isJta) {
        this.isJta = parseBoolean(isJta, true);
    }

    private boolean  parseBoolean(String value, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(value);
        } catch (Exception ex) {
            // keep default;
        }
        return defaultValue;
    }

    private ExceptionDispatcher createDispatcher(List<ExceptionListener> listeners, ExceptionFilter blacklistFilter) {
        if (async) {
            AsyncExceptionDispatcher asyncDispatcher = new AsyncExceptionDispatcher(Executors.defaultThreadFactory(), blacklistFilter);
            for (ExceptionListener listener : listeners) {
                asyncDispatcher.registerListener(listener);
            }
            return asyncDispatcher;
        } else {
            return new BasicExceptionDispatcher(blacklistFilter, listeners);
        }
    }

    private ExceptionListener createDatabaseListener() throws IOException {
        StaxPackageDataParser packageDataParser = new StaxPackageDataParser();
        InputStream packageInput = getClass().getClassLoader().getResourceAsStream("data/packages.xml");
        Node packageTree = packageDataParser.parseInput(packageInput);
        PackageTreeSearcher searcher = new PackageTreeSearcher(packageTree);
        ExceptionReportClassifier classifier = new ExceptionReportClassifier(searcher);

        if (dataSourceJNDI == null) {
            throw new RuntimeException("Database Listener cannot be initialize if [dataSourceJNDI] property is not set.");
        }
        Optional<TransactionManager> transactionManager = Optional.absent();
        if (isJta) {
            try {
                writer.write("Getting JNDI context");
                writer.newLine();
                writer.flush();
                Context context = new InitialContext();

                writer.write("Looking for user transaction in JNDI");
                writer.newLine();
                writer.flush();
                TransactionManager txManager = (TransactionManager) context.lookup(TRANSACTION_MANAGER_JNDI);
                transactionManager = Optional.of(txManager);
            } catch (Exception ex) {
                writer.write("Exception while looking for transaction:"  + ex.toString());
                writer.newLine();
                writer.flush();
                throw new RuntimeException("JNDI lookup of TransactionManager has failed.");
            }
        }

        writer.write("Creating persistence unit creator");
        writer.newLine();
        writer.flush();
        PersistenceUnitCreator creator = new PersistenceUnitCreator(dataSourceJNDI, transactionManager);
        TicketRepository ticketRepository = new JPATicketRepository(creator);

        writer.write("database listener was created.");
        writer.newLine();
        writer.flush();
        return new DatabaseExceptionListener(ticketRepository, classifier, new LevenshteinSimilarityChecker());
    }

    /**
     * Method creates Exception report for given throwable.
     *
     * @param throwable throwable, which report should be created for
     * @return report for given throwable or {@code null} if throwable is {@code null}
     */
    private ExceptionReport createReport(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        ExceptionReport cause = createReport(throwable.getCause());
        List<StackTraceElement> stackTrace = Arrays.asList(throwable.getStackTrace());
        ExceptionReport report = new ExceptionReport(throwable.getClass().getCanonicalName(), throwable.getMessage(), stackTrace, cause);

        return report;
    }
}
