package cz.muni.exceptions.listener.classifier;

import cz.muni.exceptions.listener.db.model.TicketClass;

import javax.xml.stream.*;
import java.io.InputStream;
import java.util.EnumSet;

/**
 * Implementation of {@link cz.muni.exceptions.listener.classifier.PackageDataParser}, that uses StAX XML parser.
 *
 * @author Jan Ferko
 */
public final class StaxPackageDataParser implements PackageDataParser {

    private XMLInputFactory readerFactory;

    public StaxPackageDataParser() {
        this.readerFactory = XMLInputFactory.newInstance();
    }

    @Override
    public Node parseInput(InputStream dataStream) {
        if (dataStream == null) {
            throw new IllegalArgumentException("[DataStream] is required and should not be null");
        }
        Node result = null;

        XMLStreamReader reader = null;
        try {
            reader = readerFactory.createXMLStreamReader(dataStream);
            // move from start element to first element
            reader.nextTag();

            Element element = Element.forName(reader.getLocalName());
            // check for root element
            switch (element) {
                case PACKAGES: {
                    result = parsePackages(reader);
                    break;
                }

                default: {
                    String location = buildLocationString(reader);
                    throw new IllegalStateException("Reader hit unexpected element [" + reader.getLocalName() + "] " + location);
                }
            }


        } catch (XMLStreamException ex) {
            throw new RuntimeException("Error while opening or reading from [dataStream]", ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ex) {
                    throw new RuntimeException("Error while closing reader", ex);
                }

            }
        }

        return result;
    }

    private Node parsePackages(XMLStreamReader reader) throws XMLStreamException {
        PackageTreeBuilder builder = new PackageTreeBuilder();

        // parse every package element
        while (reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
            Element element = Element.forName(reader.getLocalName());
            if (!Element.PACKAGE.equals(element)) {
                String location = buildLocationString(reader);
                throw new IllegalStateException("Invalid element [" + reader.getLocalName() + "] at " + location);
            }

            parsePackage(reader, builder);

            if (reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
                throw new IllegalStateException("Missing end tag of [Package] element at " + buildLocationString(reader));
            }
        }

        return builder.build();
    }

    private void parsePackage(XMLStreamReader reader, PackageTreeBuilder builder) throws XMLStreamException {
        EnumSet<Element> required = EnumSet.of(Element.PACKAGE_LABEL, Element.PACKAGE_NAME, Element.PACKAGE_WEIGHT);

        String packageName = "";
        TicketClass label = TicketClass.UNKNOWN;
        double weight = 0.0;

        for (int i = 0; i < reader.getAttributeCount(); i++) {
            Element attribute = Element.forName(reader.getAttributeLocalName(i));
            if (!required.remove(attribute)) {
                String location = buildLocationString(reader);
                throw new IllegalStateException("Invalid attribute [" + reader.getAttributeLocalName(i) + "] at " + location);
            }

            String attributeValue = reader.getAttributeValue(i);
            switch (attribute) {
                case PACKAGE_NAME: {
                    packageName = attributeValue;
                    break;
                }

                case PACKAGE_LABEL: {
                    label = TicketClass.forKey(attributeValue.toLowerCase());
                    break;
                }

                case PACKAGE_WEIGHT: {
                    weight = Double.parseDouble(attributeValue);
                }

                default: {
                    break;
                }
            }
        }

        if (!required.isEmpty()) {
            String location = buildLocationString(reader);
            throw new IllegalStateException("Missing attributes[" + required + "] of [Package] element at " + location);
        }

        builder.addPackage(packageName, label, weight);
    }

    private String buildLocationString(XMLStreamReader reader) {
        Location location = reader.getLocation();
        return String.format("[%1$d, %2$d]", location.getLineNumber(), location.getColumnNumber());
    }

    private enum Element {
        PACKAGES("packages"),
        PACKAGE("package"),
        PACKAGE_NAME("name"),
        PACKAGE_LABEL("label"),
        PACKAGE_WEIGHT("weight");

        private final String key;

        private Element(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }

        public static Element forName(String key) {
            for (Element element : Element.values()) {
                if (element.getKey().equalsIgnoreCase(key)) {
                    return element;
                }
            }
            return null;
        }
    }
}
