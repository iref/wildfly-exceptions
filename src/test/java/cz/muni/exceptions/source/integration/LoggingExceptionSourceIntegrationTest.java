package cz.muni.exceptions.source.integration;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.base.Optional;
import cz.muni.exceptions.listener.db.JPATicketRepository;
import cz.muni.exceptions.listener.db.PersistenceUnitCreator;
import cz.muni.exceptions.listener.db.TicketRepository;
import cz.muni.exceptions.listener.db.model.Ticket;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * @author Jan Ferko
 */
@RunWith(Arquillian.class)
public class LoggingExceptionSourceIntegrationTest {



    @Deployment
    public static Archive<?> getDeployment() {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("com.google.guava:guava:16.0.1", "net.sourceforge.htmlunit:htmlunit:2.7")
                .withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "loggingSourceTest.war")
                .setWebXML("deployments/logging/web.xml")
                .addAsWebInfResource("deployments/logging/jboss-web.xml")
                .addAsWebInfResource("jbossas-ds.xml")
                .addClass(LoggingMockServlet.class)
                .addPackage(Ticket.class.getPackage())
                .addPackage(TicketRepository.class.getPackage())
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsLibraries(libs);
        return archive;
    }

    private TicketRepository repository;

    @Before
    public void setUp() {
        PersistenceUnitCreator creator = new PersistenceUnitCreator("jdbc/arquillian", Optional.<UserTransaction>absent());
        repository = new JPATicketRepository(creator);

        EntityManager entityManager = creator.createEntityManager();
        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM Ticket t").executeUpdate();
        entityManager.getTransaction().commit();
    }

    @Test
    public void testExceptionWasStored() throws IOException {
        sendRequestToServlet();

        Set<Ticket> all = repository.all();
        Assert.assertEquals(1, all.size());
    }

    private void sendRequestToServlet() throws IOException {
        WebClient webClient = new WebClient();

        HtmlPage page = webClient.getPage("http://localhost:8080/exceptions/");

        // check if request was processed
        HtmlElement body = page.getBody();
        Assert.assertEquals("Hello World!", body.getTextContent());
    }
}
