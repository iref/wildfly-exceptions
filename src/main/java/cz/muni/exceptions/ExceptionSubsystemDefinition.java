package cz.muni.exceptions;

import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * @author <a href="mailto:tcerar@redhat.com">Tomaz Cerar</a>
 */
public class ExceptionSubsystemDefinition extends SimpleResourceDefinition {
    public static final ExceptionSubsystemDefinition INSTANCE = new ExceptionSubsystemDefinition();

    private ExceptionSubsystemDefinition() {
        super(ExceptionExtension.SUBSYSTEM_PATH,
                ExceptionExtension.getResourceDescriptionResolver(null),
                //We always need to add an 'add' operation
                ExceptionSubsystemAddHandler.INSTANCE,
                //Every resource that is added, normally needs a delete operation
                ExceptionSubsystemRemoveHandler.INSTANCE);
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        //you can register aditional operations here
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        //you can register attributes here
    }
}
