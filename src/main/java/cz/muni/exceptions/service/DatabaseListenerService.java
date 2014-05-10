package cz.muni.exceptions.service;

import com.google.common.base.Optional;
import com.sun.corba.se.spi.activation._RepositoryImplBase;
import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import cz.muni.exceptions.listener.DatabaseExceptionListener;
import cz.muni.exceptions.listener.classifier.ExceptionReportClassifier;
import cz.muni.exceptions.listener.classifier.Node;
import cz.muni.exceptions.listener.classifier.PackageTreeSearcher;
import cz.muni.exceptions.listener.classifier.StaxPackageDataParser;
import cz.muni.exceptions.listener.db.JPATicketRepository;
import cz.muni.exceptions.listener.db.PersistenceUnitCreator;
import cz.muni.exceptions.listener.db.TicketRepository;
import cz.muni.exceptions.listener.db.TicketRepositoryFactory;
import cz.muni.exceptions.listener.duplication.LevenshteinSimilarityChecker;
import cz.muni.exceptions.listener.duplication.SimilarityChecker;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;

import javax.transaction.TransactionManager;
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
    private final InjectedValue<TransactionManager> transactionManager = new InjectedValue<>();

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
        Optional<TransactionManager> userTransactionOptional = Optional.fromNullable(transactionManager.getOptionalValue());
//        PersistenceUnitCreator creator = new PersistenceUnitCreator(dataSourceJNDIName, userTransactionOptional);
//        JPATicketRepository repository = new JPATicketRepository(creator);
        TicketRepository repository = TicketRepositoryFactory.newInstance(dataSourceJNDIName, userTransactionOptional.isPresent());

        ExceptionReportClassifier classifier;
        try {
            classifier = buildClassifier();
        } catch(Exception ex) {
            throw new StartException("Exception while initializing exception classifier", ex);
        }

        SimilarityChecker checker = new LevenshteinSimilarityChecker();
        listener = new DatabaseExceptionListener(repository, classifier, checker);
        getDispatcher().getValue().registerListener(listener);
    }

    @Override
    public void stop(StopContext stopContext) {
        exceptionDispatcher.getValue().unregisterListener(listener);
    }

    @Override
    public DatabaseExceptionListener getValue() throws IllegalStateException, IllegalArgumentException {
        return this.listener;
    }

    public InjectedValue<ExceptionDispatcher> getDispatcher() {
        return this.exceptionDispatcher;
    }

    public InjectedValue<TransactionManager> getTransactionManager() {
        return this.transactionManager;
    }

    private ExceptionReportClassifier buildClassifier() {
        InputStream dataStream = getClass().getClassLoader().getResourceAsStream(PACKAGE_DATASET);
        Node tree = new StaxPackageDataParser().parseInput(dataStream);
        PackageTreeSearcher searcher = new PackageTreeSearcher(tree);

        return new ExceptionReportClassifier(searcher);
    }
}
