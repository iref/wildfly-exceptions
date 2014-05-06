package cz.muni.exceptions.listener.db.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.util.Properties;

/**
 * Implementation of SqlSessionFactory, that allows programmatic creation of SqlSession.
 *
 * @author Jan Ferko
 */
public abstract class AbstractDatabaseConfiguration implements SqlSessionFactory {

    private final SqlSessionFactory delegate;

    public AbstractDatabaseConfiguration(String configPath, String environment, Properties properties) {

        try {
            final Reader reader = Resources.getResourceAsReader(getClass().getClassLoader(), configPath);
            final SqlSessionFactoryBuilder factoryBuilder = new SqlSessionFactoryBuilder();
            this.delegate = factoryBuilder.build(reader, environment, properties);
        } catch (IOException ex) {
            throw new IllegalStateException("Configuration was not found at path[" + configPath + "].", ex);
        }
    }

    protected void registerAliases(Configuration configuration);

    @Override
    public Configuration getConfiguration() {
        return delegate.getConfiguration();
    }

    @Override
    public SqlSession openSession() {
        return delegate.openSession();
    }

    @Override
    public SqlSession openSession(boolean autoCommit) {
        return delegate.openSession(autoCommit);
    }

    @Override
    public SqlSession openSession(Connection connection) {
        return delegate.openSession(connection);
    }

    @Override
    public SqlSession openSession(TransactionIsolationLevel level) {
        return delegate.openSession(level);
    }

    @Override
    public SqlSession openSession(ExecutorType execType) {
        return delegate.openSession(execType);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, boolean autoCommit) {
        return delegate.openSession(execType, autoCommit);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, TransactionIsolationLevel level) {
        return delegate.openSession(execType, level);
    }

    @Override
    public SqlSession openSession(ExecutorType execType, Connection connection) {
        return delegate.openSession(execType, connection);
    }

}
