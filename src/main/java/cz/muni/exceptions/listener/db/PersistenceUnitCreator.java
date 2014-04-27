package cz.muni.exceptions.listener.db;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.transaction.jta.platform.internal.JBossAppServerJtaPlatform;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class creates new EntityManageFactory, that allows to persist exception data 
 * to database.
 * 
 * @author Jan Ferko
 * @date 2014-04-15T03:28:51+0100
 */
public class PersistenceUnitCreator {
    
    /** Name of persistence unit. */
    private static final String PERSISTENCE_UNIT_NAME = "exceptionsPU";

    private EntityManagerFactory emf;

    private Optional<UserTransaction> userTransaction;
    
    /**
     * Constructor creates new instance of creator for given datasource name.
     * 
     * @param dataSourceJNDIName JNDI identifier of data source.
     * @param userTransaction JTA transaction manager, that should be used to manage transaction
     *                           or {@link com.google.common.base.Optional#absent()} if data source
     *                           does not use JTA
     * @throws IllegalArgumentException if {@code dataSourceJNDIName} is {@code null} or empty
     */
    public PersistenceUnitCreator(String dataSourceJNDIName, Optional<UserTransaction> userTransaction) {
        if (dataSourceJNDIName == null || dataSourceJNDIName.isEmpty()) {
            throw new IllegalArgumentException("[DataSourceJndiName] is required and should not be null.");
        }
        this.userTransaction = userTransaction;
        this.emf = createEntityManagerFactory(dataSourceJNDIName);
    }

    /**
     * Creates new EntityManager.
     *
     * @return new entity manager
     */
    public EntityManager createEntityManager() {
        final EntityManager result;

        if (this.userTransaction.isPresent()) {
            EntityManagerInvocationHandler proxy = new EntityManagerInvocationHandler(
                    this.emf.createEntityManager(), userTransaction.get());
            ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
            result = (EntityManager) Proxy.newProxyInstance(threadClassLoader, new Class<?>[]{EntityManager.class},
                    proxy);
        } else {
            result = emf.createEntityManager();
        }

        return result;
    }

    /**
     * Returns true if PersistenceUnitCreator creates JTA managed entity managers.
     *
     * @return {@code true} if PersistenceUnitCreator creates JTA managed entity managers, otherwise {@code false}.
     */
    public boolean isJtaManaged() {
        return this.userTransaction.isPresent();
    }
    
    /**
     * Creates new {@link EntityManagerFactory}, that is able to persist 
     * exception into database represented by dataSource identifier provided in
     * constructor.
     * 
     * @return new EntityManagerFactory for given datasource.
     */
    private EntityManagerFactory createEntityManagerFactory(String dataSourceJNDIName) {
        Map<String, Object> properties = new HashMap<>();
        
        try {
            if (this.userTransaction.isPresent()) {
                properties.put("javax.persistence.jtaDataSource", dataSourceJNDIName);
                properties.put("javax.persistence.transactionType", "JTA");
                properties.put(AvailableSettings.JTA_PLATFORM, new JBossAppServerJtaPlatform());
            } else {
                properties.put("javax.persistence.nonJtaDataSource", dataSourceJNDIName);
                properties.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
            }

            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        } catch (Exception e) {
            throw new SecurityException("It was not possible to create EntityManagerFactory", e);
        }
        
    }

    /**
     * Dynamic proxy for JTA managed EntityManager.
     */
    private static class EntityManagerInvocationHandler implements InvocationHandler {

        /** Set of method name that require transactions. */
        private static final Set<String> TRANSACTION_MANAGED_METHODS;

        /** EntityManager instance, that should be decorated. */
        private final EntityManager entityManager;

        /** Transaction manager, that is used to manage transactions. */
        private final UserTransaction userTransaction;

        static {
            TRANSACTION_MANAGED_METHODS = ImmutableSet.of("persist", "merge", "remove", "flush", "lock",
                    "refresh", "getLockMode");
        }

        /**
         * Constructor creates new instance of invocation handler.
         *
         * @param entityManager entity manager, that should be decorated
         * @param userTransaction transaction manager, that is used to manage transaction
         */
        public EntityManagerInvocationHandler(EntityManager entityManager, UserTransaction userTransaction) {
            this.userTransaction = userTransaction;
            this.entityManager = entityManager;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if (TRANSACTION_MANAGED_METHODS.contains(method.getName())) {
                if (userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION) {
                    userTransaction.begin();
                }

                this.entityManager.joinTransaction();
            }

            try {
                return method.invoke(entityManager, args);
            } finally {
                if (userTransaction.getStatus() == Status.STATUS_ACTIVE) {
                    userTransaction.commit();
                }
            }
        }
    }

}
