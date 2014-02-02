package cz.muni.exceptions;

import java.util.List;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

/**
 *
 * @author johnny
 */
public class LoggingAddHandler extends AbstractAddStepHandler {
    
    public static final LoggingAddHandler INSTANCE = new LoggingAddHandler();
    
    private LoggingAddHandler() {        
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        super.populateModel(operation, model);
        for (AttributeDefinition attribute : LoggingResourceDefinition.INSTANCE.getAttributes()) {
            attribute.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        super.performRuntime(context, operation, model, verificationHandler, newControllers);
        
        boolean isEnabled = LoggingResourceDefinition.ENABLED.resolveModelAttribute(context, model)
                .asBoolean();
        
        // if service is enabled add logging source
    }
    
    
    
}
