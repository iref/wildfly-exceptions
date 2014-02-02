package cz.muni.exceptions;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author johnny
 */
public class DebuggerRemoveHandler extends AbstractRemoveStepHandler {
    
    public static final DebuggerRemoveHandler INSTANCE = new DebuggerRemoveHandler();
    
    private DebuggerRemoveHandler() {        
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        super.performRuntime(context, operation, model);
        
        //TODO stop debugger service
    }        
    
}
