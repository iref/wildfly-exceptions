package cz.muni.exceptions;

import cz.muni.exceptions.dispatcher.*;
import cz.muni.exceptions.service.ExceptionDispatcherService;
import org.jboss.as.controller.*;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author Jan Ferko
 */
public class ExceptionDispatcherAddHandler extends AbstractAddStepHandler {

    public static final ExceptionDispatcherAddHandler INSTANCE = new ExceptionDispatcherAddHandler();

    private ExceptionDispatcherAddHandler() {
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        super.populateModel(operation, model);
        for (AttributeDefinition attribute : ExceptionDispatcherResourceDefinition.INSTANCE.getAttributes()) {
            attribute.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
                                  ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        super.performRuntime(context, operation, model, verificationHandler, newControllers);

        boolean isAsync = ExceptionDispatcherResourceDefinition.ASYNC
                .resolveModelAttribute(context, model)
                .asBoolean();

        List<String> blacklistPatterns = getBlacklistPatterns(context, model);
        ExceptionFilter blacklistFilter = new BlacklistFilter(blacklistPatterns);

        final ExceptionDispatcher dispatcher;
        if (isAsync) {
            dispatcher = new AsyncExceptionDispatcher(Executors.defaultThreadFactory(), blacklistFilter);
        } else {
            dispatcher = new BasicExceptionDispatcher(blacklistFilter);
        }

        // Add exception dispatcher service
        PathAddress pathAddress = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.OP_ADDR));
        String alias = pathAddress.getLastElement().getValue();
        ServiceName serviceName = ExceptionDispatcherService.createServiceName();

        Service<ExceptionDispatcher> dispatcherService = new ExceptionDispatcherService(dispatcher);
        ServiceController<ExceptionDispatcher> serviceController = context.getServiceTarget()
                .addService(serviceName, dispatcherService)
                .addListener(verificationHandler)
                .setInitialMode(ServiceController.Mode.ACTIVE).install();

        if (newControllers != null) {
            newControllers.add(serviceController);
        }
    }

    private List<String> getBlacklistPatterns(OperationContext context, ModelNode model) throws OperationFailedException{
        ModelNode blacklistItems = ExceptionDispatcherResourceDefinition.BLACKLIST
                .resolveModelAttribute(context, model);

        List<String> blacklistPatterns = new ArrayList<>();
        if (!blacklistItems.isDefined()) {
            return blacklistPatterns;
        }


        for (ModelNode item : blacklistItems.asList()) {
            if (item.isDefined()) {
                blacklistPatterns.add(item.asString());
            }
        }

        return blacklistPatterns;
    }
}
