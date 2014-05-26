package cz.muni.exceptions.source.integration;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import cz.muni.exceptions.listener.db.DatabaseBuilder;
import cz.muni.exceptions.listener.db.TicketRepository;
import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.mybatis.ExceptionDatabaseConfiguration;
import cz.muni.exceptions.listener.db.mybatis.MybatisTicketRepository;
import cz.muni.exceptions.listener.db.mybatis.handlers.TicketClassHandler;
import cz.muni.exceptions.listener.db.mybatis.mappers.TicketMapper;
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

import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * @author Jan Ferko
 */
//@RunWith(Arquillian.class)
public class LoggingExceptionSourceIntegrationTest {



    @Deployment
    public static Archive<?> getDeployment() {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("com.google.guava:guava:16.0.1", "org.mybatis:mybatis:3.2.3", "cglib:cglib:2.2.2",
                         "net.sourceforge.htmlunit:htmlunit:2.7")
                .withTransitivity().asFile();

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "loggingSourceTest.war")
                .setWebXML("deployments/logging/web.xml")
                .addAsWebInfResource("deployments/logging/jboss-web.xml")
                .addClass(LoggingMockServlet.class)
                .addPackage(Ticket.class.getPackage())
                .addPackage(TicketRepository.class.getPackage())
                .addPackage(ExceptionDatabaseConfiguration.class.getPackage())
                .addPackage(TicketMapper.class.getPackage())
                .addPackage(TicketClassHandler.class.getPackage())
                .addAsLibraries(libs)
                .addAsResource("mybatis/database-config.xml")
                .addAsResource("sql/database-drop.sql")
                .addAsResource("sql/database-build.sql")
                .addAsResource("jbossas-ds.xml")
                .addAsResources(TicketMapper.class.getPackage(), "TicketMapper.xml", "TicketOccurrenceMapper.xml");
        return archive;
    }

    private TicketRepository repository;

    @Before
    public void setUp() throws NamingException {
        ExceptionDatabaseConfiguration configuration = ExceptionDatabaseConfiguration
                .createConfiguration("jdbc/arquillian", false);
        new DatabaseBuilder(configuration).tryToBuildDatabase();
        repository = new MybatisTicketRepository(configuration);
    }

    //@Test
    public void testExceptionWasStored() throws IOException {
        sendRequestToServlet();

        Set<Ticket> all = repository.all();
        Assert.assertEquals(2, all.size());
    }

    private void sendRequestToServlet() throws IOException {
        WebClient webClient = new WebClient();

        HtmlPage page = webClient.getPage("http://localhost:8080/exceptions/");

        // check if request was processed
        HtmlElement body = page.getBody();
        Assert.assertEquals("Hello World!", body.getTextContent());
    }
}
