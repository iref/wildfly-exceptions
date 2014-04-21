package cz.muni.exceptions;

import java.util.Arrays;
import java.util.Collection;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 *
 * @author Jan Ferko
 */
public class DebuggerResourceDefinition extends SimpleResourceDefinition {
    
    public static final DebuggerResourceDefinition INSTANCE = new DebuggerResourceDefinition();
    
    public static final SimpleAttributeDefinition ENABLED = 
            new SimpleAttributeDefinitionBuilder(ModelElement.DEBUGGER_SOURCE_ENABLED.getName(), ModelType.BOOLEAN)
            .setAllowExpression(true)
            .setAllowNull(false)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setDefaultValue(new ModelNode(false))
            .setXmlName("enabled")
            .build();

    public static final SimpleAttributeDefinition PORT =
            new SimpleAttributeDefinitionBuilder(ModelElement.DEBUGGER_SOURCE_PORT.getName(), ModelType.INT)
            .setAllowExpression(true)
            .setAllowNull(false)
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setDefaultValue(new ModelNode(8787))
            .setXmlName("port")
            .build();

    private DebuggerResourceDefinition() {
        super(PathElement.pathElement(ModelElement.DEBUGGER_SOURCE.getName()),
                ExceptionExtension.getResourceDescriptionResolver(ModelElement.DEBUGGER_SOURCE.getName()),
                DebuggerAddHandler.INSTANCE, DebuggerRemoveHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        OperationStepHandler enabledWriteHandler = new ReloadRequiredWriteAttributeHandler(ENABLED);
        resourceRegistration.registerReadWriteAttribute(ENABLED, null, enabledWriteHandler);

        OperationStepHandler portWriteHandler = new ReloadRequiredWriteAttributeHandler(PORT);
        resourceRegistration.registerReadWriteAttribute(PORT, null, portWriteHandler);
    }

    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.<AttributeDefinition>asList(ENABLED, PORT);
    }
    
}
