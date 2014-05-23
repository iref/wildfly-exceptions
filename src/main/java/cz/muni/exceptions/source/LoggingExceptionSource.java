
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
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("/tmp/logging.log", true);
        } catch (IOException e) {
        }
        writer = new BufferedWriter(fileWriter);
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
        if (record == null || !isLoggable(record)) {
            return;
        }

        if (writer != null) {
            try {
                writer.write(record.getSourceClassName() + ": " + record.getThrown());
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Throwable thrown = record.getThrown();
        if (thrown == null) {
            return;
        }

        if (writer != null) {
            try {
            if (!initialize()) {
                    writer.write("Handler was not initialized.");
                    writer.newLine();
                    writer.flush();
                    return;
                }
            } catch (IOException e) {
                // ok
                e.printStackTrace(System.out);
            }
        }
        
        ExceptionReport report = createReport(thrown);
        if (writer != null) {
            try {
                writer.write("Reporting ticket. " + report);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
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
        try {
            if (writer != null) {
                writer.write("Reporting ticket");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private ExceptionListener createDatabaseListener() throws IOException {
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
            writer.write(ex.toString());
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
