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
 * @author johnny
 */
public class LoggingResourceDefinition extends SimpleResourceDefinition {
    
    public static final LoggingResourceDefinition INSTANCE = new LoggingResourceDefinition();
    
    public static final SimpleAttributeDefinition ENABLED = 
            new SimpleAttributeDefinitionBuilder("enabled", ModelType.BOOLEAN)
            .setAllowNull(false)
            .setDefaultValue(new ModelNode(true))
            .setAllowExpression(true)
            .setXmlName("enabled")
            .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .build();

    private LoggingResourceDefinition() {
        super(PathElement.pathElement("logging-source"), 
                ExceptionExtension.getResourceDescriptionResolver("logging-source"), 
                LoggingAddHandler.INSTANCE, LoggingRemoveHandler.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        OperationStepHandler writeHandler = new ReloadRequiredWriteAttributeHandler(ENABLED);
        resourceRegistration.registerReadWriteAttribute(ENABLED, null, writeHandler);
    }
    
    public Collection<AttributeDefinition> getAttributes() {
        return Arrays.<AttributeDefinition>asList(ENABLED);
    }
            
}
