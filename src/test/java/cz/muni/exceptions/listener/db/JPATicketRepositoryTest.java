package cz.muni.exceptions.listener.db;

import com.google.common.base.Optional;
import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.listener.db.model.TicketOccurence;
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

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.transaction.TransactionManager;
import java.io.File;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

/**
 * 
 * @author Jan Ferko
 * @date 2014-04-16T03:57:34+0100
 */
@RunWith(Arquillian.class)
public class JPATicketRepositoryTest {
    
    @Deployment
    public static Archive<?> getDeployment() {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("com.google.guava:guava:16.0.1").withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "jpaTicketRepositoryTest.war")
                .addAsLibraries(libs)
                .addPackage(JPATicketRepository.class.getPackage())
                .addPackage(Ticket.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("jbossas-ds.xml");

        return archive;
    }
    
    private TicketRepository repository;
    
    private PersistenceUnitCreator persistenceUnitCreator;
    
    private EntityManager entityManager;
    
    private EntityTransaction entityTx;
    
    private Ticket mockTicket;

    @Before
    public void before() {
        if (persistenceUnitCreator == null) {
            persistenceUnitCreator = new PersistenceUnitCreator("jdbc/arquillian", Optional.<TransactionManager>absent());
        }

        repository = new JPATicketRepository(persistenceUnitCreator);
        entityManager = persistenceUnitCreator.createEntityManager();
        
        // clean database
        cleanDatabase();
        
        // insert some data
        insertTestData();
        
        // start transaction
        entityTx = entityManager.getTransaction();
        entityTx.begin();
    }
    
    @After
    public void after() {
        // commit / rollback transaction
        entityTx.commit();
        
        // clean database
        cleanDatabase();
        
        // close em
        entityManager.close();
    }
    
    @Test
    public void testAllTickets() {
        Set<Ticket> all = repository.all();
        Assert.assertNotNull(all);
        Assert.assertEquals(1, all.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetTicketWithNullId() {
        repository.get(null);
    }

    @Test
    public void testGetMissingTicket() {
        Optional<Ticket> actual = repository.get(mockTicket.getId() + 1);
        Assert.assertFalse(actual.isPresent());
    }

    @Test
    public void testGetTicket() {
        Optional<Ticket> actual = repository.get(mockTicket.getId());
        Assert.assertTrue(actual.isPresent());

        Ticket actualTicket = actual.get();
        Assert.assertEquals(mockTicket, actualTicket);
        Assert.assertEquals(mockTicket.getDetailMessage(), actualTicket.getDetailMessage());
        Assert.assertEquals(mockTicket.getStackTrace(), actualTicket.getStackTrace());
        Assert.assertEquals(mockTicket.getTicketClass(), actualTicket.getTicketClass());
        Assert.assertEquals(mockTicket.getOccurences().size(), actualTicket.getOccurences().size());

        for (int i = 0; i < mockTicket.getOccurences().size(); i++) {
            TicketOccurence actualOccurence = actualTicket.getOccurences().get(i);
            TicketOccurence expectedOccurence = mockTicket.getOccurences().get(i);
            Assert.assertEquals(expectedOccurence.getId(), actualOccurence.getId());
            Assert.assertEquals(expectedOccurence.getTimestamp(), actualOccurence.getTimestamp());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveWithNullId() {
        repository.remove(null);
    }

    @Test
    public void testRemoveMissingTicket() {
        TypedQuery<Ticket> selectQuery = entityManager.createQuery("SELECT t FROM Ticket t", Ticket.class);

        int ticketCount = selectQuery.getResultList().size();
        repository.remove(mockTicket.getId() + 1);
        int afterRemoveCount = selectQuery.getResultList().size();

        Assert.assertEquals(ticketCount, afterRemoveCount);
    }

    @Test
    public void testRemoveMockTicket() {
        repository.remove(mockTicket.getId());

        entityManager.find(Ticket.class, mockTicket.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNullTicket() {
        repository.update(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTicketWithoutId() {
        Ticket withoutId = new Ticket();
        repository.update(withoutId);
    }

    @Test
    public void testUpdateMockTicket() {
        mockTicket.setDetailMessage("Hello from OctoCat!!");

        repository.update(mockTicket);

        Ticket actual = entityManager.find(Ticket.class, mockTicket.getId());
        Assert.assertEquals(mockTicket, actual);
        Assert.assertEquals(mockTicket.getDetailMessage(), actual.getDetailMessage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullTicket() {
        repository.add(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTicketWithId() {
        repository.add(mockTicket);
    }

    @Test
    public void testAddNewTicket() {
        TicketOccurence to = new TicketOccurence();
        to.setTimestamp(new Timestamp(new Date().getTime()));
        Ticket ticket = new Ticket("Hello From Octocat!!", "OctocatException", "OctoCat stack", TicketClass.FILE, Arrays.asList(to));

        repository.add(ticket);

        Ticket actual = entityManager.find(Ticket.class, ticket.getId());
        Assert.assertEquals(ticket.getDetailMessage(), actual.getDetailMessage());
        Assert.assertEquals(ticket.getClassName(), actual.getClassName());
        Assert.assertEquals(ticket.getStackTrace(), actual.getStackTrace());
        Assert.assertEquals(ticket.getTicketClass(), ticket.getTicketClass());
        Assert.assertEquals(ticket.getOccurences().size(), actual.getOccurences().size());
    }

    private void insertTestData() {
        TicketOccurence ticketOccurence = new TicketOccurence();
        ticketOccurence.setTimestamp(new Timestamp(new Date().getTime()));
        
        mockTicket = new Ticket("Something went terribly wrong", "LongException", "Some long stacktrace",
                TicketClass.DATABASE, Arrays.asList(ticketOccurence));
        
        entityManager.getTransaction().begin();
        entityManager.persist(mockTicket);
        entityManager.getTransaction().commit();
    }
    
    private void cleanDatabase() {
        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM TicketOccurence to").executeUpdate();
        entityManager.createQuery("DELETE FROM Ticket t").executeUpdate();
        entityManager.getTransaction().commit();
    }

}
