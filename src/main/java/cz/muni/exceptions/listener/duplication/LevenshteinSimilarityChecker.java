package cz.muni.exceptions.listener.duplication;

import com.google.common.collect.Ordering;

import java.util.List;

/**
 * Implementation of similarity checker using Levenshtein distance algorithm.
 *
 * @see http://en.wikipedia.org/wiki/Levenshtein_distance
 * @author Jan Ferko
 */
public class LevenshteinSimilarityChecker implements SimilarityChecker {

    @Override
    public <T> int checkSimilarity(List<T> leftWord, List<T> rightWord) {
        if (leftWord == null || leftWord.isEmpty()) {
            return rightWord == null ? 0 : rightWord.size();
        }
        if (rightWord == null || rightWord.isEmpty()) {
            return leftWord.size();
        }

        int leftSize = leftWord.size() + 1;
        int rightSize = rightWord.size() + 1;

        int[][] similarityMatrix = new int[leftSize][rightSize];

        // init all indexes for first stacktrace
        for (int i = 1; i < leftSize; i++) {
            similarityMatrix[i][0] = i;
        }

        // init all indexes for second stacktrace
        for (int j = 0; j < rightSize; j++) {
            similarityMatrix[0][j] = j;
        }

        // compute matrix
        for (int j = 1; j < rightSize; j++) {
            for (int i = 1; i < leftSize; i++) {
                if (leftWord.get(i - 1).equals(rightWord.get(j - 1))) {
                    similarityMatrix[i][j] = similarityMatrix[i-1][j-1];
                } else {
                    int deletionCount = similarityMatrix[i - 1][j] + 1;
                    int insertionCount = similarityMatrix[i][j - 1] + 1;
                    int substitutionCount = similarityMatrix[i - 1][j - 1] + 1;

                    similarityMatrix[i][j] = Ordering.natural().min(deletionCount, insertionCount, substitutionCount);
                }
            }
        }

        // return final distance from bottom right column
        return similarityMatrix[leftSize - 1][rightSize - 1];
    }
}
