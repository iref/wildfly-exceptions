
package cz.muni.exceptions.source;

import cz.muni.exceptions.listener.ExceptionListener;
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
    public void warnListeners(Throwable throwable) {
        if (throwable == null) {
            return;
        }
        
        for (ExceptionListener listener : this.listeners) {
            listener.onThrownException(throwable);
        }
    }

    @Override
    public void registerListener(ExceptionListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }        
    }

    @Override
    public Set<ExceptionListener> getListeners() {
        return new HashSet<ExceptionListener>(this.listeners);
    }

}
