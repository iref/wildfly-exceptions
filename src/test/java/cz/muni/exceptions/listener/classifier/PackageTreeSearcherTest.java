package cz.muni.exceptions.listener.classifier;

import com.google.common.base.Optional;
import cz.muni.exceptions.listener.db.model.TicketClass;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Jan Ferko
 */
public class PackageTreeSearcherTest {

    private Node tree;

    private PackageTreeSearcher treeSearcher;

    @Before
    public void setUp() {
        tree = new PackageTreeBuilder()
                .addPackage("org.jboss.logging", TicketClass.UTILS, 1.0)
                .addPackage("org.jboss.as", TicketClass.INTEGRATION, 2.0)
                .addPackage("javax.sql", TicketClass.DATABASE, 2.0)
                .build();
        treeSearcher = new PackageTreeSearcher(tree);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSearcherForNullTree() {
        new PackageTreeSearcher(null);
    }

    @Test
    public void testSearchForExistingNode() {
        Optional<Node> actual = treeSearcher.search("org.jboss.logging");
        Assert.assertTrue(actual.isPresent());
        Node actualNode = actual.get();

        Assert.assertEquals(TicketClass.UTILS, actualNode.getLabel());
        Assert.assertEquals("logging", actualNode.getToken());
        Assert.assertEquals(1.0, actualNode.getWeight());
        Assert.assertEquals(0, actualNode.getChildren().size());
    }

    @Test
    public void testSearchForClosestPrefixNode() {
        Optional<Node> actual = treeSearcher.search("org.jboss.arquillian.internal");
        Assert.assertTrue(actual.isPresent());

        Node actualNode = actual.get();
        Assert.assertEquals(TicketClass.UNKNOWN, actualNode.getLabel());
        Assert.assertEquals("jboss", actualNode.getToken());
        Assert.assertEquals(0.0, actualNode.getWeight());
        Assert.assertEquals(2, actualNode.getChildren().size());
    }

    @Test
    public void testSearchForNonexistentPackage() {
        Optional<Node> actual = treeSearcher.search("ch.logback");
        Assert.assertTrue(actual.isPresent());
        Assert.assertEquals(tree, actual.get());
    }

    @Test
    public void testSearchForNullPackage() {
        Optional<Node> actual = treeSearcher.search(null);
        Assert.assertFalse(actual.isPresent());
    }
}
