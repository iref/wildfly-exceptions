package cz.muni.exceptions.listener.classifier;

import com.google.common.base.Optional;

/**
 * @author Jan Ferko
 */
public class PackageTrieSearcher {

    private final Node root;

    public PackageTrieSearcher(Node root) {
        if (root == null) {
            throw new IllegalArgumentException("[Root] is required and should not be null.");
        }
        this.root = root;
    }

    public Optional<Node> lookup(String packageName) {
        if (packageName == null) {
            return Optional.absent();
        }

        String[] tokens = packageName.split("\\.");
        Node currentNode = root;
        for (String token : tokens) {
            Optional<Node> child = currentNode.lookupChild(token);
            if (child.isPresent()) {
                currentNode = child.get();
            } else {
                return Optional.of(currentNode);
            }
        }

        return Optional.absent();
    }
}
