package cz.muni.exceptions.service;

import com.google.common.base.Optional;
import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import cz.muni.exceptions.listener.DatabaseExceptionListener;
import cz.muni.exceptions.listener.classifier.ExceptionReportClassifier;
import cz.muni.exceptions.listener.classifier.Node;
import cz.muni.exceptions.listener.classifier.PackageTreeSearcher;
import cz.muni.exceptions.listener.classifier.StaxPackageDataParser;
import cz.muni.exceptions.listener.db.JPATicketRepository;
import cz.muni.exceptions.listener.db.PersistenceUnitCreator;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;

import javax.transaction.UserTransaction;
import java.io.InputStream;

/**
 * Service, that creates and registers DatabaseListener
 *
 * @author Jan Ferko
 */
public class DatabaseListenerService implements Service<DatabaseExceptionListener> {

    /** Name of service. */
    private static final String SERVICE_NAME = "Exceptions-DatabaseListener";

    private static final String PACKAGE_DATASET = "data/packages.xml";

    /** JNDI name of data source */
    private final String dataSourceJNDIName;

    /** JBoss AS Transaction manager. */
    private final InjectedValue<UserTransaction> userTransaction = new InjectedValue<>();

    /** Exception dispatcher, that is created and is running in subsystem. */
    private final InjectedValue<ExceptionDispatcher> exceptionDispatcher = new InjectedValue<>();

    /** Already created instance of listener. */
    private DatabaseExceptionListener listener;

    /**
     * Constructor creates new instance of service.
     *
     * @param dataSourceJNDIName JNDI name of data source
     * @throws java.lang.IllegalArgumentException if {@code dataSourceJNDIName} is {@code null} or empty
     */
    public DatabaseListenerService(String dataSourceJNDIName) {
        if (dataSourceJNDIName == null || dataSourceJNDIName.isEmpty()) {
            throw new IllegalArgumentException("[DataSourceJNDIName] is required and should not be null.");
        }
        this.dataSourceJNDIName = dataSourceJNDIName;
    }

    public static ServiceName createServiceName() {
        return ServiceName.JBOSS.append(SERVICE_NAME);
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        Optional<UserTransaction> userTransactionOptional = Optional.fromNullable(userTransaction.getValue());
        PersistenceUnitCreator creator = new PersistenceUnitCreator(dataSourceJNDIName, userTransactionOptional);
        JPATicketRepository repository = new JPATicketRepository(creator);

        ExceptionReportClassifier classifier;
        try {
            classifier = buildClassifier();
        } catch(Exception ex) {
            throw new StartException("Exception while initializing exception classifier", ex);
        }


        listener = new DatabaseExceptionListener(repository, classifier);
        exceptionDispatcher.getValue().registerListener(listener);
    }

    @Override
    public void stop(StopContext stopContext) {
        exceptionDispatcher.getValue().unregisterListener(listener);
    }

    @Override
    public DatabaseExceptionListener getValue() throws IllegalStateException, IllegalArgumentException {
        return this.listener;
    }

    private ExceptionReportClassifier buildClassifier() {
        InputStream dataStream = getClass().getClassLoader().getResourceAsStream(PACKAGE_DATASET);
        Node tree = new StaxPackageDataParser().parseInput(dataStream);
        PackageTreeSearcher searcher = new PackageTreeSearcher(tree);

        return new ExceptionReportClassifier(searcher);
    }
}
