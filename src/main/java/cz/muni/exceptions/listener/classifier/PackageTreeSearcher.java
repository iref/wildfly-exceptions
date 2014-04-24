package cz.muni.exceptions.listener.classifier;

import com.google.common.base.Optional;

/**
 * Class provides searching of given tree.
 *
 * @author Jan Ferko
 */
public class PackageTreeSearcher {

    /** Root node of tree. */
    private final Node root;

    /**
     * Constructor creates new instance of searcher for given tree.
     *
     * @param root tree represented by its root
     * @throws java.lang.IllegalArgumentException if root is {@code null}
     */
    public PackageTreeSearcher(Node root) {
        if (root == null) {
            throw new IllegalArgumentException("[Root] is required and should not be null.");
        }
        this.root = root;
    }

    /**
     * Searches tree for node, that represents closest match to given package name.
     *
     * @param packageName fully qualified name of package
     * @return option with node, that represents the closest match or {@link com.google.common.base.Optional#absent()}
     *  if package name was {@code null}.
      */
    public Optional<Node> search(String packageName) {
        if (packageName == null) {
            return Optional.absent();
        }

        String[] tokens = packageName.split("\\.");
        Node currentNode = root;
        for (String token : tokens) {
            Optional<Node> child = currentNode.lookupChild(token);
            if (!child.isPresent()) {
                break;
            }
            currentNode = child.get();
        }

        return Optional.of(currentNode);
    }
}