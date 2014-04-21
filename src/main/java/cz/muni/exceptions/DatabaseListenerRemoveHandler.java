package cz.muni.exceptions;

import cz.muni.exceptions.service.DatabaseListenerService;
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
public class DatabaseListenerRemoveHandler extends AbstractRemoveStepHandler {

    public static final DatabaseListenerRemoveHandler INSTANCE = new DatabaseListenerRemoveHandler();

    private DatabaseListenerRemoveHandler() {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        super.performRuntime(context, operation, model);

        String suffix = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS))
                .getLastElement().getValue();
        ServiceName serviceName = DatabaseListenerService.createServiceName(suffix);
        context.removeService(serviceName);
    }
}