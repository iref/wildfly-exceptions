package cz.muni.exceptions.dispatcher;

import cz.muni.exceptions.source.ExceptionReport;

/**
 * Simple interface, that allows to define exception report filtering
 *
 * @author Jan Ferko
 */
public interface ExceptionFilter {

    /**
     * Method applies filter on exception report.
     *
     * @param exceptionReport report, that should be filtered.
     * @return {@code true} if exception is filtered, otherwise {@code false}.
     */
    boolean apply(ExceptionReport exceptionReport);
}
