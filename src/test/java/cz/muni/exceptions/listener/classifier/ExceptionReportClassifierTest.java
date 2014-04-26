package cz.muni.exceptions.listener.classifier;

import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.source.ExceptionReport;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Jan Ferko
 */
public class ExceptionReportClassifierTest {

    private ExceptionReportClassifier classifier;

    @Before
    public void setUp() {
        Node tree = new PackageTreeBuilder()
                .addPackage("org.springframework.core.bean", TicketClass.INTEGRATION, 1.0)
                .addPackage("java.lang", TicketClass.JVM, 1.0)
                .addPackage("org.springframework.context", TicketClass.INTEGRATION, 1.0)
                .addPackage("org.springframework.bean", TicketClass.INTEGRATION, 1.0)
                .addPackage("org.springframework.jdbc", TicketClass.DATABASE, 1.0)
                .build();
        PackageTreeSearcher searcher = new PackageTreeSearcher(tree);
        classifier = new ExceptionReportClassifier(searcher);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithoutSearcher() {
        new ExceptionReportClassifier(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClassifyNullReport() {
        classifier.classify(null);
    }

    @Test
    public void testClassifyReportWithoutStackTrace() {
        ExceptionReport report = new ExceptionReport(
                "java.lang.IllegalArgumentException", "Some illegal argument in method", Collections.EMPTY_LIST, null);

        TicketClass category = classifier.classify(report);
        Assert.assertEquals(TicketClass.JVM, category);
    }

    @Test
    public void testClassifyReportWithCause() {
        ExceptionReport cause = new ExceptionReport(
                "java.lang.IllegalArgumentException", "Some illegal argument in method", Collections.EMPTY_LIST, null);
        ExceptionReport report = new ExceptionReport(
                "org.springframework.jdbc.DataAccessException", "Exception while accessing data", Collections.EMPTY_LIST, cause);

        TicketClass category = classifier.classify(report);
        Assert.assertEquals(TicketClass.JVM, category);
    }

    @Test
    public void testClassifyReportWithStackTrace() {
        StackTraceElement first = new StackTraceElement("org.springframework.jdbc.data.DataSourceLoader",
                "loadDataSource", "DataSourceLoader.java", 100);
        StackTraceElement second = new StackTraceElement("org.springframework.bean.BeanInitializer",
                "initializeBean", "BeanInitializer.java", 42);
        StackTraceElement third = new StackTraceElement("org.springframework.context.ApplicationContext", "init",
                "ApplicationContext.java", 256);
        List<StackTraceElement> stackTraceElements = Arrays.asList(first, second, third);

        ExceptionReport report = new ExceptionReport("java.lang.IllegalArgumentException", "Missing driver class name",
                stackTraceElements, null);

        TicketClass category = classifier.classify(report);
        Assert.assertEquals(TicketClass.INTEGRATION, category);
    }
}
