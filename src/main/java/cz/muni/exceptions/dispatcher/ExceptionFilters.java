package cz.muni.exceptions.dispatcher;

import cz.muni.exceptions.source.ExceptionReport;

/**
 * Enum of simple stateless exception filters.
 *
 * @author Jan Ferko
 */
public enum ExceptionFilters implements ExceptionFilter {

    /** Filter, that filters every report. */
    ALWAYS_FILTERED("alwaysFiltered") {
        @Override
        public boolean apply(ExceptionReport exceptionReport) {
            return true;
        }
    },

    /** Dummy filter, that does not filter any report */
    ALWAYS_PASSES("alwaysPasses") {
        @Override
        public boolean apply(ExceptionReport exceptionReport) {
            return false;
        }
    };

    /** Key of enum element */
    private String key;

    private ExceptionFilters(String key) {
        this.key = key;
    }

    /**
     * Returns key of enum element.
     *
     * @return key of enum element
     */
    public String getKey() {
        return this.key;
    }
}
