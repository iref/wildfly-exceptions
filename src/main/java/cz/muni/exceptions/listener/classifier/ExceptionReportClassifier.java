package cz.muni.exceptions.listener.classifier;

import com.google.common.base.Optional;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.source.ExceptionReport;

import java.util.EnumMap;
import java.util.Map;

/**
 * Class, that provides exception classification
 *
 * @author Jan Ferko
 */
public class ExceptionReportClassifier {

    /** Weight of exception class. */
    private static final double EXCEPTION_CLASS_COEFFICIENT = 1.2;

    /** Searcher for finding packages in trie. */
    private final PackageTreeSearcher searcher;

    /**
     * Creates new instance of classifier, that uses given searcher.
     *
     * @param searcher searcher for finding package classes.
     * @throws java.lang.IllegalArgumentException if searcher is {@code null}
     */
    public ExceptionReportClassifier(PackageTreeSearcher searcher) {
        if (searcher == null) {
            throw new IllegalArgumentException("[Searcher] is required and should not be null.");
        }

        this.searcher = searcher;
    }

    /**
     * Classifies report into one of categories.
     *
     * @param report report, that should be classified
     * @return class of report
     * @throws java.lang.IllegalArgumentException if report is {@code null}
     */
    public TicketClass classify(ExceptionReport report) {
        if (report == null) {
            throw new IllegalArgumentException("[Report] is required and should not be null");
        }

        if (report.getCause() != null) {
            return classify(report.getCause());
        }
        EnumMap<TicketClass, Double> classification = buildClassification(report);
        return max(classification);
    }

    /**
     * Builds report classification.
     * Classification contains categories where stack trace elements where classified
     * and score of each category for whole report.
     *
     * @param report report, for which classification should be built
     * @return map of categories and their score for given report.
     */
    private EnumMap<TicketClass, Double> buildClassification(ExceptionReport report) {
        EnumMap<TicketClass, Double> classification = new EnumMap<>(TicketClass.class);

        // search for category of exception class
        Optional<Node> exceptionClassClassification = searcher.search(report.getExceptionClass());
        if (exceptionClassClassification.isPresent()) {
            double weight = exceptionClassClassification.get().getWeight() * EXCEPTION_CLASS_COEFFICIENT;
            classification.put(exceptionClassClassification.get().getLabel(), weight);
        }

        double depthCoefficient = 1.0;
        // classify each element and store its score into result map
        for (StackTraceElement element : report.getStackTrace()) {
            Optional<Node> node = searcher.search(element.getClassName());
            if (node.isPresent()) {
                TicketClass label = node.get().getLabel();
                Double existingWeight = classification.get(label);
                double newWeight = node.get().getWeight() * depthCoefficient;
                double weight = existingWeight != null ? existingWeight + newWeight : newWeight;
                classification.put(label, weight);
            }
            depthCoefficient = Math.max(depthCoefficient - 0.05, 0.0);
        }
        return classification;
    }

    /**
     * Finds category with maximum score in given classification.
     *
     * @param classification report classification
     * @return category with maximum score
     */
    private TicketClass max(EnumMap<TicketClass, Double> classification) {
        double maxWeight = Double.MIN_VALUE;
        TicketClass maxWeightLabel = TicketClass.UNKNOWN;
        for (Map.Entry<TicketClass, Double> entry : classification.entrySet()) {
            if (!TicketClass.UNKNOWN.equals(entry.getKey()) && maxWeight < entry.getValue()) {
                maxWeightLabel = entry.getKey();
                maxWeight = entry.getValue();
            }
        }

        return maxWeightLabel;
    }
}
