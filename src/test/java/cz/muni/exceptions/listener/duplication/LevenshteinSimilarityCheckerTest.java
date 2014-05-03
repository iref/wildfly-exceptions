package cz.muni.exceptions.listener.duplication;

import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Jan Ferko
 */
public class LevenshteinSimilarityCheckerTest {

    private LevenshteinSimilarityChecker checker = new LevenshteinSimilarityChecker();

    @Test
    public void testCheckLeftEmptyOrNull() {
        List<Character> left = Lists.<Character>newArrayList();

        List<Character> right = Arrays.asList('T', 'e', 's', 't');
        Assert.assertEquals(4, checker.checkSimilarity(left, right));
        Assert.assertEquals(4, checker.checkSimilarity(null, right));
    }

    @Test
    public void testCheckRightEmptyOrNull() {
        List<Character> right = Lists.<Character>newArrayList();
        List<Character> left = Arrays.asList('T', 'e', 's', 't');
        Assert.assertEquals(4, checker.checkSimilarity(left, right));
        Assert.assertEquals(4, checker.checkSimilarity(left, null));
    }

    @Test
    public void testBothNull() {
        Assert.assertEquals(0, checker.checkSimilarity(null, null));
    }

    @Test
    public void testCheckSameString() {
        List<Character> right = Lists.<Character>newArrayList('T', 'e', 's', 't');
        Assert.assertEquals(0, checker.checkSimilarity(right, right));
    }

    @Test
    public void testCheckWithOneInsertion() {
        List<Character> left = Lists.<Character>newArrayList('s', 'i', 't', 't', 'i', 'n');
        List<Character> right = Lists.<Character>newArrayList('s', 'i', 't', 't', 'i', 'n', 'g');
        Assert.assertEquals(1, checker.checkSimilarity(left, right));
    }

    @Test
    public void testCheckWithOneDeletion() {
        List<Character> left = Lists.<Character>newArrayList('s', 'i', 't', 't', 'i', 'n', 'g');
        List<Character> right = Lists.<Character>newArrayList('s', 'i', 't', 't', 'i', 'n');
        Assert.assertEquals(1, checker.checkSimilarity(left, right));
    }

    @Test
    public void testCheckWithOneSubstitution() {
        List<Character> left = Lists.<Character>newArrayList('s', 'i', 't');
        List<Character> right = Lists.<Character>newArrayList('s', 'y', 't');
        Assert.assertEquals(1, checker.checkSimilarity(left, right));
    }

    @Test
    public void testCheckWithMultipleChanges() {
        List<Character> left = Lists.<Character>newArrayList('k', 'i', 't', 't', 'e', 'n');
        List<Character> right = Lists.<Character>newArrayList('s', 'i', 't', 't', 'i', 'n', 'g');
        Assert.assertEquals(3, checker.checkSimilarity(left, right));
    }
}
