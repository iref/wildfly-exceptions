package cz.muni.exceptions.listener.db.mybatis;

import cz.muni.exceptions.listener.db.JPATicketRepository;
import cz.muni.exceptions.listener.db.TicketRepository;
import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.listener.db.model.TicketOccurence;
import cz.muni.exceptions.listener.db.mybatis.mappers.TicketMapper;
import cz.muni.exceptions.listener.db.mybatis.mappers.TicketOccurrenceMapper;
import junit.framework.Assert;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.type.TypeAliasRegistry;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Collection;

/**
 * @author Jan Ferko
 */
@RunWith(Arquillian.class)
public class ExceptionDatabaseConfigurationTest {

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
                .addPackage(Ticket.class.getPackage())
                .addAsResource("mybatis/database-config.xml", "mybatis/database-config.xml")
                .addAsResource(TicketMapper.class.getPackage(), "TicketMapper.xml", "TicketOccurrenceMapper.xml")
                .addAsWebInfResource("jbossas-ds.xml");

        return archive;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDatabaseConfigurationWithoutDataSource() {
        ExceptionDatabaseConfiguration.createConfiguration(null, false);
    }

    @Test
    public void testCreateJtaDatabaseConfiguration() {
        ExceptionDatabaseConfiguration configuration = ExceptionDatabaseConfiguration.createConfiguration(
                "java:jboss/datasources/ExampleDS", true);
        Assert.assertNotNull(configuration);
        Assert.assertEquals("jta", configuration.getConfiguration().getEnvironment().getId());
    }

    @Test
    public void testCreateNonJtaDatabaseConfiguration() {
        ExceptionDatabaseConfiguration configuration = ExceptionDatabaseConfiguration.createConfiguration(
                "jdbc/arquillian", false);
        Assert.assertNotNull(configuration);
        Assert.assertEquals("local-resources", configuration.getConfiguration().getEnvironment().getId());
    }

    @Test
    public void testRegisterMappers() {
        ExceptionDatabaseConfiguration configuration = ExceptionDatabaseConfiguration.createConfiguration(
            "jdbc/arquillian", false
        );

        Collection<Class<?>> mappers = configuration.getConfiguration().getMapperRegistry().getMappers();
        Assert.assertEquals(2, mappers.size());
        Assert.assertTrue(mappers.contains(TicketMapper.class));
        Assert.assertTrue(mappers.contains(TicketOccurrenceMapper.class));
    }

    @Test
    public void testRegisterAliases() {
        ExceptionDatabaseConfiguration configuration = ExceptionDatabaseConfiguration.createConfiguration(
                "jdbc/arquillian", false        );

        TypeAliasRegistry typeAliasRegistry = configuration.getConfiguration().getTypeAliasRegistry();
        Assert.assertEquals(Ticket.class, typeAliasRegistry.resolveAlias(Ticket.class.getSimpleName()));
        Assert.assertEquals(TicketOccurence.class, typeAliasRegistry.resolveAlias("ticketOccurrence"));
        Assert.assertEquals(TicketClass.class, typeAliasRegistry.resolveAlias(TicketClass.class.getSimpleName()));
    }
}
