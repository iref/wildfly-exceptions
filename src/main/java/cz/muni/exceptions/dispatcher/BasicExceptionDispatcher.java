
package cz.muni.exceptions.dispatcher;

import cz.muni.exceptions.listener.ExceptionListener;
import cz.muni.exceptions.source.ExceptionReport;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /** Filter, for filtering exception, that should not be reported. */
    private final ExceptionFilter filter;

    /** Indicator if dispatcher should dispatch new reports to listeners. */
    private AtomicBoolean isRunning = new AtomicBoolean(true);
    
    /**
     * Constructor constructs new instance without any registered listeners.
     *
     * @param filter filter for exception filtering.
     */
    public BasicExceptionDispatcher(ExceptionFilter filter) {
        this(filter, null);
    }
    
    /**
     * Constructor constructs new instance with listeners from {@code listeners}
     * collection.
     * 
     * @param listeners collection of listeners, that should be registered to source
     */
    public BasicExceptionDispatcher(ExceptionFilter filter, Collection<ExceptionListener> listeners) {
        this.filter = filter == null ? ExceptionFilters.ALWAYS_PASSES : filter;

        this.listeners = new HashSet<ExceptionListener>();
        
        if (listeners != null && !listeners.isEmpty()) {
            this.listeners.addAll(listeners);
        }
    }

    @Override
    public void warnListeners(ExceptionReport report) {
        if (!isRunning.get() || report == null || filter.apply(report)) {
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
    public void start() {
        isRunning.compareAndSet(false, true);
    }

    @Override
    public void stop() {
        isRunning.compareAndSet(true, false);
    }

    @Override
    public Set<ExceptionListener> getListeners() {
        return new HashSet<ExceptionListener>(this.listeners);
    }

}
