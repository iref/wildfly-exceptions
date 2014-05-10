package cz.muni.exceptions.source;

import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.VirtualMachineManager;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 *
 * @author Jan Ferko 
 */
public class DebuggerExceptionSource {

    /** Wow Logger */
    private static final Logger LOGGER = Logger.getLogger(DebuggerExceptionSource.class.getSimpleName());

    /** Executor to run debugger threads. */
    private final ExecutorService executor;

    /** Dispatcher for sending exception reports */
    private final ExceptionDispatcher dispatcher;

    /** Translator, that translate debugger API objects to ExceptionReport. */
    private final DebuggerReferenceTranslator translator;

    /** Flag, for shutting down debugger thread. */
    private final AtomicBoolean stopDebuggerFlag = new AtomicBoolean(false);

    /** Port, where debugger agent is running. */
    private final int port;

    /**
     * Constructor, that constructs new instance of  debugger source.
     *
     * @param dispatcher dispatcher for sending exception reports.
     * @param translator translator, that translate debugger API objects to ExceptionReport
     * @param port port, where debugger agent is running
     * @throws java.lang.IllegalArgumentException if {@code dispatcher == null || translator == null}
     * or port is negative integer
     */
    public DebuggerExceptionSource(ExceptionDispatcher dispatcher, DebuggerReferenceTranslator translator, int port) {
        if (dispatcher == null) {
            throw new IllegalArgumentException("[Dispatcher] is required and should not be null.");
        }
        if (translator == null) {
            throw new IllegalArgumentException("[Translator] is required and should not be null.");
        }
        
        this.dispatcher = dispatcher;
        this.executor = Executors.newSingleThreadExecutor();
        this.translator = translator;
        this.port = port;
    }

    /**
     * Starts to listen for exceptions.
     */
    public void start() {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Starting DebuggerExceptionSource.");
        }
        final VirtualMachine vm = launchVirtualMachine();        
        
        Runnable listeningTask = new Runnable() {
            @Override
            public void run() {
                listen(vm);
                vm.exit(0);
            }            
        };                
        executor.submit(listeningTask);        
    }

    /**
     * Stops listening for exceptions.
     */
    public void stop() {
        LOGGER.log(Level.INFO, "Stopping debugger source");
        stopDebuggerFlag.set(true);
        
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Listening task was not stopped before termination timeout", ex);
        }
    }

    /**
     * Launches mirror of VM, that runs on {@code port}.
     *
     * @return mirror of VM
     */
    private VirtualMachine launchVirtualMachine() {
        AttachingConnector connector = findConnector();
        
        if (connector == null) {
            throw new IllegalStateException("Socket connector was not found check wildfly launching arguments.");
        }
        
        Map<String, Connector.Argument> arguments = prepareConectorArguments(connector);                
        try {
            return connector.attach(arguments);                  
        } catch (IllegalConnectorArgumentsException ex) {
            throw new IllegalStateException("Illegal arguments set on connector", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Exception while transferring data between VM and connector", ex);
        }
    }

    /**
     * Prepares configuration of VM connector.
     *
     * @param connector connector, that is supposed to be configured
     * @return map representing connector configuration
     */
    private Map<String, Connector.Argument> prepareConectorArguments(AttachingConnector connector) {
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        Connector.Argument portArgument = arguments.get("port");
        portArgument.setValue(String.valueOf(this.port));
        return arguments;
    }

    /**
     * Listens to events on targeted VM.
     *
     * @param vm targeted VM
     */
    private void listen(VirtualMachine vm) {        
        EventRequestManager requestManager = vm.eventRequestManager();
        LOGGER.info("RequestManager created.");

        ExceptionRequest exceptionRequest = createExceptionRequest(requestManager);        
        
        EventQueue eventQueue = vm.eventQueue();

        while (!stopDebuggerFlag.get()) {
            EventSet eventSet = null;            
            try {                
                eventSet = eventQueue.remove(1000L);
                if (eventSet == null) {
                    continue;
                }
                LOGGER.log(Level.INFO, "Size of event set is {0}.", eventSet.size());

                processEventSet(eventSet);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO, "No exception caught in this try");
                // no error occurred in last 1 seconds thats actually good :)
            } catch (VMDisconnectedException ex) {
                LOGGER.log(Level.INFO, "Target VM was disconnected.", ex);
                DebuggerExceptionSource.this.stop();
            } finally {
                if (eventSet != null) {
                    eventSet.resume();
                }
            }
        }
    }

    /**
     * Processes given event set.
     *
     * @param eventSet event set to be processed
     */
    private void processEventSet(EventSet eventSet) {
        for (Event event : eventSet) {
            if (event instanceof VMDeathEvent) {
                //deathRequest.disable();
                stopDebuggerFlag.set(true);
            } else if ( event instanceof VMDisconnectEvent) {
                stopDebuggerFlag.set(true);
            } else if (event instanceof ExceptionEvent) {
                LOGGER.log(Level.INFO, "Exception event was caught.");
                ExceptionEvent exceptionEvent = (ExceptionEvent) event;
                LOGGER.info("Creating report");
                ExceptionReport report = translator.processExceptionEvent(exceptionEvent);
                LOGGER.info("Sending report");
                dispatcher.warnListeners(report);
            }
        }
    }

    /**
     * Creates and send exception request to given request manager.
     * Request asks for every caught and uncaught exception, excepts
     * debugger api exceptions.
     *
     * @param requestManager request manager where request is sent to
     * @return created exception request
     */
    private ExceptionRequest createExceptionRequest(EventRequestManager requestManager) {
        ExceptionRequest exceptionRequest = requestManager.createExceptionRequest(null, true, true);
        exceptionRequest.setSuspendPolicy(ExceptionRequest.SUSPEND_NONE);
        exceptionRequest.addClassExclusionFilter("com.sun.tools.jdi.*");
        exceptionRequest.addClassExclusionFilter("com.sun.jdi.*");
        
        exceptionRequest.enable();
        LOGGER.info("ExceptionRequest sent..");
        
        return exceptionRequest;
    }

    /**
     * Finds attaching connector, that can be used to connect to VM
     *
     * @return attaching connector for connecting to target VM
     */
    private AttachingConnector findConnector() {
        VirtualMachineManager vmManager = Bootstrap.virtualMachineManager();
        
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Trying to attach to VirtualMachine Connector.");
        }
        
        AttachingConnector connector = null;        
        for (AttachingConnector attachingConnector : vmManager.attachingConnectors()) {
            String name = attachingConnector.name();
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "Checking connector [{0}]", attachingConnector.name());
            }
            if ("com.sun.jdi.SocketAttach".equals(name)) {
                connector = attachingConnector;
                break;
            }
        }
        return connector;
    }

}
