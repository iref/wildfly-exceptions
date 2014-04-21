package cz.muni.exceptions.dispatcher;

import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple filter class, that checks if exception is on blaclist and should not be processed.
 *
 * @author Jan Ferko
 */
public class BlacklistFilter {

    /** Set of compiled blacklist, representing blacklist. */
    private final Set<Pattern> blacklist;

    /**
     * Constructor creates new instance of blacklist filter for given patterns,
     * If patterns are empty it creates empty filter
     *
     * @param patterns collection of patterns, that are supposed to be filtered by blacklist
     */
    public BlacklistFilter(Collection<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            this.blacklist = ImmutableSet.<Pattern>of();
        } else {
            this.blacklist = buildPatterns(patterns);
        }
    }

    /**
     * Returns set of compiled patterns.
     *
     * @param patterns strings representing patterns
     * @return immutable set of compiled regular expressions patterns
     */
    private Set<Pattern> buildPatterns(Collection<String> patterns) {
        ImmutableSet.Builder<Pattern> builder = ImmutableSet.builder();

        for (String pattern : patterns) {
            if (pattern == null || pattern.trim().isEmpty()) {
                continue;
            }

            builder.add(buildPattern(pattern.trim()));
        }

        return builder.build();
    }

    /**
     * Builds pattern from value.
     * It escapes every special character in pattern except '*'.
     *
     * @param value string value of pattern
     * @return compiled regular expression pattern
     */
    private Pattern buildPattern(String value) {
        String[] fragments = value.split("\\*");
        StringBuilder patternBuilder = new StringBuilder();
        for (int i = 0; i < fragments.length; i++) {
            String fragment = fragments[i];
            patternBuilder.append(Pattern.quote(fragment));

            if (i < fragments.length - 1) {
                patternBuilder.append(".*");
            }
        }

        // if pattern ends with * add one more .* to the end of RegExp
        if (value.endsWith("*")) {
            patternBuilder.append(".*");
        }
        return Pattern.compile(patternBuilder.toString());
    }

    /**
     * Checks if given exception class name is on blacklist.
     *
     * @param exceptionClassName fully qualified name of exception
     * @return {@code true} if class name matches one or more patterns in blacklist, otherwise {@code false}
     */
    public boolean isOnBlacklist(String exceptionClassName) {
        if (exceptionClassName == null || exceptionClassName.isEmpty()) {
            return false;
        }

        boolean onBlacklist = false;

        for (Pattern pattern : blacklist) {
            Matcher matcher = pattern.matcher(exceptionClassName);
            if (matcher.matches()) {
                onBlacklist = true;
                break;
            }
        }

        return onBlacklist;
    }
}
