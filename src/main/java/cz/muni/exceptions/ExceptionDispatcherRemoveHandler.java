package cz.muni.exceptions;

import cz.muni.exceptions.service.ExceptionDispatcherService;
import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

/**
 * @author Jan Ferko
 */
public class ExceptionDispatcherRemoveHandler extends AbstractRemoveStepHandler {

    public static final ExceptionDispatcherRemoveHandler INSTANCE = new ExceptionDispatcherRemoveHandler();

    private ExceptionDispatcherRemoveHandler() {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        super.performRuntime(context, operation, model);

        String suffix = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS))
                .getLastElement().getValue();
        ServiceName serviceName = ExceptionDispatcherService.createServiceName();
        context.removeService(serviceName);
    }
}
