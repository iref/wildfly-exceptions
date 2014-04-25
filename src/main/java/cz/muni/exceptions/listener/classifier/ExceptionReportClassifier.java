package cz.muni.exceptions.listener.classifier;

import com.google.common.base.Optional;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.source.ExceptionReport;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Jan Ferko
 */
public class ExceptionReportClassifier {

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

        EnumMap<TicketClass, Double> classification = buildClassification(report);
        return max(classification);
    }

    private EnumMap<TicketClass, Double> buildClassification(ExceptionReport report) {
        EnumMap<TicketClass, Double> classification = new EnumMap<>(TicketClass.class);
        for (StackTraceElement element : report.getStackTrace()) {
            Optional<Node> node = searcher.search(element.getClassName());
            if (node.isPresent()) {
                TicketClass label = node.get().getLabel();
                Double existingWeight = classification.get(label);
                double weight = existingWeight != null ? existingWeight + node.get().getWeight() : node.get().getWeight();

                classification.put(label, weight);
            }
        }
        return classification;
    }

    private TicketClass max(EnumMap<TicketClass, Double> classification) {
        double maxWeight = Double.MIN_VALUE;
        TicketClass maxWeightLabel = TicketClass.UNKNOWN;
        for (Map.Entry<TicketClass, Double> entry : classification.entrySet()) {
            if (maxWeight < entry.getValue()) {
                maxWeightLabel = entry.getKey();
                maxWeight = entry.getValue();
            }
        }

        return maxWeightLabel;
    }
}
