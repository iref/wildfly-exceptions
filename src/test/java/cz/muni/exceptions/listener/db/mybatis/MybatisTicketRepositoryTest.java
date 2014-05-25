package cz.muni.exceptions.listener.db.mybatis;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import cz.muni.exceptions.listener.db.TicketRepository;
import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.listener.db.model.TicketOccurence;
import cz.muni.exceptions.listener.db.mybatis.handlers.TicketClassHandler;
import cz.muni.exceptions.listener.db.mybatis.mappers.TicketMapper;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

/**
 * @author Jan Ferko
 */
@RunWith(Arquillian.class)
public class MybatisTicketRepositoryTest {

    private ExceptionDatabaseConfiguration configuration;

    private MybatisTicketRepository repository;

    @Deployment
    public static Archive<?> getDeployment() {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("com.google.guava:guava:16.0.1", "org.mybatis:mybatis:3.2.3", "cglib:cglib:2.2.2")
                .withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "mybatisExceptionDatabaseConfiguration.war")
                .addAsLibraries(libs)
                .addClass(TicketRepository.class)
                .addPackage(TicketMapper.class.getPackage())
                .addPackage(ExceptionDatabaseConfiguration.class.getPackage())
                .addPackage(TicketClassHandler.class.getPackage())
                .addPackage(Ticket.class.getPackage())
                .addAsResource("mybatis/database-config.xml")
                .addAsResource("sql/database-drop.sql")
                .addAsResource("sql/database-build.sql")
                .addAsResource("sql/tickets.sql")
                .addAsResources(TicketMapper.class.getPackage(), "TicketMapper.xml", "TicketOccurrenceMapper.xml");

        return archive;
    }

    @Before
    public void setUp() throws IOException {
        configuration = ExceptionDatabaseConfiguration.createConfiguration("java:jboss/datasources/ExampleDS", true);
        repository = new MybatisTicketRepository(configuration);

        cleanDatabase();
        buildDatabase();
    }

    @After
    public void tearDown() throws IOException {
        cleanDatabase();
    }

    @Test
    public void testGetTicket() {
        Optional<Ticket> ticketOptional = repository.get(10L);
        Assert.assertTrue(ticketOptional.isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTicketByNullId() {
        Optional<Ticket> ticketOptional = repository.get(null);
    }

    @Test
    public void testGetTicketByNonExistingId() {
        Optional<Ticket> ticketOptional = repository.get(-1L);
        Assert.assertFalse(ticketOptional.isPresent());
    }

    @Test
    public void testGetTicketById() {
        Optional<Ticket> ticketOptional = repository.get(10L);
        Assert.assertTrue(ticketOptional.isPresent());

        Ticket ticket = ticketOptional.get();
        Assert.assertEquals(10L, ticket.getId().longValue());
        Assert.assertEquals("Something went horribly wrong", ticket.getDetailMessage());
        Assert.assertEquals("StackTrace1", ticket.getStackTrace());
        Assert.assertEquals(TicketClass.find(1), ticket.getTicketClass());
        Assert.assertEquals(2, ticket.getOccurences().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullTicket() {
        repository.add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddExistingTicket() {
        Ticket ticket = new Ticket();
        ticket.setId(1L);

        repository.add(ticket);
    }

    @Test
    public void testAddTicket() {
        TicketOccurence ticketOccurence = new TicketOccurence();
        ticketOccurence.setId(300L);
        ticketOccurence.setTimestamp(new Timestamp(new Date().getTime()));
        Ticket ticket = new Ticket("OctocatException", "Stacktrace3", TicketClass.DATABASE, Lists.newArrayList(ticketOccurence));

        repository.add(ticket);

        Assert.assertNotNull(ticket.getId());
        Assert.assertTrue(repository.get(ticket.getId()).isPresent());
    }

    @Test
    public void testUpdateTicket() {
        TicketOccurence ticketOccurence = new TicketOccurence();
        ticketOccurence.setTimestamp(new Timestamp(new Date().getTime()));
        Ticket ticket = new Ticket("NewException", "Stacktrace updated", TicketClass.FILE,
                Lists.newArrayList(ticketOccurence));
        ticket.setId(10L);

        repository.update(ticket);

        Optional<Ticket> ticketOptional = repository.get(10L);
        Assert.assertTrue(ticketOptional.isPresent());

        Ticket updated = ticketOptional.get();
        Assert.assertEquals("NewException", updated.getDetailMessage());
        Assert.assertEquals("Stacktrace updated", updated.getStackTrace());
        Assert.assertEquals(TicketClass.FILE, updated.getTicketClass());
        Assert.assertEquals(1, updated.getOccurences().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNullTicket() {
        repository.update(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTicketWithoutId() {
        repository.update(new Ticket());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveByNullId() {
        repository.remove(null);
    }

    @Test
    public void testRemove() {
        repository.remove(20L);

        Set<Ticket> all = repository.all();
        Assert.assertEquals(1, all.size());
    }

    @Test
    public void testAll() {
        Set<Ticket> all = repository.all();
        Assert.assertEquals(2, all.size());
    }

    private void buildDatabase() throws IOException {
        SqlSession sqlSession = configuration.openSession();
        ScriptRunner runner = new ScriptRunner(sqlSession.getConnection());
        runner.setStopOnError(true);
        runner.runScript(Resources.getResourceAsReader(getClass().getClassLoader(), "sql/database-build.sql"));
        runner.runScript(Resources.getResourceAsReader(getClass().getClassLoader(), "sql/tickets.sql"));
        runner.closeConnection();
        sqlSession.close();
    }

    private void cleanDatabase() throws IOException {
        SqlSession sqlSession = configuration.openSession();
        ScriptRunner runner = new ScriptRunner(sqlSession.getConnection());
        runner.runScript(Resources.getResourceAsReader(getClass().getClassLoader(), "sql/database-drop.sql"));
        runner.closeConnection();
        sqlSession.close();
    }
}
