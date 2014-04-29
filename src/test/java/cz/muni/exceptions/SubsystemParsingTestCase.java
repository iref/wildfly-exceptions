package cz.muni.exceptions;


import cz.muni.exceptions.ExceptionExtension;
import cz.muni.exceptions.service.DatabaseListenerService;
import cz.muni.exceptions.service.DebuggerService;
import cz.muni.exceptions.service.ExceptionDispatcherService;
import junit.framework.Assert;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.AdditionalInitialization;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.jgroups.tests.bla;
import org.junit.Test;

import java.io.*;
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
        Assert.assertEquals(4, operations.size());

        //Check that each operation has the correct content
        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(ExceptionExtension.SUBSYSTEM_NAME, element.getValue());

        // check that dispatcher was set
        ModelNode addDispatcher = operations.get(1);
        Assert.assertEquals(ADD, addDispatcher.get(OP).asString());
        PathAddress dispatcherAddress = PathAddress.pathAddress(addDispatcher.get(OP_ADDR));
        Assert.assertEquals(2, dispatcherAddress.size());
        element = dispatcherAddress.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(ExceptionExtension.SUBSYSTEM_NAME, element.getValue());

        PathElement dispatcherElement = dispatcherAddress.getElement(1);
        Assert.assertEquals("dispatcher", dispatcherElement.getKey());
        Assert.assertEquals("dispatcher", dispatcherElement.getValue());
        Assert.assertTrue(addDispatcher.get("async").asBoolean());

        List<ModelNode> blacklist = addDispatcher.get("blacklist").asList();
        Assert.assertEquals(2, blacklist.size());
        Assert.assertEquals("java.util.SecurityException", blacklist.get(0).asString());
        Assert.assertEquals("java.lang.*", blacklist.get(1).asString());
        
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
        Assert.assertTrue(addDebuggerSource.get("enabled").asBoolean());
        Assert.assertEquals(addDebuggerSource.get("port").asInt(), 8787);

        // Check that database-listener was set
        ModelNode addDatabaseListener = operations.get(3);
        Assert.assertEquals(ADD, addDatabaseListener.get(OP).asString());
        PathAddress databaseListenerAddress = PathAddress.pathAddress(addDatabaseListener.get(OP_ADDR));
        Assert.assertEquals(2, databaseListenerAddress.size());
        element = databaseListenerAddress.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(ExceptionExtension.SUBSYSTEM_NAME, element.getValue());

        PathElement databaseListener = databaseListenerAddress.getElement(1);
        Assert.assertEquals("database-listener", databaseListener.getKey());
        Assert.assertEquals("database-listener", databaseListener.getValue());
        Assert.assertTrue(addDatabaseListener.get("isJta").asBoolean());
        Assert.assertEquals("java:jboss/datasources/ExampleDS", addDatabaseListener.get("dataSource").asString());
    }

    /**
     * Test that the model created from the xml looks as expected
     */
    @Test
    public void testInstallIntoController() throws Exception {
        //Parse the subsystem xml and install into the controller
        //Parse the subsystem xml into operations
        String subsystemXml = getSubsystemXml();
        KernelServices services = super.createKernelServicesBuilder(null)
                .setSubsystemXml(subsystemXml).build();
        
        //Read the whole model and make sure it looks as expected
        ModelNode model = services.readWholeModel();
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(ExceptionExtension.SUBSYSTEM_NAME));
        
        Assert.assertTrue(model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME).hasDefined("database-listener"));
        final ModelNode databaseListener = model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME, "database-listener", "database-listener");
        Assert.assertTrue(databaseListener.hasDefined("isJta"));
        Assert.assertTrue(databaseListener.get("isJta").asBoolean());
        Assert.assertTrue(databaseListener.hasDefined("dataSource"));
        Assert.assertEquals("java:jboss/datasources/ExampleDS", databaseListener.get("dataSource").asString());
        
        Assert.assertTrue(model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME).hasDefined("debugger-source"));        
        final ModelNode debuggerSource = model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME, "debugger-source", "debugger-source");
        Assert.assertTrue(debuggerSource.hasDefined("enabled"));
        Assert.assertTrue(debuggerSource.get("enabled").asBoolean());
        Assert.assertTrue(debuggerSource.hasDefined("port"));
        Assert.assertEquals(8787, debuggerSource.get("port").asInt());

        Assert.assertTrue(model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME).hasDefined("dispatcher"));
        final ModelNode dispatcher = model.get(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME, "dispatcher", "dispatcher");
        Assert.assertTrue(dispatcher.hasDefined("async"));
        Assert.assertTrue(dispatcher.get("async").asBoolean());
        Assert.assertTrue(dispatcher.hasDefined("blacklist"));
        List<ModelNode> blacklist = dispatcher.get("blacklist").asList();
        Assert.assertEquals(2, blacklist.size());
        Assert.assertEquals("java.util.SecurityException", blacklist.get(0).asString());
        Assert.assertEquals("java.lang.*", blacklist.get(1).asString());

    }

    /**
     * Starts a controller with a given subsystem xml and then checks that a second
     * controller started with the xml marshalled from the first one results in the same model
     */
    @Test
    public void testParseAndMarshalModel() throws Exception {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml = getSubsystemXml();
        KernelServices servicesA = super.createKernelServicesBuilder(null)
                .setSubsystemXml(subsystemXml).build();
        
        //Get the model and the persisted xml from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        String marshalled = servicesA.getPersistedSubsystemXml();

        //Install the persisted xml from the first controller into a second controller
        KernelServices servicesB = super.createKernelServicesBuilder(AdditionalInitialization.MANAGEMENT)
                .setSubsystemXml(marshalled).build();
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
        KernelServices servicesA = super.createKernelServicesBuilder(null)
                .setSubsystemXml(subsystemXml).build();
        //Get the model and the describe operations from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(
                PathAddress.pathAddress(
                        PathElement.pathElement(SUBSYSTEM, ExceptionExtension.SUBSYSTEM_NAME)).toModelNode());
        List<ModelNode> operations = checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();


        //Install the describe options from the first controller into a second controller
        KernelServices servicesB = super.createKernelServicesBuilder(AdditionalInitialization.MANAGEMENT)
                .setBootOperations(operations).build();
        ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    /**
     * Tests that the subsystem can be removed
     */
    @Test
    public void testSubsystemRemoval() throws Exception {
        String subsystemXml =   getSubsystemXml();
        KernelServices services = super.createKernelServicesBuilder(null)
                .setSubsystemXml(subsystemXml).build();
        final ServiceName serviceName = ExceptionDispatcherService.createServiceName();
        ServiceController<?> dispatcherService = services.getContainer()
                .getRequiredService(serviceName);
        Assert.assertNotNull(dispatcherService);

        final ServiceName debuggerServiceName = DebuggerService.createServiceName();
        ServiceController<?> debuggerService = services.getContainer()
                .getRequiredService(debuggerServiceName);
        Assert.assertNotNull(debuggerService);

        final ServiceName databaseServiceName = DatabaseListenerService.createServiceName();
        ServiceController<?> databaseService = services.getContainer()
                .getRequiredService(databaseServiceName);
        Assert.assertNotNull(databaseService);
        
        //Checks that the subsystem was removed from the model
        super.assertRemoveSubsystemResources(services);

        //Check that any services that were installed were removed here
        try {
            services.getContainer().getRequiredService(serviceName);
            Assert.fail("Dispatcher service was not removed.");
        } catch (ServiceNotFoundException ex) {            
            // this is ok
        }

        try {
            services.getContainer().getRequiredService(debuggerServiceName);
            Assert.fail("Debugger source was not removed.");
        } catch (ServiceNotFoundException ex) {
            // this is ok
        }

        try {
            services.getContainer().getRequiredService(databaseServiceName);
            Assert.fail("Database listener was not removed.");
        } catch (ServiceNotFoundException ex) {
            // this is ok
        }
    }
        
    private String getSubsystemXml() {
        //Parse the subsystem xml and install into the first controller
        File configFile = null;
        try {
            configFile = new File(getClass().getClassLoader().getResource("configs/complete_subsystem.xml").toURI());
        } catch (Exception ex) {
            throw new IllegalStateException("Configuration file was not found.", ex);
        }

        StringBuilder configBuilder = new StringBuilder();
        try (InputStreamReader isr = new FileReader(configFile);
            BufferedReader br = new BufferedReader(isr);) {

            String line = br.readLine();
            while (line != null) {
                configBuilder.append(line.trim());
                line = br.readLine();
            }

        } catch (Exception ex) {
            throw new IllegalStateException("Error occurred while loading subsystem configuration.", ex);
        }


        return configBuilder.toString();
    }
}
