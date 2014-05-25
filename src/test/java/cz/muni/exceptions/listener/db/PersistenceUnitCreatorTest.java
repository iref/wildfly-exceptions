package cz.muni.exceptions.listener.db;

import com.google.common.base.Optional;
import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.listener.db.model.TicketOccurence;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;
import java.io.File;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

/**
 *
 * @author Jan Ferko
 * @date 2014-04-15T04:30:45+0100
 */
@RunWith(Arquillian.class)
public class PersistenceUnitCreatorTest {
    
    @Deployment
    public static WebArchive createDeployment() {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("com.google.guava:guava:16.0.1", "org.mockito:mockito-core:1.9.5")
                .withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "persistenceTest.war")
                .addClass(PersistenceUnitCreator.class)
                .addPackage(Ticket.class.getPackage())
                .addAsLibraries(libs)
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource("jbossas-ds.xml");
                
        return archive;
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructForNullDataSource() {
        new PersistenceUnitCreator(null, Optional.<TransactionManager>absent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructForEmptyDataSource() {
        new PersistenceUnitCreator("", Optional.<TransactionManager>absent());
    }

    @Test
    public void isJtaManaged() {
        TransactionManager mockUserTransaction = Mockito.mock(TransactionManager.class);
        PersistenceUnitCreator managedCreator = new PersistenceUnitCreator("java:jboss/datasources/ExampleDS",
                Optional.of(mockUserTransaction));
        Assert.assertTrue(managedCreator.isJtaManaged());
    }

    @Test
    public void isNotJtaManaged() {
        PersistenceUnitCreator creator = new PersistenceUnitCreator("java:jdbc/arquillian", Optional.<TransactionManager>absent());
        Assert.assertFalse(creator.isJtaManaged());
    }
    
    @Test
    public void testCreateEntityManagerWithoutJTA() {
        PersistenceUnitCreator creator = new PersistenceUnitCreator("java:jdbc/arquillian",
                Optional.<TransactionManager>absent());
        EntityManager em = creator.createEntityManager();
        Assert.assertNotNull(em);

        TicketOccurence ticketOccurence = new TicketOccurence();
        ticketOccurence.setTimestamp(new Timestamp(new Date().getTime()));

        Ticket ticket = new Ticket("Something went terribly wrong", "java.lang.Exception",
                "Some awefull stacktrace", TicketClass.DATABASE, Arrays.asList(ticketOccurence));

        em.getTransaction().begin();
        em.persist(ticket);
        em.getTransaction().commit();

        EntityManager entityManager2 = creator.createEntityManager();
        Ticket actual = entityManager2.find(Ticket.class, ticket.getId());
        Assert.assertNotNull(actual);

    }

}
