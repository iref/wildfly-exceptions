package cz.muni.exceptions.listener.db;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
    
    /** JNDI name of datasource. */
    private final String dataSourceJNDIName;

    private final boolean isJtaManaged;
    
    /**
     * Constructor creates new instance of creator for given datasource name.
     * 
     * @param dataSourceJNDIName JNDI identifier of datasource.
     * @param isJtaManaged indicator if transaction on datasource are managed by jta
     * @throws IllegalArgumentException if {@code dataSourceJNDIName} is {@code null} or empty
     */
    public PersistenceUnitCreator(String dataSourceJNDIName, boolean isJtaManaged) {
        if (dataSourceJNDIName == null || dataSourceJNDIName.isEmpty()) {
            throw new IllegalArgumentException("[DataSourceJndiName] is required and should not be null.");
        } 
        this.dataSourceJNDIName = dataSourceJNDIName;
        this.isJtaManaged = isJtaManaged;
    }
    
    /**
     * Creates new {@link EntityManagerFactory}, that is able to persist 
     * exception into database represented by dataSource identifier provided in
     * constructor.
     * 
     * @return new EntityManagerFactory for given datasource.
     */
    public EntityManagerFactory createEntityManagerFactory() {        
        Map<String, Object> properties = new HashMap<>();
        
        try {
            if (isJtaManaged) {
                properties.put("javax.persistence.jtaDataSource", dataSourceJNDIName);
                properties.put("javax.persistence.transactionType", "JTA");
            } else {
                properties.put("javax.persistence.nonJtaDataSource", dataSourceJNDIName);
                properties.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
            }

            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        } catch (Exception e) {
            throw new SecurityException("It was not possible to create EntityManagerFactory", e);
        }
        
    }        

}
