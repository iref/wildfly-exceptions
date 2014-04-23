package cz.muni.exceptions.dispatcher;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Jan Ferko
 */
public class BlacklistFilterTest {

    private static final String[] TEST_PATTERNS = new String[] {
            "java.lang.IllegalArgumentException", "java.lang.*",
            "java.*.IllegalArgumentException", "*.IllegalArgumentException"
    };

    @Test
    public void testIsEmptyStringOnBlacklist() {
        BlacklistFilter filter = new BlacklistFilter(null);
        String[] emptyExceptionClassNames = new String[]{"", null, "  ", " "};
        for (String emptyExceptionClassName : emptyExceptionClassNames) {
            Assert.assertFalse(filter.isOnBlacklist(emptyExceptionClassName));
        }

        filter = new BlacklistFilter(Arrays.asList(TEST_PATTERNS));
        for (String emptyExceptionClassName : emptyExceptionClassNames) {
            Assert.assertFalse(filter.isOnBlacklist(emptyExceptionClassName));
        }
    }

    @Test
    public void testIsOnBlacklistWithStar() {
        BlacklistFilter filter = new BlacklistFilter(Arrays.asList("*"));
        Assert.assertTrue(filter.isOnBlacklist("java.lang.IllegalArgumentException"));
        Assert.assertTrue(filter.isOnBlacklist("java.lang.NullPointerException"));
        Assert.assertTrue(filter.isOnBlacklist("javax.persistence.TransactionRequiredException"));
    }

    @Test
    public void testIsOnEmptyBlacklistEmptyFilter() {
        BlacklistFilter filter = new BlacklistFilter(null);
        Assert.assertFalse(filter.isOnBlacklist("java.lang.IllegalArgumentException"));

        filter = new BlacklistFilter(Collections.<String>emptyList());
        Assert.assertFalse(filter.isOnBlacklist("java.lang.IllegalArgumentException"));
    }

    @Test
    public void testIsOnBlacklistWithExactMatch() {
        BlacklistFilter filter = new BlacklistFilter(Arrays.asList(TEST_PATTERNS[0]));

        Assert.assertTrue(filter.isOnBlacklist("java.lang.IllegalArgumentException"));
        Assert.assertFalse(filter.isOnBlacklist("java.lang.IllegalStateException"));
    }

    @Test
    public void testIsOnBlacklistWithWildcardAtEnd() {
        BlacklistFilter filter = new BlacklistFilter(Arrays.asList(TEST_PATTERNS[1]));

        Assert.assertTrue(filter.isOnBlacklist("java.lang.IllegalArgumentException"));
        Assert.assertTrue(filter.isOnBlacklist("java.lang.IllegalStateException"));
        Assert.assertFalse(filter.isOnBlacklist("javax.persistence.TransactionRequiredException"));
        Assert.assertFalse(filter.isOnBlacklist("java.util.logging.InvalidHandlerRegisteredException"));
    }

    @Test
    public void testIsOnBlacklistWithWildcardInMiddle() {
        BlacklistFilter filter = new BlacklistFilter(Arrays.asList(TEST_PATTERNS[2]));

        Assert.assertTrue(filter.isOnBlacklist("java.lang.IllegalArgumentException"));
        Assert.assertTrue(filter.isOnBlacklist("java.some.weird.package.IllegalArgumentException"));
        Assert.assertFalse(filter.isOnBlacklist("java.lang.IllegalStateException"));
        Assert.assertFalse(filter.isOnBlacklist("java.util.logging.InvalidHandlerRegisteredException"));
    }

    @Test
    public void testIsOnBlacklistWithWildcardAtBeginning() {
        BlacklistFilter filter = new BlacklistFilter(Arrays.asList(TEST_PATTERNS[3]));

        Assert.assertTrue(filter.isOnBlacklist("java.lang.IllegalArgumentException"));
        Assert.assertTrue(filter.isOnBlacklist("java.some.weird.package.IllegalArgumentException"));
        Assert.assertFalse(filter.isOnBlacklist("java.lang.IllegalStateException"));
        Assert.assertFalse(filter.isOnBlacklist("java.util.logging.InvalidHandlerRegisteredException"));
    }
}
