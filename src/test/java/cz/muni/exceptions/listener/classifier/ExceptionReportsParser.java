package cz.muni.exceptions.listener.classifier;

import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.source.ExceptionReport;
import org.dom4j.DocumentFactory;
import org.w3c.dom.*;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jan Ferko
 */
final class ExceptionReportsParser {

    public ExceptionReportsDataSet parseDatSet(String filePath) throws ParserConfigurationException, IOException, SAXException {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = dbFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(is);

        doc.getDocumentElement().normalize();

        List<ExceptionReport> reports = new ArrayList<>();
        List<TicketClass> labels = new ArrayList<>();

        NodeList exceptions = doc.getElementsByTagName("exception");
        for (int i = 0; i < exceptions.getLength(); i++) {
            Node item = exceptions.item(i);

            Element element = (Element) item;
            String className = element.getAttribute("class");
            String message = element.getAttribute("message");
            TicketClass label = TicketClass.forKey(element.getAttribute("label"));

            String stackTraceValue = element.getElementsByTagName("stacktrace").item(0).getTextContent();
            List<StackTraceElement> stackTraces = buildStackTrace(stackTraceValue);

            reports.add(new ExceptionReport(className, message, stackTraces, null));
            labels.add(label);
        }

        is.close();

        return new ExceptionReportsDataSet(reports, labels);
    }

    private List<StackTraceElement> buildStackTrace(String stacktrace) {
        Pattern pattern = Pattern.compile("at (.+)\\((.+?)\\:*(\\d*)\\)");
        Matcher matcher = pattern.matcher(stacktrace);

        List<StackTraceElement> elements = new ArrayList<>();

        while (matcher.find()) {
            String classNameAndMethod = matcher.group(1);
            String[] split = classNameAndMethod.split("\\.");

            String method = split[split.length - 1];
            String[] classNames = Arrays.copyOfRange(split, 0, split.length - -1);
            StringBuilder classNameBuilder = new StringBuilder();
            for (String fragment : classNames) {
                classNameBuilder.append(fragment).append(".");
            }

            String fileName = matcher.group(2);
            String lineNumberValue = matcher.group(3);
            int lineNumber = 0;
            if (!lineNumberValue.isEmpty()) {
                Integer.parseInt(lineNumberValue);
            }

            StackTraceElement element = new StackTraceElement(classNameBuilder.toString(), method, fileName, lineNumber);
            elements.add(element);
        }

        return elements;
    }

    final class ExceptionReportsDataSet {
        private final List<ExceptionReport> reports;

        private final List<TicketClass> expectedLabels;

        ExceptionReportsDataSet(List<ExceptionReport> reports, List<TicketClass> expectedLabels) {
            this.reports = reports;
            this.expectedLabels = expectedLabels;
        }

        public List<ExceptionReport> getReports() {
            return reports;
        }

        public List<TicketClass> getExpectedLabels() {
            return expectedLabels;
        }
    }
}
