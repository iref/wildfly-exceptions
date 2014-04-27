package cz.muni.exceptions.listener.classifier;

import cz.muni.exceptions.listener.db.model.TicketClass;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Jan Ferko
 */
public class PackageTreeBuilderTest {

    @Test
    public void testAddPackageWithNullName() {
        Node root = new PackageTreeBuilder()
                .addPackage(null, TicketClass.DATABASE, 1.0)
                .build();
        Assert.assertTrue(root.isLeaf());
    }

    @Test
    public void testAddPackageWithEmptyToken() {
        Node root = new PackageTreeBuilder()
                .addPackage("", TicketClass.DATABASE, 1.0)
                .build();
        Assert.assertTrue(root.isLeaf());
    }

    @Test
    public void testAddPackageWithNullLabel() {
        Node root = new PackageTreeBuilder()
                .addPackage("org.jboss.logging", null, 1.0)
                .build();
        Assert.assertTrue(root.isLeaf());
    }

    @Test
    public void testAddPackageWithNegativeWeight() {
        Node root = new PackageTreeBuilder()
                .addPackage("org.jboss.logging", null, -1.0)
                .build();
        Assert.assertTrue(root.isLeaf());
    }

    @Test
    public void testBuildSinglePackageTree() {
        Node root = new PackageTreeBuilder()
                .addPackage("org.jboss.logging", TicketClass.UTILS, 1.0)
                .build();
        Assert.assertEquals(1, root.getChildren().size());

        Node thirdTokenNode = new Node("logging", TicketClass.UTILS, 1.0, Collections.EMPTY_LIST);
        Node secondTokenNode = new Node("jboss", TicketClass.UNKNOWN, 0.0, Collections.singleton(thirdTokenNode));
        Node firstTokenNode = new Node("org", TicketClass.UNKNOWN, 0.0, Collections.singleton(secondTokenNode));

        Assert.assertEquals(firstTokenNode, root.getChildren().iterator().next());
    }

    @Test
    public void testBuildPackageTreeForPackageWithSamePrefix() {
        Node root = new PackageTreeBuilder()
                .addPackage("org.jboss.logging", TicketClass.UTILS, 1.0)
                .addPackage("org.jboss.as", TicketClass.INTEGRATION, 1.0)
                .build();

        Assert.assertEquals(1, root.getChildren().size());

        Node thirdTokenANode = new Node("as", TicketClass.INTEGRATION, 1.0, Collections.EMPTY_LIST);
        Node thirdTokenBNode = new Node("logging", TicketClass.UTILS, 1.0, Collections.EMPTY_LIST);
        Node secondTokenNode = new Node("jboss", TicketClass.UNKNOWN, 0.0, Arrays.asList(thirdTokenANode, thirdTokenBNode));
        Node firstTokenNode = new Node("org", TicketClass.UNKNOWN, 0.0, Collections.singleton(secondTokenNode));

        Assert.assertEquals(firstTokenNode, root.getChildren().iterator().next());
    }

    @Test
    public void testBuildPackageTreeForOnePackageIsPrefixOfOther() {
        Node root = new PackageTreeBuilder()
                .addPackage("org.jboss.logging", TicketClass.UTILS, 1.0)
                .addPackage("org.jboss", TicketClass.INTEGRATION, 0.5)
                .build();

        Assert.assertEquals(1, root.getChildren().size());

        Node thirdTokenNode = new Node("logging", TicketClass.UTILS, 1.0, Collections.EMPTY_LIST);
        Node secondTokenNode = new Node("jboss", TicketClass.INTEGRATION, 0.5, Arrays.asList(thirdTokenNode));
        Node firstTokenNode = new Node("org", TicketClass.UNKNOWN, 0.0, Collections.singleton(secondTokenNode));

        Assert.assertEquals(firstTokenNode, root.getChildren().iterator().next());
    }

    @Test
    public void testBuildPackageTreeForTwoDifferentPackages() {
        Node root = new PackageTreeBuilder()
                .addPackage("org.jboss.logging", TicketClass.UTILS,  1.0)
                .addPackage("com.google.guava", TicketClass.INTEGRATION, 2.0)
                .build();
        Assert.assertEquals(2, root.getChildren().size());

        Node thirdATokenNode = new Node("logging", TicketClass.UTILS, 1.0, Collections.EMPTY_LIST);
        Node secondATokenNode = new Node("jboss", TicketClass.UNKNOWN, 0.0, Collections.singleton(thirdATokenNode));
        Node firstATokenNode = new Node("org", TicketClass.UNKNOWN, 0.0, Collections.singleton(secondATokenNode));

        Node thirdBTokenNode = new Node("guava", TicketClass.INTEGRATION, 2.0, Collections.EMPTY_SET);
        Node secondBTokenNode = new Node("google", TicketClass.UNKNOWN, 0.0, Collections.singleton(thirdBTokenNode));
        Node firstBTokenNode = new Node("com", TicketClass.UNKNOWN, 0.0, Collections.singleton(secondBTokenNode));

        Assert.assertTrue(root.getChildren().contains(firstATokenNode));
        Assert.assertTrue(root.getChildren().contains(firstBTokenNode));
    }

    @Test
    public void testBuildPackageTreeWithTwoSamePackages() {
        Node root = new PackageTreeBuilder()
                .addPackage("org.jboss.logging", TicketClass.UTILS, 1.0)
                .addPackage("org.jboss.logging", TicketClass.JVM,  2.0)
                .build();
        Assert.assertEquals(1, root.getChildren().size());

        Node thirdTokenNode = new Node("logging", TicketClass.JVM, 2.0, Collections.EMPTY_LIST);
        Node secondTokenNode = new Node("jboss", TicketClass.UNKNOWN, 0.0, Collections.singleton(thirdTokenNode));
        Node firstTokenNode = new Node("org", TicketClass.UNKNOWN, 0.0, Collections.singleton(secondTokenNode));

        Assert.assertEquals(firstTokenNode, root.getChildren().iterator().next());
    }
}
