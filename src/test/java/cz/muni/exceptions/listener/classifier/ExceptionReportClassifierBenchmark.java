package cz.muni.exceptions.listener.classifier;

import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.source.ExceptionReport;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jan Ferko
 */
public class ExceptionReportClassifierBenchmark {

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        new ExceptionReportClassifierBenchmark().run();
    }

    public void run() throws IOException, SAXException, ParserConfigurationException {
        ExceptionReportsParser.ExceptionReportsDataSet reportsData = new ExceptionReportsParser()
                .parseDatSet("classifier/exceptions_dataset.xml");

        StaxPackageDataParser packageDataParser = new StaxPackageDataParser();
        Node tree = packageDataParser.parseInput(getClass().getResourceAsStream("data/packages.xml"));
        PackageTreeSearcher searcher = new PackageTreeSearcher(tree);
        ExceptionReportClassifier classifier = new ExceptionReportClassifier(searcher);

        List<TicketClass> actualLabels = new ArrayList<>();
        for (ExceptionReport report : reportsData.getReports()) {
            actualLabels.add(classifier.classify(report));
        }

        int correctlyClassified = 0;
        for (int i = 0; i < actualLabels.size(); i++) {
            TicketClass actual = actualLabels.get(i);
            TicketClass expected = reportsData.getExpectedLabels().get(i);

            if (expected.equals(actual)) {
                correctlyClassified++;
            }
        }

        double totalNumberOfReports = reportsData.getExpectedLabels().size();
        System.out.println("Total number of reports: " + totalNumberOfReports);
        System.out.print("Correctly classified reports: " + correctlyClassified);
        System.out.print("Accuracy: " + (correctlyClassified / totalNumberOfReports));
    }

}
