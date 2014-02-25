package cz.muni.exceptions;

import cz.muni.exceptions.service.ExceptionDispatcherService;
import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * Handler responsible for removing the subsystem resource from the model
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
class ExceptionSubsystemRemoveHandler extends AbstractRemoveStepHandler {

    static final ExceptionSubsystemRemoveHandler INSTANCE = new ExceptionSubsystemRemoveHandler();

    private final Logger log = Logger.getLogger(ExceptionSubsystemRemoveHandler.class);

    private ExceptionSubsystemRemoveHandler() {
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        //Remove any services installed by the corresponding add handler here
        PathAddress pathAddress = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.OP_ADDR));
        String alias = pathAddress.getLastElement().getValue();
        context.removeService(ExceptionDispatcherService.createServiceName(alias));
        context.restartRequired();
    }


}
