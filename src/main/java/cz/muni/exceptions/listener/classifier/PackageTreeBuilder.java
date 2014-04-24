package cz.muni.exceptions.listener.classifier;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import cz.muni.exceptions.listener.db.model.TicketClass;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class builds package tree represented by {@link cz.muni.exceptions.listener.classifier.Node}.
 * Tree is based on trie data structure.
 *
 * @author Jan Ferko
 */
public class PackageTreeBuilder {

    /** Root node of tree. */
    private final BuilderNode root;

    /**
     * Constructor, creates new instance of builder. Dawwgh! :)
     */
    public PackageTreeBuilder() {
        root = new BuilderNode("", TicketClass.UNKNOWN, 0.0, Collections.EMPTY_SET);
    }

    /**
     * Adds new package to tree by building new path to existing tree.
     * Path shares nodes that have same token as one of package tokens.
     * If package already exists in tree, it's node attributes are updated to provided values.
     * Only last node on path has its attributes set to given values, inner nodes are empty (if they didn't exist before addition).
     *
     * New node is not created, if newPackage is empty or label is {@code null} or weight is negative.
     *
     * @param newPackage string represented by fully qualified name
     * @param label label, that classifies package into ticket class
     * @param weight classification weight of given package to given label
     * @return this instance of builder with added package
     */
    public PackageTreeBuilder addPackage(String newPackage, TicketClass label, double weight) {
        if (newPackage == null || newPackage.trim().isEmpty() || label == null || weight < 0.0) {
            return this;
        }

        // split by dot
        String[] tokens = newPackage.split("\\.");
        BuilderNode currentNode = root;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            // flag if token is last token in package name
            boolean isLast = i == tokens.length - 1;

            // find node with token
            Optional<BuilderNode> child = currentNode.lookupChild(token);
            if (child.isPresent()) {
                // traverse to child
                currentNode = child.get();

                // update last node to new values
                if (isLast) {
                    // update attributes
                    currentNode.token = token;
                    currentNode.ticketClass = label;
                    currentNode.weight = weight;
                }
            } else {
                // create new leaf
                TicketClass nodeLabel = isLast ? label : TicketClass.UNKNOWN;
                double nodeWeight = isLast ? weight : 0.0;
                BuilderNode newNode = new BuilderNode(token, nodeLabel, nodeWeight, Collections.EMPTY_SET);

                currentNode.addChild(newNode);
                currentNode = newNode;
            }
        }

        return this;
    }

    /**
     * Builds new package tree from added packages.
     *
     * @return new package tree
     */
    public Node build() {
        return root.build();
    }

    /**
     * Inner class, thats used to create mutable nodes of tree for easier modifications, while building tree.
     */
    private class BuilderNode {
        /** Token of node. */
        String token;

        /** Node label */
        TicketClass ticketClass;

        /** Node weight */
        double weight;

        /** Node childrens */
        Set<BuilderNode> children = new HashSet<>();

        /**
         * Costructs new instance.
         *
         * @param token node token
         * @param ticketClass node label
         * @param weight node weight
         * @param children node children
         */
        public BuilderNode(String token, TicketClass ticketClass, double weight, Collection<BuilderNode> children) {
            this.token = token;
            this.ticketClass = ticketClass;
            this.weight = weight;

            if (children != null && children.isEmpty()) {
                this.children.addAll(children);
            }
        }

        /**
         * Find child of this node with given token
         *
         * @param token child token
         * @return child node with given token or {@link com.google.common.base.Optional#absent()} if child doesn't exist
         */
        Optional<BuilderNode> lookupChild(String token) {
            for (BuilderNode node : children) {
                if (node.token.equalsIgnoreCase(token)) {
                    return Optional.of(node);
                }
            }

            return Optional.absent();
        }

        /**
         * Add new child node to this node.
         *
         * @param child child node
         * @return {@code true} if node was added, otherwise {@code false}
         */
        boolean addChild(BuilderNode child) {
            return this.children.add(child);
        }

        /**
         *  Builds new instance of Node from  this instance.
         *  Recursively builds Node for all children of this node.
         *
         * @return new instance of node created from this BuilderNode.
         */
        Node build() {
            Collection<Node> nodeChildren = Collections2.transform(children, new Function<BuilderNode, Node>() {
                @Override
                public Node apply(BuilderNode builderNode) {
                    return builderNode.build();
                }
            });

            return new Node(token, ticketClass, weight, nodeChildren);
        }
    }
}
