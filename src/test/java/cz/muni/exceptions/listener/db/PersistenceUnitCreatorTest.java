package cz.muni.exceptions.listener.db;

import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.listener.db.model.TicketOccurence;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Jan Ferko
 * @date 2014-04-15T04:30:45+0100
 */
@RunWith(Arquillian.class)
public class PersistenceUnitCreatorTest {
    
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, "persistenceTest.war")
                .addClass(PersistenceUnitCreator.class)
                .addPackage(Ticket.class.getPackage())
                .addAsResource("META-INF/persistence.xml", "META-INF/persistence.xml");
                
        return archive;
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructForNullDataSource() {
        new PersistenceUnitCreator(null, false);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructForEmptyDataSource() {
        new PersistenceUnitCreator("", false);
    }
    
    @Test
    public void testCreateEntityManagerFactory() {
        PersistenceUnitCreator creator = new PersistenceUnitCreator("java:jboss/datasources/ExampleDS", false);
        EntityManagerFactory emf = creator.createEntityManagerFactory();
        Assert.assertNotNull(emf);

        EntityManager entityManager = emf.createEntityManager();
        Assert.assertNotNull(entityManager);
        
        TicketOccurence ticketOccurence = new TicketOccurence();
        ticketOccurence.setTimestamp(new Timestamp(new Date().getTime()));
        
        Ticket ticket = new Ticket("Something went terribly wrong", 
                "Some awefull stacktrace", TicketClass.DATABASE, Arrays.asList(ticketOccurence));
        
        entityManager.getTransaction().begin();
        entityManager.persist(ticket);
        entityManager.getTransaction().commit();
        
        EntityManager entityManager2 = emf.createEntityManager();
        Ticket actual = entityManager2.find(Ticket.class, ticket.getId());
        Assert.assertNotNull(actual);
        
    }
    
    @Test(expected = SecurityException.class)
    public void testCreateEntityManagerFactoryForNonexistingDataSource() {
        PersistenceUnitCreator creator = new PersistenceUnitCreator("java:jboss/missingDS", false);
        EntityManagerFactory emf = creator.createEntityManagerFactory();
    }

}
