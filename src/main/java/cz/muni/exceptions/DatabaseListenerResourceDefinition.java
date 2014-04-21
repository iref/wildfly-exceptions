package cz.muni.exceptions;

import org.jboss.as.controller.*;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Jan Ferko
 */
public class DatabaseListenerResourceDefinition extends SimpleResourceDefinition {

    public static final SimpleAttributeDefinition DATA_SOURCE =
            new SimpleAttributeDefinitionBuilder(ModelElement.DATABASE_LISTENER_DATA_SOURCE.getName(), ModelType.STRING)
            .setAllowExpression(true)
            .setAllowNull(false)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setXmlName(ModelElement.DATABASE_LISTENER_DATA_SOURCE.getName())
            .build();

    public static final SimpleAttributeDefinition IS_JTA =
            new SimpleAttributeDefinitionBuilder(ModelElement.DATABASE_LISTENER_JTA.getName(), ModelType.BOOLEAN)
            .setAllowExpression(true)
            .setAllowNull(false)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setDefaultValue(new ModelNode(true))
            .setXmlName(ModelElement.DATABASE_LISTENER_JTA.getName())
            .build();

    public static final DatabaseListenerResourceDefinition INSTANCE = new DatabaseListenerResourceDefinition();

    private DatabaseListenerResourceDefinition() {
        super(PathElement.pathElement(ModelElement.DATABASE_LISTENER.getName()),
                ExceptionExtension.getResourceDescriptionResolver(ModelElement.DATABASE_LISTENER.getName()),
                DatabaseListenerAddHandler.INSTANCE, DatabaseListenerRemoveHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        ReloadRequiredWriteAttributeHandler dataSourceWriteHandler = new ReloadRequiredWriteAttributeHandler(DATA_SOURCE);
        resourceRegistration.registerReadWriteAttribute(DATA_SOURCE, null, dataSourceWriteHandler);

        ReloadRequiredWriteAttributeHandler jtaWriteHandler = new ReloadRequiredWriteAttributeHandler(IS_JTA);
        resourceRegistration.registerReadWriteAttribute(IS_JTA, null, jtaWriteHandler);
    }

    public Collection<SimpleAttributeDefinition> getAttributes() {
        return Arrays.asList(DATA_SOURCE, IS_JTA);
    }
}
