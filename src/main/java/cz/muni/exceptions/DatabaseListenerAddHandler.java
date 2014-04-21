package cz.muni.exceptions;

import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import cz.muni.exceptions.listener.DatabaseExceptionListener;
import cz.muni.exceptions.service.DatabaseListenerService;
import cz.muni.exceptions.service.ExceptionDispatcherService;
import org.hibernate.metamodel.relational.Database;
import org.jboss.as.controller.*;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.txn.service.TxnServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import java.util.List;

/**
 * @author Jan Ferko
 */
public class DatabaseListenerAddHandler extends AbstractAddStepHandler {

    public static final DatabaseListenerAddHandler INSTANCE = new DatabaseListenerAddHandler();

    private DatabaseListenerAddHandler() {
    }

    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        super.populateModel(operation, model);
        for (SimpleAttributeDefinition attribute : DatabaseListenerResourceDefinition.INSTANCE.getAttributes()) {
            attribute.validateAndSet(operation, model);
        }
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        super.performRuntime(context, operation, model, verificationHandler, newControllers);

        String dataSource = DatabaseListenerResourceDefinition.DATA_SOURCE
                .resolveModelAttribute(context, model)
                .asString();

        boolean isJta = DatabaseListenerResourceDefinition.IS_JTA
                .resolveModelAttribute(context, model)
                .asBoolean();

        DatabaseListenerService databaseListenerService = new DatabaseListenerService(dataSource);

        String dispatcherAlias = operation.get(ModelDescriptionConstants.ADDRESS).asPropertyList().get(0)
                .getValue().asString();
        ServiceName dispatcherServiceName = ExceptionDispatcherService.createServiceName(dispatcherAlias);

        PathAddress operationAddress = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.OP_ADDR));
        String serviceAlias = operationAddress.getLastElement().getValue();
        ServiceName databaseListenerServiceName = DatabaseListenerService.createServiceName(serviceAlias);

        ServiceBuilder<DatabaseExceptionListener> serviceBuilder = context.getServiceTarget()
                .addService(databaseListenerServiceName, databaseListenerService)
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .addDependency(ServiceBuilder.DependencyType.REQUIRED, dispatcherServiceName)
                .addListener(verificationHandler);

        if (isJta) {
            ServiceName transactionManagerServiceName = TxnServices.JBOSS_TXN_TRANSACTION_MANAGER;
            serviceBuilder.addDependency(ServiceBuilder.DependencyType.REQUIRED, transactionManagerServiceName);
        }

        if (newControllers != null) {
            newControllers.add(serviceBuilder.install());
        }
    }
}
