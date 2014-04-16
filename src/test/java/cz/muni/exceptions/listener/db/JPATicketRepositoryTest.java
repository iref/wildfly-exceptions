package cz.muni.exceptions.listener.db;

import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.listener.db.model.TicketOccurence;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author Jan Ferko
 * @date 2014-04-16T03:57:34+0100
 */
@RunWith(Arquillian.class)
public class JPATicketRepositoryTest {
    
    @Deployment
    public static Archive<?> getDeployment() {        
        return ShrinkWrap.create(WebArchive.class, "ticketRepositoryTest.war")
                .addPackage(JPATicketRepository.class.getPackage())
                .addPackage(Ticket.class.getPackage())                
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("jbossas-ds.xml");
                
    }
    
    private TicketRepository repository;
    
    private final PersistenceUnitCreator persistenceUnitCreator = new PersistenceUnitCreator(
            "jdbc/arquillian", false);
    
    private EntityManager entityManager;
    
    private EntityTransaction entityTx;
    
    private Ticket mockTicket;
    
    @Before
    public void before() {
        repository = new JPATicketRepository(persistenceUnitCreator);
        entityManager = persistenceUnitCreator.createEntityManagerFactory().createEntityManager();
        
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
        
        // clean databasae
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
    
    private void insertTestData() {
        TicketOccurence ticketOccurence = new TicketOccurence();
        ticketOccurence.setTimestamp(new Timestamp(new Date().getTime()));
        
        mockTicket = new Ticket("Something went terribly wrong", "Some long stacktrace", 
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
