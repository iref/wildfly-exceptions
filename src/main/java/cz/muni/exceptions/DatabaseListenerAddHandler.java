package cz.muni.exceptions;

import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import cz.muni.exceptions.listener.DatabaseExceptionListener;
import cz.muni.exceptions.service.DatabaseListenerService;
import cz.muni.exceptions.service.ExceptionDispatcherService;
import org.jboss.as.controller.*;
import org.jboss.as.txn.service.TxnServices;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import javax.transaction.TransactionManager;
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
        ServiceName dispatcherServiceName = ExceptionDispatcherService.createServiceName();

        ServiceName databaseListenerServiceName = DatabaseListenerService.createServiceName();

        ServiceBuilder<DatabaseExceptionListener> serviceBuilder = context.getServiceTarget()
                .addService(databaseListenerServiceName, databaseListenerService)
                .setInitialMode(ServiceController.Mode.ACTIVE)
                .addDependency(dispatcherServiceName, ExceptionDispatcher.class, databaseListenerService.getDispatcher())
                .addListener(verificationHandler);

        if (isJta) {
            ServiceName transactionManagerService = TxnServices.JBOSS_TXN_TRANSACTION_MANAGER;
            serviceBuilder.addDependency(transactionManagerService, TransactionManager.class,
                    databaseListenerService.getTransactionManager());
        }

        if (newControllers != null) {
            newControllers.add(serviceBuilder.install());
        }
    }
}
