package cz.muni.exceptions;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Parser and writer for exception subsystem management model.
 *
 * @author Jan Ferko
 */
public class ExceptionSubsystemParser implements XMLStreamConstants,
        XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

    private static ModelNode createAddSubsystemOperation() {
        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).set(PathAddress.pathAddress(ExceptionExtension.SUBSYSTEM_PATH).toModelNode());
        return subsystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(ExceptionExtension.NAMESPACE, false);

        ModelNode node = context.getModelNode();

        // marshall dispatcher model
        if (node.hasDefined(ModelElement.DISPATCHER.getName())) {
            ModelNode dispatchers = node.get(ModelElement.DISPATCHER.getName());

            for (Property property : dispatchers.asPropertyList()) {
                final String name = property.getName();
                final ModelNode dispatcher = property.getValue();
                if (dispatcher.isDefined()) {
                    writeDispatcher(writer, name, dispatcher);
                }
            }
        }

        writer.writeStartElement("sources");

        // marshall logging source model
        if (node.hasDefined(ModelElement.LOGGING_SOURCE.getName())) {
            ModelNode loggingSources = node.get(ModelElement.LOGGING_SOURCE.getName());

            for (Property property : loggingSources.asPropertyList()) {
                final String name = property.getName();
                final ModelNode loggingSource = property.getValue();
                if (loggingSource.isDefined()) {
                    writeLoggingSource(writer, name, loggingSource);
                }
            }
        }

        // marshall debugger source model
        if (node.hasDefined(ModelElement.DEBUGGER_SOURCE.getName())) {
            ModelNode debuggerSources = node.get(ModelElement.DEBUGGER_SOURCE.getName());

            for (Property property : debuggerSources.asPropertyList()) {
                final String name = property.getName();
                final ModelNode debuggerSource = property.getValue();
                if (debuggerSource.isDefined()) {
                    writeDebuggerSource(writer, name, debuggerSource);
                }
            }
        }

        // end of sources
        writer.writeEndElement();

        writer.writeStartElement("listeners");

        if (node.hasDefined(ModelElement.DATABASE_LISTENER.getName())) {
            ModelNode databaseListeners = node.get(ModelElement.DATABASE_LISTENER.getName());

            for (Property property : databaseListeners.asPropertyList()) {
                final String name = property.getName();
                final ModelNode databaseListener = property.getValue();
                if (databaseListener.isDefined()) {
                    writeDatabaseListener(writer, name, databaseListener);
                }
            }
        }

        writer.writeEndElement();
        // end of subsystem
        writer.writeEndElement();
    }

    private void writeDispatcher(XMLExtendedStreamWriter writer, String name, ModelNode dispatcher) throws XMLStreamException {
            writer.writeStartElement(ModelElement.DISPATCHER.getName());

            ExceptionDispatcherResourceDefinition.ASYNC.marshallAsAttribute(dispatcher, true, writer);
            ExceptionDispatcherResourceDefinition.BLACKLIST.marshallAsElement(dispatcher, false, writer);
            writer.writeEndElement();
    }

    private void writeLoggingSource(XMLExtendedStreamWriter writer, String name, ModelNode source) throws XMLStreamException {
            writer.writeStartElement(name);
            LoggingResourceDefinition.ENABLED.marshallAsAttribute(source, true, writer);
            writer.writeEndElement();
    }

    private void writeDebuggerSource(XMLExtendedStreamWriter writer, String name, ModelNode source) throws XMLStreamException {
            writer.writeStartElement(name);
            DebuggerResourceDefinition.ENABLED.marshallAsAttribute(source, true, writer);
            DebuggerResourceDefinition.PORT.marshallAsAttribute(source, true, writer);
            writer.writeEndElement();
    }

    private void writeDatabaseListener(XMLExtendedStreamWriter writer, String name, ModelNode listener) throws XMLStreamException {
            writer.writeStartElement(name);
            DatabaseListenerResourceDefinition.DATA_SOURCE.marshallAsAttribute(listener, true, writer);
            DatabaseListenerResourceDefinition.IS_JTA.marshallAsAttribute(listener, true, writer);
            writer.writeEndElement();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        // Require no attributes
        ParseUtils.requireNoAttributes(reader);
        final ModelNode root = createAddSubsystemOperation();

        list.add(root);

        while(reader.hasNext() && reader.nextTag() != END_DOCUMENT) {
            String localName = reader.getLocalName();
            if (!reader.isStartElement()) {
                continue;
            }

            if (reader.getLocalName().equals(ModelElement.DISPATCHER.getName())) {
                readDispatcher(reader, list, root);
            }
            if (reader.getLocalName().equals("sources")) {
                readSources(reader, list, root);
            }
            if (reader.getLocalName().equals("listeners")) {
                readListeners(reader, list, root);
            }
        }
    }

    private void readDispatcher(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode root) throws XMLStreamException {
        ModelNode addDispatcherOperation = new ModelNode();
        addDispatcherOperation.get(OP).set(ModelDescriptionConstants.ADD);

        String elementName = reader.getLocalName();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attributeName = reader.getAttributeLocalName(i);
            String attributeValue = reader.getAttributeValue(i);

            if (ModelElement.DISPATCHER_ASYNC.getName().equals(attributeName)) {
                ExceptionDispatcherResourceDefinition.ASYNC
                    .parseAndSetParameter(attributeValue, addDispatcherOperation, reader);
            } else {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }

        if (reader.next() != END_ELEMENT) {
            String nestedElementName = reader.getLocalName();
            if (ModelElement.DISPATCHER_BLACKLIST.getName().equals(nestedElementName)) {
                parseBlacklist(reader, addDispatcherOperation);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
        }


        PathAddress address = PathAddress.pathAddress(ExceptionExtension.SUBSYSTEM_PATH,
            PathElement.pathElement(elementName, elementName));
        addDispatcherOperation.get(ModelDescriptionConstants.OP_ADDR).set(address.toModelNode());
        list.add(addDispatcherOperation);
    }

    private void parseBlacklist(XMLExtendedStreamReader reader, ModelNode addDispatcherOperation) throws XMLStreamException {
        while (reader.hasNext() && reader.next() != END_ELEMENT) {
            String elementName = reader.getLocalName();
            if (ModelElement.DISPATCHER_BLACKLIST_CLASS.getName().equals(elementName)) {
                String content = reader.getElementText();
                addDispatcherOperation.get(ExceptionDispatcherResourceDefinition.BLACKLIST.getName())
                    .add(new ModelNode(content));
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }

            if (!reader.hasNext() || reader.next() != END_ELEMENT) {
                throw ParseUtils.unexpectedElement(reader);
            }
        }
    }

    private void readSources(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode root) throws XMLStreamException {
        while(reader.hasNext() && reader.nextTag() != END_ELEMENT) {

            String modelId = reader.getLocalName();
            ModelElement modelElement = ModelElement.forName(modelId);

            switch(modelElement) {
                case DEBUGGER_SOURCE: createDebuggerAddOperation(reader, list); break;
                case LOGGING_SOURCE: createLoggingAddOperation(reader, list); break;
                default:
                    throw ParseUtils.unexpectedElement(reader);
            }
        }
    }

    private void createLoggingAddOperation(XMLExtendedStreamReader reader, List<ModelNode> list)
            throws XMLStreamException {

        ModelNode addLoggingOperation = new ModelNode();
        addLoggingOperation.get(OP).set(ModelDescriptionConstants.ADD);

        String elementName = reader.getLocalName();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attributeName = reader.getAttributeLocalName(i);
            String attributeValue = reader.getAttributeValue(i);
            if (ModelElement.LOGGING_SOURCE_ENABLED.getName().equals(attributeName)) {
                LoggingResourceDefinition.ENABLED.parseAndSetParameter(//
                    attributeValue, addLoggingOperation, reader);
            } else {
                throw ParseUtils.unexpectedElement(reader);
            }
        }
        ParseUtils.requireNoContent(reader);

        PathAddress address = PathAddress.pathAddress(ExceptionExtension.SUBSYSTEM_PATH,
        PathElement.pathElement(elementName, elementName));
        addLoggingOperation.get(OP_ADDR).set(address.toModelNode());
        list.add(addLoggingOperation);
    }

    private void createDebuggerAddOperation(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        ModelNode addDebuggerOperation = new ModelNode();
        addDebuggerOperation.get(OP).set(ModelDescriptionConstants.ADD);

        String elementName = reader.getLocalName();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attributeName = reader.getAttributeLocalName(i);
            String attributeValue = reader.getAttributeValue(i);
            if (ModelElement.DEBUGGER_SOURCE_ENABLED.getName().equals(attributeName)) {
                DebuggerResourceDefinition.ENABLED.parseAndSetParameter(//
                    attributeValue, addDebuggerOperation, reader);
            } else if (ModelElement.DEBUGGER_SOURCE_PORT.getName().equals(attributeName)) {
                DebuggerResourceDefinition.PORT.parseAndSetParameter(//
                    attributeValue, addDebuggerOperation, reader);
            } else {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }
        ParseUtils.requireNoContent(reader);

        PathAddress address = PathAddress.pathAddress(ExceptionExtension.SUBSYSTEM_PATH,
             PathElement.pathElement(elementName, elementName));
        addDebuggerOperation.get(OP_ADDR).set(address.toModelNode());
        list.add(addDebuggerOperation);
    }

    private void readListeners(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode root) throws XMLStreamException {
        while(reader.hasNext() && reader.nextTag() != END_ELEMENT) {

            String modelId = reader.getLocalName();
            ModelElement modelElement = ModelElement.forName(modelId);

            switch(modelElement) {
                case DATABASE_LISTENER: createDatabaseListenerAddOperation(reader, list); break;
                default: throw ParseUtils.unexpectedElement(reader);
            }
        }
    }

    private void createDatabaseListenerAddOperation(XMLExtendedStreamReader reader, List<ModelNode> list)
        throws XMLStreamException{

        ModelNode addDatabaseListenerOperation = new ModelNode();
        addDatabaseListenerOperation.get(OP).set(ModelDescriptionConstants.ADD);

        String elementName = reader.getLocalName();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String attributeName = reader.getAttributeLocalName(i);
            String attributeValue = reader.getAttributeValue(i);

            if (ModelElement.DATABASE_LISTENER_DATA_SOURCE.getName().equals(attributeName)) {
                DatabaseListenerResourceDefinition.DATA_SOURCE.parseAndSetParameter(attributeValue,
                    addDatabaseListenerOperation, reader);
            } else if (ModelElement.DATABASE_LISTENER_JTA.getName().equals(attributeName)) {
                DatabaseListenerResourceDefinition.IS_JTA.parseAndSetParameter(attributeValue,
                    addDatabaseListenerOperation, reader);
            } else {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }

        ParseUtils.requireNoContent(reader);

        PathAddress address = PathAddress.pathAddress(ExceptionExtension.SUBSYSTEM_PATH,
            PathElement.pathElement(elementName, elementName));
        addDatabaseListenerOperation.get(OP_ADDR).set(address.toModelNode());
        list.add(addDatabaseListenerOperation);
    }
}