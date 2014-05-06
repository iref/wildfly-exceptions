package cz.muni.exceptions.listener.db.mybatis;

import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketOccurence;
import cz.muni.exceptions.listener.db.mybatis.mappers.TicketMapper;
import org.apache.ibatis.session.Configuration;

import java.util.Properties;

/**
 * Configuration for Exceptions database.
 *
 * @author Jan Ferko
 */
public class ExceptionDatabaseConfiguration extends AbstractDatabaseConfiguration {

    private static final String JTA_ENVIRONMENT = "jta";
    private static final String LOCAL_RESOURCES_ENVIRONMENT = "local-resources";

    public ExceptionDatabaseConfiguration(String environment, Properties props) {
        super("mybatis/database-config.xml", environment, props);
    }

    public static ExceptionDatabaseConfiguration createConfiguration(String dataSource, boolean isJta) {
        String environment = isJta ? JTA_ENVIRONMENT : LOCAL_RESOURCES_ENVIRONMENT;
        Properties props = new Properties();
        props.setProperty("jndi_datasource", dataSource);

        return new ExceptionDatabaseConfiguration(environment, props);
    }

    @Override
    protected void registerAliases(Configuration configuration) {
        configuration.getTypeAliasRegistry().registerAlias(Ticket.class);
        configuration.getTypeAliasRegistry().registerAlias(TicketOccurence.class);
    }

    @Override
    protected void registerMappers(Configuration configuration) {
        configuration.getMapperRegistry().addMapper(TicketMapper.class);
        configuration.getMapperRegistry().addMapper(TicketOccurence.class);
    }
}
