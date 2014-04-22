package cz.muni.exceptions.service;

import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * Service that provides exception dispatcher for other services.
 * 
 * @author Jan Ferko
 */
public class ExceptionDispatcherService implements Service<ExceptionDispatcher> {
    
    /** Wow Logger. */
    private static final Logger LOGGER = Logger.getLogger(ExceptionDispatcherService.class);
    
    /** Service name. */
    private static final String SERVICE_NAME = "Exception-ExceptionDispatcherService";
    
    /** Dispatcher, provided by service. */
    private final ExceptionDispatcher dispatcher;        
    
    /**
     * Constructor constructs new instance of service with provided dispatcher.
     * 
     * @param dispatcher dispatcher, that should be provided by service
     * @throws IllegalArgumentException if {@code dispatcher} is {@code null}.
     */
    public ExceptionDispatcherService(ExceptionDispatcher dispatcher) {
        if (dispatcher == null) {
            throw new IllegalArgumentException("[Dispatcher] is required and must not be null.");
        }
        this.dispatcher = dispatcher;
    }
    
    public static ServiceName createServiceName() {
        return ServiceName.JBOSS.append(SERVICE_NAME);
    }

    @Override
    public void start(StartContext context) throws StartException {
        LOGGER.infov("Starting {} service", SERVICE_NAME);
    }

    @Override
    public void stop(StopContext context) {
        LOGGER.infov("Stopping {} service", SERVICE_NAME);
    }

    @Override
    public ExceptionDispatcher getValue() throws IllegalStateException, IllegalArgumentException {
        return dispatcher;
    }
    
}
