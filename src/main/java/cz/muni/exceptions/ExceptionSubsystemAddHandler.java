package cz.muni.exceptions;

import java.util.List;

import cz.muni.exceptions.dispatcher.ExceptionFilter;
import cz.muni.exceptions.dispatcher.ExceptionFilters;
import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;

import cz.muni.exceptions.deployment.SubsystemDeploymentProcessor;
import cz.muni.exceptions.dispatcher.AsyncExceptionDispatcher;
import cz.muni.exceptions.service.ExceptionDispatcherService;
import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import java.util.concurrent.Executors;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;

/**
 * Handler responsible for adding the subsystem resource to the model
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
class ExceptionSubsystemAddHandler extends AbstractBoottimeAddStepHandler {

    static final ExceptionSubsystemAddHandler INSTANCE = new ExceptionSubsystemAddHandler();

    private final Logger log = Logger.getLogger(ExceptionSubsystemAddHandler.class);

    private ExceptionSubsystemAddHandler() {
    }

    /** {@inheritDoc} */
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        log.info("Populating the model");
        model.setEmptyObject();
    }

    /** {@inheritDoc} */
    @Override
    public void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {

        //Add deployment processors here
        //Remove this if you don't need to hook into the deployers, or you can add as many as you like
        //see SubDeploymentProcessor for explanation of the phases
        context.addStep(new AbstractDeploymentChainStep() {
            public void execute(DeploymentProcessorTarget processorTarget) {
                processorTarget.addDeploymentProcessor(SubsystemDeploymentProcessor.PHASE, 
                        SubsystemDeploymentProcessor.PRIORITY, 
                        new SubsystemDeploymentProcessor());

            }
        }, OperationContext.Stage.RUNTIME);
        
        
        // Add exception dispatcher service
        PathAddress pathAddress = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.OP_ADDR));
        String alias = pathAddress.getLastElement().getValue();
        ServiceName serviceName = ExceptionDispatcherService.createServiceName(alias);

        //TODO create blacklist filter instead
        ExceptionFilter exceptionFilter = ExceptionFilters.ALWAYS_PASSES;
        Service<ExceptionDispatcher> dispatcherService = new ExceptionDispatcherService(
                new AsyncExceptionDispatcher(Executors.defaultThreadFactory(), exceptionFilter));
        ServiceController<ExceptionDispatcher> serviceController = context.getServiceTarget()
                .addService(serviceName, dispatcherService)
                .addListener(verificationHandler)
                .setInitialMode(ServiceController.Mode.ACTIVE).install();
        
        if (newControllers != null) {
            newControllers.add(serviceController);
        }        
    }
}
