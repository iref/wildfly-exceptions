package cz.muni.exceptions.listener.db;

import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.mybatis.ExceptionDatabaseConfiguration;
import cz.muni.exceptions.listener.db.mybatis.handlers.TicketClassHandler;
import cz.muni.exceptions.listener.db.mybatis.mappers.TicketMapper;
import junit.framework.Assert;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

/**
 * @author Jan Ferko
 */
@RunWith(Arquillian.class)
public class DatabaseBuilderTest {

    private ExceptionDatabaseConfiguration configuration;

    @Deployment
    public static Archive<?> getDeployment() {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("com.google.guava:guava:16.0.1", "org.mybatis:mybatis:3.2.3", "cglib:cglib:2.2.2")
                .withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "databaseBuilderTest.war")
                .addAsLibraries(libs)
                .addClass(DatabaseBuilder.class)
                .addPackage(TicketMapper.class.getPackage())
                .addPackage(ExceptionDatabaseConfiguration.class.getPackage())
                .addPackage(TicketClassHandler.class.getPackage())
                .addPackage(Ticket.class.getPackage())
                .addAsResource("mybatis/database-config.xml")
                .addAsResource("sql/database-drop.sql")
                .addAsResource("sql/database-build.sql")
                .addAsResources(TicketMapper.class.getPackage(), "TicketMapper.xml", "TicketOccurrenceMapper.xml");

        return archive;
    }

    @Before
    public void setUp() throws IOException {
        this.configuration = ExceptionDatabaseConfiguration
                .createConfiguration("java:jboss/datasources/ExampleDS", true);
        cleanDatabase(configuration);
    }

    @After
    public void tearDown() throws IOException {
        cleanDatabase(configuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNewBuilderForNullSqlSessionFactory() {
        new DatabaseBuilder(null);
    }

    @Test
    public void testTryToBuildDatabaseIfSchemaExists() throws IOException {
        buildDatabase(configuration);

        DatabaseBuilder databaseBuilder = new DatabaseBuilder(configuration);
        Assert.assertFalse(databaseBuilder.tryToBuildDatabase());
    }

    @Test
    public void testTryToBuildDatabase() {
        DatabaseBuilder databaseBuilder = new DatabaseBuilder(configuration);
        Assert.assertTrue(databaseBuilder.tryToBuildDatabase());
    }

    private void cleanDatabase(ExceptionDatabaseConfiguration configuration) throws IOException {
        SqlSession sqlSession = configuration.openSession();

        ScriptRunner runner = new ScriptRunner(sqlSession.getConnection());
        runner.runScript(Resources.getResourceAsReader(getClass().getClassLoader(), "sql/database-drop.sql"));
        runner.closeConnection();

        sqlSession.close();
    }

    private void buildDatabase(ExceptionDatabaseConfiguration configuration) throws IOException {
        SqlSession session = configuration.openSession();
        ScriptRunner runner = new ScriptRunner(session.getConnection());
        runner.runScript(Resources.getResourceAsReader(getClass().getClassLoader(), "sql/database-build.sql"));
        runner.closeConnection();
        session.close();
    }
}
