package cz.muni.exceptions.listener.classifier;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import cz.muni.exceptions.listener.db.model.TicketClass;
import net.jcip.annotations.Immutable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Class represents one node in trie classification tree.
 *
 * @author Jan Ferko
 */
public class Node {

    /** Token, represents one part of package name */
    private final String token;

    /** Label, that matches package name built by path from root to this node. */
    private final TicketClass label;

    /** Weight of this node and its label. */
    private final double weight;

    /** Set of children of this node */
    private final Set<Node> children;

    /**
     * Constructor creates new node for given arguments.
     * If label is not provided, label of this node is {@link cz.muni.exceptions.listener.db.model.TicketClass#UNKNOWN}
     *
     * @param token token, represents one part of package name.
     * @param label label, that matches package name built by path from root to this node
     * @param weight weight of node
     * @param children collection of children of this node
     * @throws java.lang.IllegalArgumentException if {@code token} is {@code null} or {@code weight} is negative.
     */
    public Node(String token, TicketClass label, double weight, Collection<Node> children) {
        if (token == null) {
            throw new IllegalArgumentException("[Token] is required and should not be null.");
        }
        if (weight < 0.0) {
            throw new IllegalArgumentException("[Weight] shouldn't be negative number.");
        }
        this.token = token;
        this.label = label == null ? TicketClass.UNKNOWN : label;
        this.weight = weight;

        if (children != null && !children.isEmpty()) {
            this.children = ImmutableSet.copyOf(children);
        } else {
            this.children = ImmutableSet.of();
        }
    }

    /**
     * Returns token of this node.
     *
     * @return token of this node
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns label of this node.
     *
     * @return label of this node
     */
    public TicketClass getLabel() {
        return label;
    }

    /**
     * Returns weight of this node for its label.
     *
     * @return weight of this node for its label
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Returns immutable set of children nodes.
     *
     * @return immutable set of children nodes
     */
    public Set<Node> getChildren() {
        return ImmutableSet.copyOf(children);
    }

    /**
     * Indicates if this node is leaf of tree.
     *
     * @return {@code true} if node is leaf, otherwise {@code false}
     */
    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    /**
     * Lookup children with given token.
     *
     * @param token child token
     * @return child with given token or {@code Optional.absent()} if child was not found.
     */
    public Optional<Node> lookupChild(String token) {
        Optional<Node> result = Optional.absent();

        if (token == null || token.trim().isEmpty() || isLeaf()) {
            return result;
        }

        String trimmedToken = token.trim();
        for (Node child : getChildren()) {
            if (child.getToken().equalsIgnoreCase(trimmedToken)) {
                result = Optional.of(child);
                break;
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) {
            return false;
        }

        Node other = (Node) obj;
        return Objects.equals(token, other.token) && Objects.equals(label, other.label)
                && Objects.equals(weight, other.weight) && Objects.equals(children, other.children);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = token.hashCode();
        result = 31 * result + label.hashCode();
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + children.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("Node {token=%1$s, label=%2$s, weight=%3$.2f, children=%4$s}",
                token, label, weight, children);
    }
}
