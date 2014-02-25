package cz.muni.exceptions;


import cz.muni.exceptions.ExceptionExtension;
import cz.muni.exceptions.service.ExceptionDispatcherService;
import junit.framework.Assert;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.junit.Test;

import java.util.List;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceNotFoundException;


/**
 * Tests all management expects for subsystem, parsing, marshaling, model definition and other
 * Here is an example that allows you a fine grained controler over what is tested and how. So it can give you ideas what can be done and tested.
 * If you have no need for advanced testing of subsystem you look at {@link SubsystemBaseParsingTestCase} that testes same stuff but most of the code
 * is hidden inside of test harness
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SubsystemParsingTestCase extends AbstractSubsystemTest {

    public SubsystemParsingTestCase() {
        super(ExceptionExtension.SUBSYSTEM_NAME, new ExceptionExtension());
    }

    /**
     * Tests that the xml is parsed into the correct operations
     */
    @Test
    public void testParseSubsystem() throws Exception {
        //Parse the subsystem xml into operations
        String subsystemXml = getSubsystemXml();
        List<ModelNode> operations = super.parse(subsystemXml);

        ///Check that we have the expected number of operations
        Assert.assertEquals(3, operations.size());

        //Check that each operation has the correct content
        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(ExceptionExtension.SUBSYSTEM_NAME, element.getValue());
        
        // Check that logging-source was set
        ModelNode addLoggingSource = operations.get(1);
        Assert.assertEquals(ADD, addLoggingSource.get(OP).asString());
        PathAddress loggingSourceAddress = PathAddress.pathAddress(addLoggingSource.get(OP_ADDR));
        Assert.assertEquals(2, loggingSourceAddress.size());
        element = loggingSourceAddress.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(ExceptionExtension.SUBSYSTEM_NAME, element.getValue());
        
        PathElement loggingSourceElement = loggingSourceAddress.getElement(1);
        Assert.assertEquals("logging-source", loggingSourceElement.getKey());
        Assert.assertEquals("logging-source", loggingSourceElement.getValue());
        Assert.assertTrue(addLoggingSource.get("enabled").asBoolean());
        
        // check that debugger-source was set
        ModelNode addDebuggerSource = operations.get(2);
        Assert.assertEquals(ADD, addDebuggerSource.get(OP).asString());
        PathAddress debuggerSourceAddress = PathAddress.pathAddress(addDebuggerSource.get(OP_ADDR));
        Assert.assertEquals(2, debuggerSourceAddress.size());
        element = debuggerSourceAddress.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(ExceptionExtension.SUBSYSTEM_NAME, element.getValue());
        
        PathElement debuggerSourceElement = debuggerSourceAddress.getElement(1);
        Assert.assertEquals("debugger-source", debuggerSourceElement.getKey());
        Assert.assertEquals("debugger-source", debuggerSourceElement.getValue());
        Assert.assertFalse(addDebuggerSource.get("enabled").asBoolean());
    }

    /**
     * Test that the model created from the xml looks as expected
     */
    @Test
    public void testInstallIntoController() throws Exception {
        //Parse the subsystem xml and install into the controller
        //Parse the subsystem xml into operations
        String subsystemXml = getSubsystemXml();
        KernelServices services = super.installInController(subsystemXml);
        
        //Read the whole model and make sure it looks as expected
        ModelNode model = services.readWholeModel();
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(ExceptionExtension.SUBSYSTEM_NAME));
        
        Assert.assertTrue(model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME).hasDefined("logging-source"));                
        final ModelNode loggingSource = model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME, "logging-source", "logging-source");
        Assert.assertTrue(loggingSource.hasDefined("enabled"));
        Assert.assertTrue(model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME, "logging-source", "logging-source", "enabled").asBoolean());
        
        Assert.assertTrue(model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME).hasDefined("debugger-source"));        
        final ModelNode debuggerSource = model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME, "debugger-source", "debugger-source");
        Assert.assertTrue(debuggerSource.hasDefined("enabled"));
        Assert.assertFalse(model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME, "debugger-source", "debugger-source", "enabled").asBoolean());
    }

    /**
     * Starts a controller with a given subsystem xml and then checks that a second
     * controller started with the xml marshalled from the first one results in the same model
     */
    @Test
    public void testParseAndMarshalModel() throws Exception {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml = getSubsystemXml();
        KernelServices servicesA = super.installInController(subsystemXml);
        
        //Get the model and the persisted xml from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        String marshalled = servicesA.getPersistedSubsystemXml();

        //Install the persisted xml from the first controller into a second controller
        KernelServices servicesB = super.installInController(marshalled);
        ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Starts a controller with the given subsystem xml and then checks that a second
     * controller started with the operations from its describe action results in the same model
     */
    @Test
    public void testDescribeHandler() throws Exception {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml = getSubsystemXml();
        KernelServices servicesA = super.installInController(subsystemXml);
        //Get the model and the describe operations from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(
                PathAddress.pathAddress(
                        PathElement.pathElement(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME)).toModelNode());
        List<ModelNode> operations = checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();


        //Install the describe options from the first controller into a second controller
        KernelServices servicesB = super.installInController(operations);
        ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Tests that the subsystem can be removed
     */
    @Test
    public void testSubsystemRemoval() throws Exception {
        String subsystemXml = getSubsystemXml();
        KernelServices services = super.installInController(subsystemXml);
        final ServiceName serviceName = ExceptionDispatcherService.createServiceName("exception");
        ServiceController<?> dispatcherService = services.getContainer()
                .getRequiredService(serviceName);
        Assert.assertNotNull(dispatcherService);
        
        //Checks that the subsystem was removed from the model
        super.assertRemoveSubsystemResources(services);

        //Check that any services that were installed were removed here
        try {
            services.getContainer().getRequiredService(serviceName);
            Assert.fail("Dispatcher service was not removed.");
        } catch (ServiceNotFoundException ex) {            
            // this is ok
        }
    }
        
    private String getSubsystemXml() {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml =
                "<subsystem xmlns=\"" + ExceptionExtension.NAMESPACE + "\">" +
                "<sources>"
                + "<logging-source enabled='true' />"
                + "<debugger-source enabled='false' />"
                + "</sources>" +
                "</subsystem>";
        return subsystemXml;
    }
}
