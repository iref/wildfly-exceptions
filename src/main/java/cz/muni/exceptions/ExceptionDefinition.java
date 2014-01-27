package cz.muni.exceptions;

import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.ManagementResourceRegistration;

/**
 * @author <a href="mailto:tcerar@redhat.com">Tomaz Cerar</a>
 */
public class ExceptionDefinition extends SimpleResourceDefinition {
    public static final ExceptionDefinition INSTANCE = new ExceptionDefinition();

    private ExceptionDefinition() {
        super(ExceptionExtension.SUBSYSTEM_PATH,
                ExceptionExtension.getResourceDescriptionResolver(null),
                //We always need to add an 'add' operation
                ExceptionAdd.INSTANCE,
                //Every resource that is added, normally needs a remove operation
                ExceptionRemove.INSTANCE);
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
