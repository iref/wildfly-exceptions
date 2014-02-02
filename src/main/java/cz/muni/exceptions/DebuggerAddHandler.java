package cz.muni.exceptions;

import java.util.List;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

/**
 *
 * @author johnny
 */
public class DebuggerAddHandler extends AbstractAddStepHandler {
    
    public static final DebuggerAddHandler INSTANCE = new DebuggerAddHandler();
    
    private DebuggerAddHandler() {        
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        super.populateModel(operation, model);
        for (AttributeDefinition attribute : DebuggerResourceDefinition.INSTANCE.getAttributes()) {
            attribute.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, 
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        super.performRuntime(context, operation, model, verificationHandler, newControllers);
        
        boolean isEnabled = DebuggerResourceDefinition.ENABLED
                .resolveModelAttribute(context, model)
                .asBoolean();
        
        //TODO get dispatcher from dispatcher service
        
        //TODO create debugger service
        
        //TODO store debugger service into newControllers
    }
    
    
    
    
}
