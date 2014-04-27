package cz.muni.exceptions.listener.classifier;

import com.google.common.base.Optional;
import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.source.ExceptionReport;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Jan Ferko
 */
public class ExceptionReportClassifier {

    private static final double EXCEPTION_CLASS_COEFFICIENT = 1.2;

    private final PackageTreeSearcher searcher;

    public ExceptionReportClassifier(PackageTreeSearcher searcher) {
        if (searcher == null) {
            throw new IllegalArgumentException("[Searcher] is required and should not be null.");
        }

        this.searcher = searcher;
    }

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

    private EnumMap<TicketClass, Double> buildClassification(ExceptionReport report) {
        EnumMap<TicketClass, Double> classification = new EnumMap<>(TicketClass.class);

        Optional<Node> exceptionClassClassification = searcher.search(report.getExceptionClass());
        if (exceptionClassClassification.isPresent()) {
            double weight = exceptionClassClassification.get().getWeight() * EXCEPTION_CLASS_COEFFICIENT;
            classification.put(exceptionClassClassification.get().getLabel(), weight);
        }

        double depthCoefficient = 1.0;
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
