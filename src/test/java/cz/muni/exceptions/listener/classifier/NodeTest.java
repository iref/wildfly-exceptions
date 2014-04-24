package cz.muni.exceptions.listener.classifier;

import com.google.common.base.Optional;
import cz.muni.exceptions.listener.db.model.TicketClass;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Jan Ferko
 */
public class NodeTest {

    private Node root;

    private Node leaf;

    @Before
    public void setUp() {
        leaf = new Node("jboss", TicketClass.INTEGRATION, 2.0, Collections.EMPTY_LIST);
        Node child2 = new Node("springframework", TicketClass.INTEGRATION, 1.0, Collections.EMPTY_LIST);
        root = new Node("org", TicketClass.UNKNOWN, 0.0, Arrays.asList(leaf, child2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithNullToken() {
        new Node(null, TicketClass.DATABASE, 1.0, Collections.EMPTY_LIST);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithNegativeWeight() {
        new Node("org", TicketClass.DATABASE, -1.0, Collections.EMPTY_LIST);
    }

    @Test
    public void testConstructWithoutLabel() {
        Node node = new Node("org", null, 1.0, Collections.EMPTY_LIST);
        Assert.assertEquals(TicketClass.UNKNOWN, node.getLabel());
    }

    @Test
    public void testIsLeaf() {
        Node node = new Node("org", TicketClass.DATABASE, 1.0, Collections.EMPTY_LIST);
        Assert.assertTrue(node.isLeaf());
    }

    @Test
    public void testIsNotLeafIfHasChildren() {
        Assert.assertFalse(root.isLeaf());
    }

    @Test
    public void testLookupChild() {
        Optional<Node> actualChild = root.lookupChild("jboss");
        Assert.assertTrue(actualChild.isPresent());
        Assert.assertEquals(leaf, actualChild.get());
    }

    @Test
    public void testLookupNonExistingChild() {
        Optional<Node> actualChild = root.lookupChild("log4j");
        Assert.assertFalse(actualChild.isPresent());
    }

    @Test
    public void testLookupNullString() {
        Optional<Node> actualChild = root.lookupChild(null);
        Assert.assertFalse(actualChild.isPresent());
    }

    @Test
    public void testLookupEmptyString() {
        Optional<Node> actualChild = root.lookupChild("");
        Assert.assertFalse(actualChild.isPresent());
    }

    @Test
    public void testLookupChildOfLeaf() {
        Optional<Node> actualChild = leaf.lookupChild("logging");
        Assert.assertFalse(actualChild.isPresent());
    }

    @Test
    public void testLookupChildByTokenWithWhitespaces() {
        Optional<Node> actualChild = root.lookupChild(" jboss ");
        Assert.assertTrue(actualChild.isPresent());
        Assert.assertEquals(actualChild.get(), leaf);
    }
}
