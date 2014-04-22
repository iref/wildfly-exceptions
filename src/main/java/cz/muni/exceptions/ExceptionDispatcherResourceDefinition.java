package cz.muni.exceptions;

import org.jboss.as.controller.*;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Jan Ferko
 */
public class ExceptionDispatcherResourceDefinition extends SimpleResourceDefinition {

    public static final SimpleAttributeDefinition ASYNC =
            new SimpleAttributeDefinitionBuilder(ModelElement.DISPATCHER_ASYNC.getName(), ModelType.BOOLEAN)
            .setAllowExpression(true)
            .setAllowNull(true)
            .setDefaultValue(new ModelNode(true))
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setXmlName(ModelElement.DISPATCHER_ASYNC.getName())
            .build();

    private static final SimpleAttributeDefinition BLACKLIST_CLASS =
            new SimpleAttributeDefinitionBuilder(ModelElement.DISPATCHER_BLACKLIST_CLASS.getName(), ModelType.STRING)
            .addFlag(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setAllowExpression(true)
            .setAllowNull(false)
            .setXmlName(ModelElement.DISPATCHER_BLACKLIST_CLASS.getName())
            .build();

    public static final SimpleListAttributeDefinition BLACKLIST =
            new SimpleListAttributeDefinition.Builder(ModelElement.DISPATCHER_BLACKLIST.getName(), BLACKLIST_CLASS)
            .setAllowNull(false)
            .setAllowExpression(true)
            .setWrapXmlList(true)
            .setAttributeMarshaller(new AttributeMarshaller() {
                @Override
                public void marshallAsElement(AttributeDefinition attribute, ModelNode resourceModel, boolean marshallDefault, XMLStreamWriter writer) throws XMLStreamException {
                    resourceModel = resourceModel.get(attribute.getName());
                    if (resourceModel.isDefined()) {
                        writer.writeStartElement(attribute.getName());
                        for (ModelNode item : resourceModel.asList()) {
                            writer.writeStartElement(BLACKLIST_CLASS.getXmlName());
                            writer.writeCData(item.asString());
                            writer.writeEndElement();
                        }
                        writer.writeEndElement();
                    }
                }
            })
            .build();

    public static final ExceptionDispatcherResourceDefinition INSTANCE = new ExceptionDispatcherResourceDefinition();

    private ExceptionDispatcherResourceDefinition() {
        super(PathElement.pathElement(ModelElement.DISPATCHER.getName()),
                ExceptionExtension.getResourceDescriptionResolver(ModelElement.DISPATCHER.getName()),
                ExceptionDispatcherAddHandler.INSTANCE, ExceptionDispatcherRemoveHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        OperationStepHandler enabledWriteHandler = new ReloadRequiredWriteAttributeHandler(ASYNC);
        resourceRegistration.registerReadWriteAttribute(ASYNC, null, enabledWriteHandler);

        OperationStepHandler portWriteHandler = new ReloadRequiredWriteAttributeHandler(BLACKLIST);
        resourceRegistration.registerReadWriteAttribute(BLACKLIST, null, portWriteHandler);
    }

    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.<AttributeDefinition>asList(ASYNC, BLACKLIST);
    }
}
