package cz.muni.exceptions.listener.db.mybatis;

import com.google.common.base.Strings;
import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.listener.db.model.TicketOccurence;
import cz.muni.exceptions.listener.db.mybatis.handlers.TicketClassHandler;
import cz.muni.exceptions.listener.db.mybatis.mappers.TicketMapper;
import cz.muni.exceptions.listener.db.mybatis.mappers.TicketOccurrenceMapper;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;

import java.util.Properties;

/**
 * Configuration for Exceptions database.
 *
 * @author Jan Ferko
 */
public class ExceptionDatabaseConfiguration extends AbstractDatabaseConfiguration {

    private static final String JTA_ENVIRONMENT = "jta";
    private static final String LOCAL_RESOURCES_ENVIRONMENT = "local-resources";

    private ExceptionDatabaseConfiguration(String environment, Properties props) {
        super("mybatis/database-config.xml", environment, props);
    }

    public static ExceptionDatabaseConfiguration createConfiguration(String dataSource, boolean isJta) {
        if (Strings.isNullOrEmpty(dataSource)) {
            throw new IllegalArgumentException("[DataSource] is required and should not be null or empty");
        }

        String environment = isJta ? JTA_ENVIRONMENT : LOCAL_RESOURCES_ENVIRONMENT;
        Properties props = new Properties();
        props.setProperty("jndi_datasource", dataSource);

        return new ExceptionDatabaseConfiguration(environment, props);
    }

    @Override
    protected void registerAliases(Configuration configuration) {
        configuration.getTypeAliasRegistry().registerAlias(Ticket.class);
        configuration.getTypeAliasRegistry().registerAlias("ticketOccurrence", TicketOccurence.class);
        configuration.getTypeAliasRegistry().registerAlias(TicketClass.class);
    }

    @Override
    protected void registerMappers(Configuration configuration) {
        configuration.getMapperRegistry().addMapper(TicketMapper.class);
        configuration.getMapperRegistry().addMapper(TicketOccurrenceMapper.class);
    }

    @Override
    protected void registerTypeHandlers(Configuration configuration) {
        super.registerTypeHandlers(configuration);
        configuration.getTypeHandlerRegistry().register(TicketClass.class, JdbcType.INTEGER, TicketClassHandler.class);
    }
}
