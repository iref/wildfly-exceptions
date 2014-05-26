
package cz.muni.exceptions.source;

import cz.muni.exceptions.dispatcher.*;
import cz.muni.exceptions.listener.DatabaseExceptionListener;
import cz.muni.exceptions.listener.ExceptionListener;
import cz.muni.exceptions.listener.classifier.ExceptionReportClassifier;
import cz.muni.exceptions.listener.classifier.Node;
import cz.muni.exceptions.listener.classifier.PackageTreeSearcher;
import cz.muni.exceptions.listener.classifier.StaxPackageDataParser;
import cz.muni.exceptions.listener.db.TicketRepository;
import cz.muni.exceptions.listener.db.TicketRepositoryFactory;
import cz.muni.exceptions.listener.duplication.LevenshteinSimilarityChecker;

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
 * Implements exception source as Logging handler.
 *
 * @author Jan Ferko 
 */
public class LoggingExceptionSource extends Handler {

    /** Dispatcher, that handles exception processing. */
    private ExceptionDispatcher dispatcher;

    /** Indicator if exceptions should be processed asynchronously. */
    private boolean async = true;

    /** Indicator if provided data source uses JTA.*/
    private boolean isJta = true;

    /** String containing comma separated list of ignored classes */
    private String blacklist;

    /** Indicator if database listener is enabled. */
    private boolean databaseListenerEnabled;

    /** JNDI name of data source, where exceptions should be stored. */
    private String dataSourceJNDI;

    public LoggingExceptionSource() {
    }

    /**
     * Creates new instance, that uses given dispatcher to handle caught exceptions.
     *
     * @param dispatcher dispatcher to handle caught exceptions
     * @throws java.lang.IllegalArgumentException if dispatcher is {@code null}
     */
    public LoggingExceptionSource(ExceptionDispatcher dispatcher) {
        super();
        if (dispatcher == null) {
            throw new IllegalArgumentException("[Dispatcher] is required and must not be null.");
        }
        this.dispatcher = dispatcher;
    }

    /**
     * Initializes handler.
     * If dispatcher is already created, it does not create new one.
     * Otherwise creates new dispatcher based on current configuration.
     */
    private synchronized void initialize() {
        if (dispatcher != null) {
            return;
        }

        List<ExceptionListener> listeners = new ArrayList<>();
        if (databaseListenerEnabled) {
            listeners.add(createDatabaseListener());
        }

        List<String> blacklistItems = Arrays.asList(blacklist.split("."));
        ExceptionFilter blacklistFilter = new BlacklistFilter(blacklistItems);

        this.dispatcher = createDispatcher(listeners, blacklistFilter);
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null || !isLoggable(record)) {
            return;
        }

        Throwable thrown = record.getThrown();
        if (thrown == null) {
            return;
        }
        initialize();

        ExceptionReport report = createReport(thrown);
        
        dispatcher.warnListeners(report);        
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

    /**
     * Creates new dispatcher instance and registers listeners.
     *
     * @param listeners list of listeners, that should be registered
     * @param blacklistFilter filter, that should be used by dispatcher to decide which exceptions should be processed.
     * @return new instance of dispatcher
     */
    private ExceptionDispatcher createDispatcher(List<ExceptionListener> listeners, ExceptionFilter blacklistFilter) {
        if (async) {
            AsyncExceptionDispatcher asyncDispatcher = new AsyncExceptionDispatcher(
                    Executors.newSingleThreadExecutor(), blacklistFilter);
            asyncDispatcher.start();
            for (ExceptionListener listener : listeners) {
                asyncDispatcher.registerListener(listener);
            }
            return asyncDispatcher;
        } else {
            return new BasicExceptionDispatcher(blacklistFilter, listeners);
        }
    }

    /**
     * Creates new instance of {@link cz.muni.exceptions.listener.DatabaseExceptionListener}
     *
     * @return new listener
     */
    private ExceptionListener createDatabaseListener() {
        StaxPackageDataParser packageDataParser = new StaxPackageDataParser();
        InputStream packageInput = getClass().getClassLoader().getResourceAsStream("data/packages.xml");
        Node packageTree = packageDataParser.parseInput(packageInput);
        PackageTreeSearcher searcher = new PackageTreeSearcher(packageTree);
        ExceptionReportClassifier classifier = new ExceptionReportClassifier(searcher);

        if (dataSourceJNDI == null) {
            throw new RuntimeException("Database Listener cannot be initialize if [dataSourceJNDI] property is not set.");
        }

        TicketRepository ticketRepository = null;
        try {
            ticketRepository = TicketRepositoryFactory.newInstance(dataSourceJNDI, isJta);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

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
