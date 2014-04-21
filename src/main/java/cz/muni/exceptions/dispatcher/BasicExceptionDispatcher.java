
package cz.muni.exceptions.dispatcher;

import cz.muni.exceptions.listener.ExceptionListener;
import cz.muni.exceptions.source.ExceptionReport;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic implementation of exception dispatcher. 
 * Class provides event dispatching to listeners and listeners registration.
 * Class dispatches events synchronously. 
 * This means that listeners are notified about exception in order that, 
 * they occurred. 
 * 
 * @author Jan Ferko 
 */
public class BasicExceptionDispatcher implements ExceptionDispatcher {
    
    /** Set of registered listeners. */
    private final Set<ExceptionListener> listeners;
    
    /**
     * Constructor constructs new instance without any registered listeners.
     */
    public BasicExceptionDispatcher() {
        this(null);
    }
    
    /**
     * Constructor constructs new instance with listeners from {@code listeners}
     * collection.
     * 
     * @param listeners collection of listeners, that should be registered to source
     */
    public BasicExceptionDispatcher(Collection<ExceptionListener> listeners) {
        this.listeners = new HashSet<ExceptionListener>();        
        
        if (listeners != null && !listeners.isEmpty()) {
            this.listeners.addAll(listeners);
        }
    }

    @Override
    public void warnListeners(ExceptionReport report) {
        if (report == null) {
            return;
        }
        
        for (ExceptionListener listener : this.listeners) {
            listener.onThrownException(report);
        }
    }

    @Override
    public void registerListener(ExceptionListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }        
    }

    @Override
    public void unregisterListener(ExceptionListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public Set<ExceptionListener> getListeners() {
        return new HashSet<ExceptionListener>(this.listeners);
    }

}
