package cz.muni.exceptions.listener.duplication;

import cz.muni.exceptions.source.ExceptionReport;

import java.util.List;

/**
 * Interface, that can check two list of Ts on similarity.
 *
 * @author Jan Ferko
 */
public interface SimilarityChecker {

    /**
     * Method computes similarity of two lists of T.
     * If one list is {@code null} or empty, size of second list is returned as similarity score.
     * Elements of lists are compared using {@code equals} method, so results are dependend of its implementation.
     *
     * @param leftWord first word to be compared
     * @param rightWord second word to be compared
     * @param <T> type of element of given lists.
     * @return similarity score, represented as number of changes, that are needed for {@code leftWord} to become {@code rightWord}.
     *  If 0 is returned two lists are identical.
     */
    <T> int checkSimilarity(List<T> leftWord, List<T> rightWord);
}
