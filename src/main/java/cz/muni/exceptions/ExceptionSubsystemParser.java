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
import java.util.Collections;
import java.util.EnumSet;
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

        EnumSet<ModelElement> required = EnumSet.of(ModelElement.SOURCES, ModelElement.DISPATCHER, ModelElement.LISTENERS);
        EnumSet<ModelElement> encountered = EnumSet.noneOf(ModelElement.class);

        while(reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            ModelElement element = ModelElement.forName(reader.getLocalName());

            required.remove(element);
            if (!encountered.add(element)) {
                throw ParseUtils.duplicateNamedElement(reader, element.getName());
            }

            switch(element) {
                case DISPATCHER: readDispatcher(reader, list, root); break;
                case SOURCES: readSources(reader, list, root); break;
                case LISTENERS: readListeners(reader, list, root); break;
                default: throw ParseUtils.unexpectedElement(reader);
            }
        }

        if (!required.isEmpty()) {
            throw ParseUtils.missingRequired(reader, required);
        }
    }

    private void readDispatcher(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode root) throws XMLStreamException {
        ModelNode addDispatcherOperation = new ModelNode();
        addDispatcherOperation.get(OP).set(ADD);

        // add ADD operation address
        PathAddress address = PathAddress.pathAddress(ExceptionExtension.SUBSYSTEM_PATH,
                PathElement.pathElement(reader.getLocalName(), reader.getLocalName()));
        addDispatcherOperation.get(OP_ADDR).set(address.toModelNode());

        // parse attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            ModelElement attribute = ModelElement.forName(reader.getAttributeLocalName(i));

            switch (attribute) {
                case DISPATCHER_ASYNC: {
                    ExceptionDispatcherResourceDefinition.ASYNC
                            .parseAndSetParameter(reader.getAttributeValue(i), addDispatcherOperation, reader);
                    break;
                }

                default: throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }

        // parse elements
        EnumSet<ModelElement> encountered = EnumSet.noneOf(ModelElement.class);
        while (reader.nextTag() != END_ELEMENT) {
            ModelElement element = ModelElement.forName(reader.getLocalName());
            if (!encountered.add(element)) {
                throw ParseUtils.duplicateNamedElement(reader, element.getName());
            }

            switch (element) {
                case DISPATCHER_BLACKLIST: readClasses(reader, addDispatcherOperation); break;
                default: throw ParseUtils.unexpectedElement(reader);
            }
        }

        list.add(addDispatcherOperation);
    }

    private void readBlacklist(XMLExtendedStreamReader reader, ModelNode addDispatcherOperation) throws XMLStreamException {
        while (reader.nextTag() != END_ELEMENT) {
            ModelElement element = ModelElement.forName(reader.getLocalName());

            switch (element) {
                case DISPATCHER_BLACKLIST: readClasses(reader, addDispatcherOperation); break;
                default: throw ParseUtils.unexpectedElement(reader);
            }
        }
    }

    private void readClasses(XMLExtendedStreamReader reader, ModelNode addDispatcherOperation) throws XMLStreamException {
        while (reader.nextTag() != END_ELEMENT) {
            ModelElement element = ModelElement.forName(reader.getLocalName());
            String content = null;
            switch(element) {
                case DISPATCHER_BLACKLIST_CLASS: {
                    content = reader.getElementText();
                    break;
                }
                default: throw ParseUtils.unexpectedElement(reader);
            }

            if (content == null) {
                throw ParseUtils.missingRequired(reader, Collections.singleton("text"));
            }

            addDispatcherOperation.get(ExceptionDispatcherResourceDefinition.BLACKLIST.getName())
                    .add(new ModelNode(content));
        }
    }

    private void readSources(XMLExtendedStreamReader reader, List<ModelNode> list, ModelNode root) throws XMLStreamException {
        while(reader.hasNext() && reader.nextTag() != END_ELEMENT) {

            String modelId = reader.getLocalName();
            ModelElement modelElement = ModelElement.forName(modelId);

            switch(modelElement) {
                case DEBUGGER_SOURCE: createDebuggerAddOperation(reader, list); break;
                default: throw ParseUtils.unexpectedElement(reader);
            }
        }
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

        EnumSet<ModelElement> required = EnumSet.of(ModelElement.DATABASE_LISTENER_DATA_SOURCE);

        String elementName = reader.getLocalName();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            ModelElement attribute = ModelElement.forName(reader.getAttributeLocalName(i));
            required.remove(attribute);

            String attributeValue = reader.getAttributeValue(i);

            if (ModelElement.DATABASE_LISTENER_DATA_SOURCE.equals(attribute)) {
                DatabaseListenerResourceDefinition.DATA_SOURCE.parseAndSetParameter(attributeValue,
                    addDatabaseListenerOperation, reader);
            } else if (ModelElement.DATABASE_LISTENER_JTA.equals(attribute)) {
                DatabaseListenerResourceDefinition.IS_JTA.parseAndSetParameter(attributeValue,
                    addDatabaseListenerOperation, reader);
            } else {
                throw ParseUtils.unexpectedAttribute(reader, i);
            }
        }

        ParseUtils.requireNoContent(reader);

        if (!required.isEmpty()) {
            throw ParseUtils.missingRequired(reader, required);
        }

        PathAddress address = PathAddress.pathAddress(ExceptionExtension.SUBSYSTEM_PATH,
            PathElement.pathElement(elementName, elementName));
        addDatabaseListenerOperation.get(OP_ADDR).set(address.toModelNode());
        list.add(addDatabaseListenerOperation);
    }
}
