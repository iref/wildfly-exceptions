package cz.muni.exceptions.listener.db;

import cz.muni.exceptions.listener.db.mybatis.ExceptionDatabaseConfiguration;
import cz.muni.exceptions.listener.db.mybatis.MybatisTicketRepository;

/**
 * Factory, that creates new TicketRepository based on given configuration..
 *
 * @author Jan Ferko
 */
public final class TicketRepositoryFactory {

    /**
     * Creates new TicketRepository for given data source.
     *
     * @param dataSource JNDI name of data source.
     * @param isJta indicator if data source is managed by JTA.
     * @return new ticket repository for accessing exceptions in data source
     */
    public static TicketRepository newInstance(String dataSource, boolean isJta) {
        ExceptionDatabaseConfiguration configuration = ExceptionDatabaseConfiguration
                .createConfiguration(dataSource, isJta);
        // build database
        DatabaseBuilder databaseBuilder = new DatabaseBuilder(configuration);
        databaseBuilder.tryToBuildDatabase();

        // create ticket repository
        return new MybatisTicketRepository(configuration);
    }
}
