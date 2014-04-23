package cz.muni.exceptions.listener.classifier;

import com.google.common.base.Optional;
import cz.muni.exceptions.listener.db.model.TicketClass;

import java.util.Collections;

/**
 * @author Jan Ferko
 */
public class PackageTrieBuilder {

    private final Node root;

    public PackageTrieBuilder() {
        root = new Node("", TicketClass.UNKNOWN, 0.0, Collections.EMPTY_SET);
    }

    public PackageTrieBuilder addPackage(String newPackage, TicketClass label, double weight) {
        if (newPackage == null || newPackage.trim().isEmpty()) {
            return this;
        }

        String[] tokens = newPackage.split("\\.");
        Node currentNode = root;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            boolean isLast = i == tokens.length - 1;

            Optional<Node> child = currentNode.lookupChild(token);
            if (child.isPresent()) {
                // traverse to child
                currentNode = child.get();
            } else {
                // create new leaf
                TicketClass nodeLabel = isLast ? label : TicketClass.UNKNOWN;
                double nodeWeight = isLast ? weight : 0.0;
                Node newNode = new Node(token, nodeLabel, nodeWeight, Collections.EMPTY_SET);

                currentNode.addChild(newNode);
                currentNode = newNode;
            }
        }

        return this;
    }

    public PackageTrieSearcher build() {
        return new PackageTrieSearcher(root);
    }
}
