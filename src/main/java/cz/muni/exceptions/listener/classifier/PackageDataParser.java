package cz.muni.exceptions.listener.classifier;

import java.io.InputStream;

/**
 * Interface, that provides parsing of package info data to package tree.
 *
 * @author Jan Ferko
 */
public interface PackageDataParser {

    /**
     * Parses data about packages into package tree.
     *
     * @param dataStream input stream, that contains data about packages
     * @return Root node of package tree
     * @throws java.lang.IllegalArgumentException if {@code dataStream} is {@code null}
     * @throws java.lang.IllegalStateException if parser hit invalid data format in stream
     * @throws java.lang.RuntimeException if there is any other error while manipulating with stream
     */
    Node parseInput(InputStream dataStream);
}
