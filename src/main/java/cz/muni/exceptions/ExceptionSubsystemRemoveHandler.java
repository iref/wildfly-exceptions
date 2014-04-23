package cz.muni.exceptions;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
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
    }


}
