package cz.muni.exceptions.dispatcher;

import cz.muni.exceptions.source.ExceptionReport;

/**
 * Enum of simple stateless exception filters.
 *
 * @author Jan Ferko
 */
public enum ExceptionFilters implements ExceptionFilter{

    ALWAYS_FILTERED("alwaysFiltered") {
        @Override
        public boolean apply(ExceptionReport exceptionReport) {
            return true;
        }
    },

    ALWAYS_PASSES("alwaysPasses") {
        @Override
        public boolean apply(ExceptionReport exceptionReport) {
            return false;
        }
    };

    private String key;

    private ExceptionFilters(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}
