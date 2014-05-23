package cz.muni.exceptions.listener.db;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * Class, that builds database schema for exceptions.
 *
 * @author Jan Ferko
 */
public class DatabaseBuilder {

    /** SQLSessionFactory to provide connection for script runner. */
    private final SqlSessionFactory sqlSessionFactory;

    /**
     * Constructor to build new builder for given sql session factory.
     *
     * @param sqlSessionFactory SQL session factory, that provides database connection for schema creation.
     * @throws java.lang.IllegalArgumentException if sqlSessionFactory is {@code null}
     */
    public DatabaseBuilder(SqlSessionFactory sqlSessionFactory) {
        if (sqlSessionFactory == null) {
            throw new IllegalArgumentException("[SqlSessionFactory] is required and should not be null.");
        }
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * Tries to build database schema.
     */
    public boolean tryToBuildDatabase() {
        if (!isDatabaseAlreadyCreated()) {
            runUpdate();
            return true;
        }
        return false;
    }

    /**
     * Checks if database schema is already created.
     *
     * @return {@code true} if schema is already created, otherwise {@code false}
     */
    private boolean isDatabaseAlreadyCreated() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        boolean isCreated = true;
        try {
            sqlSession.selectList("cz.muni.exceptions.listener.db.mybatis.mappers.TicketMapper.selectAllTickets");
        } catch (Exception ex) {
            // table does not exists
            isCreated = false;
        } finally {
            sqlSession.close();
        }

        return isCreated;
    }

    /**
     * Runs update, that actually creates database schema.
     *
     * @throws java.lang.RuntimeException if build script was not found.
     */
    private void runUpdate() {
        SqlSession sqlSession = sqlSessionFactory.openSession();

        try {
            ScriptRunner scriptRunner = new ScriptRunner(sqlSession.getConnection());
            scriptRunner.setStopOnError(true);
            Reader dropReader = Resources.getResourceAsReader(getClass().getClassLoader(), "sql/database-drop.sql");
            try (Reader buildReader = Resources.getResourceAsReader(getClass().getClassLoader(), "sql/database-build.sql")) {
                scriptRunner.runScript(dropReader);
                scriptRunner.runScript(buildReader);
            }
        } catch (IOException e) {
            throw new RuntimeException("It was not possible to read database build script.", e);
        } finally {
            sqlSession.close();
        }
    }
}
