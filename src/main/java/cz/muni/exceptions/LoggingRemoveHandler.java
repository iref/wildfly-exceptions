package cz.muni.exceptions;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author johnny
 */
public class LoggingRemoveHandler extends AbstractRemoveStepHandler {
    
    public static final LoggingRemoveHandler INSTANCE = new LoggingRemoveHandler();
    
    private LoggingRemoveHandler() {        
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        super.performRuntime(context, operation, model);
        
        // remove registered handler
    }
    
    
}
