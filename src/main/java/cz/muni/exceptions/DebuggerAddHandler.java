package cz.muni.exceptions;

import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import cz.muni.exceptions.service.DebuggerService;
import cz.muni.exceptions.service.ExceptionDispatcherService;
import cz.muni.exceptions.source.DebuggerExceptionSource;
import cz.muni.exceptions.source.DebuggerReferenceTranslator;
import org.jboss.as.controller.*;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import java.util.List;

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

        // load debugger attributes
        boolean isEnabled = DebuggerResourceDefinition.ENABLED
                .resolveModelAttribute(context, model)
                .asBoolean();

        int port = DebuggerResourceDefinition.PORT
                .resolveModelAttribute(context, model)
                .asInt();

        // if debugger is enabled start DebuggerSource on given port
        if (isEnabled) {
            PathAddress operationAddress = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.OP_ADDR));
            ServiceName dispatcherService = ExceptionDispatcherService.createServiceName();
            DebuggerService debuggerService = new DebuggerService(new DebuggerReferenceTranslator(), port);

            ServiceController<DebuggerExceptionSource> serviceController = context.getServiceTarget()
                    .addService(DebuggerService.createServiceName(), debuggerService)
                    .addDependency(dispatcherService,
                            ExceptionDispatcher.class, debuggerService.getExceptionDispatcher())
                    .addListener(verificationHandler)
                    .setInitialMode(ServiceController.Mode.ACTIVE).install();

            if (newControllers != null) {
                newControllers.add(serviceController);
            }
        }

    }
    
    
    
    
}
