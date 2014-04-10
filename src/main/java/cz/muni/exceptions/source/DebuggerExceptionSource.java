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
    
    private static final Logger LOGGER = Logger.getLogger(DebuggerExceptionSource.class.getSimpleName());
    
    private final ExecutorService executor;
    
    private final ExceptionDispatcher dispatcher;
    
    private final DebuggerReferenceTranslator translator;
    
    private final AtomicBoolean stopDebuggerFlag = new AtomicBoolean(false);        

    public DebuggerExceptionSource(ExceptionDispatcher dispatcher, DebuggerReferenceTranslator translator) {
        if (dispatcher == null) {
            throw new IllegalArgumentException("[Dispatcher] is required and should not be null.");
        }
        if (translator == null) {
            throw new IllegalArgumentException("[Translator] is required and should not be null.");
        }
        
        this.dispatcher = dispatcher;
        this.executor = Executors.newSingleThreadExecutor();
        this.translator = translator;
    }        
    
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
    
    public void stop() {
        LOGGER.log(Level.INFO, "Stopping debugger source");
        stopDebuggerFlag.set(true);
        
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, "Listening task was not stopped before termination timeout", ex);
        }
    }
    
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

    private Map<String, Connector.Argument> prepareConectorArguments(AttachingConnector connector) {
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        Connector.Argument portArgument = arguments.get("port");
        portArgument.setValue("8000");
        return arguments;
    }
    
    private void listen(VirtualMachine vm) {        
        EventRequestManager requestManager = vm.eventRequestManager();
        LOGGER.info("RequestManager instanciated.");                

        
        ExceptionRequest exceptionRequest = createExceptionRequest(requestManager);        
        
        EventQueue eventQueue = vm.eventQueue();
        
        while (!stopDebuggerFlag.get()) {            
            EventSet eventSet = null;            
            try {                
                eventSet = eventQueue.remove(1000L);                
            } catch (InterruptedException ex) {
                LOGGER.log(Level.INFO, "No exception caught in this try");
                // no error occurred in last 5 seconds thats actually good :)
            } catch (VMDisconnectedException ex) {
                LOGGER.log(Level.INFO, "Target VM was disconnected.", ex);
                DebuggerExceptionSource.this.stop();
            }
            
            if (eventSet == null) {
                continue;
            }
            
            LOGGER.log(Level.INFO, "Size of event set is {0}.", eventSet.size());
            
            for (Event event : eventSet) {
                if (event instanceof VMDeathEvent) {
                    //deathRequest.disable();
                    stopDebuggerFlag.set(true);
                } else if ( event instanceof VMDisconnectEvent) {
                    stopDebuggerFlag.set(true);
                } else if (event instanceof ExceptionEvent) {  
                    LOGGER.log(Level.INFO, "Exception event was caught.");
                    ExceptionEvent exceptionEvent = (ExceptionEvent) event;                    
                    ExceptionReport report = translator.processExceptionEvent(exceptionEvent);                    
                    dispatcher.warnListeners(report);
                }
            }            
            eventSet.resume();
        }                
    }                
    
    private ExceptionRequest createExceptionRequest(EventRequestManager requestManager) {
        ExceptionRequest exceptionRequest = requestManager.createExceptionRequest(null, true, true);
        exceptionRequest.setSuspendPolicy(ExceptionRequest.SUSPEND_NONE);
        exceptionRequest.addClassExclusionFilter("com.sun.tools.jdi.*");
        exceptionRequest.addClassExclusionFilter("com.sun.jdi.*");        
        
        exceptionRequest.enable();
        LOGGER.info("ExceptionRequest sent..");
        
        return exceptionRequest;
    }        

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
