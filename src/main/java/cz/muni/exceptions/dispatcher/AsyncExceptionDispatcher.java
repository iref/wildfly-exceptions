package cz.muni.exceptions.dispatcher;

import cz.muni.exceptions.listener.ExceptionListener;
import cz.muni.exceptions.source.ExceptionReport;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jboss.logging.Logger;

/**
 * Asynchronous implementation of exception dispatcher.
 * It uses BlockingQueue to store exceptions that should be processed.
 * 
 * @author Jan Ferko
 */
public class AsyncExceptionDispatcher implements ExceptionDispatcher {
    
    /** Wow Logger. */
    private static final Logger LOG = Logger.getLogger(AsyncExceptionDispatcher.class);
    
    /** Queue, where throwable are stored to be processed in future. */
    private final BlockingQueue<ExceptionReport> exceptionQueue;

    /** Filter for skipping exception reports.  */
    private final ExceptionFilter filter;
    
    /** Set of listeners, that are notified where throwable is processed. */
    private final Set<ExceptionListener> listeners;
    
    /** Flag, that indicates if dispatcher should process new throwables. */
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    
    /** Executor that runs processing tasks. */
    private ExecutorService executor;
    
    /**
     * Constructor, builds new dispatcher without registered listeners.
     *      
     * @param executor executor, that runs processing tasks.
     * @throws IllegalArgumentException if executor is {@code null} or cannot
     *  create new thread
     */
    public AsyncExceptionDispatcher(ExecutorService executor, ExceptionFilter filter) {
        this(executor, filter, Collections.EMPTY_LIST);
    }
    
    /**
     * Constructor creates dispatcher and registers listeners.
     * If listeners are {@code null}, they are ignored.
     * 
     * @param listeners listeners, that are registered in dispatcher
     * @param executor executor, that runs processing tasks.
     * @throws IllegalArgumentException if threadFactory is {@code null} or 
     *  cannot create new threads.
     */
    public AsyncExceptionDispatcher(ExecutorService executor, ExceptionFilter filter,
                                    Collection<ExceptionListener> listeners) {
        if (executor == null) {
            throw new IllegalArgumentException("[Executor] is required and must not be null.");
        }
        this.executor = executor;
        this.filter = filter == null ? ExceptionFilters.ALWAYS_PASSES : filter;
        this.listeners = new HashSet<>();
        if (listeners != null && !listeners.isEmpty()) {
            for (ExceptionListener listener : listeners) {
                if (listener != null) {
                    this.listeners.add(listener);
                }
            }
        }
        
        this.exceptionQueue = new LinkedBlockingQueue<>();                         
    }

    @Override
    public void warnListeners(ExceptionReport exceptionReport) {
        if (exceptionReport == null || filter.apply(exceptionReport)) {
            return;
        }
        
        try {
            exceptionQueue.put(exceptionReport);
            LOG.error("Report was stored in queue");
        } catch (InterruptedException ex) {
            LOG.error("Throwable was not added to queue because of interruption", ex);
        }        
    }

    @Override
    public void registerListener(ExceptionListener listener) {
        if (listener == null) {
            return;
        }        
        synchronized (listeners) {            
            listeners.add(listener);
        }        
    }

    @Override
    public void unregisterListener(ExceptionListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void start() {
        if (!isRunning.get()) {
            isRunning.compareAndSet(false, true);
            this.executor.execute(new WarnListenersTask());
        }
    }

    @Override
    public void stop() {
        isRunning.compareAndSet(true, false);
        try {
            this.executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            LOG.error("It wasn't possible to terminate processing task", ex);
        }
    }

    @Override
    public Set<ExceptionListener> getListeners() {
        synchronized (listeners) {
            return new HashSet(listeners);
        }        
    }
    
    /**
     * Tasks, that calls listeners if new exception occurs in exception queue.
     */
    private class WarnListenersTask implements Runnable {                                

        @Override
        public void run() {
            final BlockingQueue<ExceptionReport> processingQueue = AsyncExceptionDispatcher.this.exceptionQueue;
            final Set<ExceptionListener> listeners = AsyncExceptionDispatcher.this.listeners;
            
            while(isRunning.get()) {
                LOG.info("Processing queue");
                ExceptionReport toProcess = null;
                try {
                    toProcess = processingQueue.take();
                } catch (InterruptedException ex) {
                    LOG.error("Taking throwable from queue was interrupted", ex);
                }                                
                
                LOG.info("Got report: " + toProcess);
                if (toProcess != null) {
                    LOG.warn(listeners);
                    for (ExceptionListener listener : listeners) {
                        try {
                            LOG.info("Sending report to " + listener);
                            listener.onThrownException(toProcess);
                        } catch (Exception ex) {
                            LOG.error("Error while notifying listener:", ex);
                        }
                        
                    }
                }
            }                        
        }        
    }
    
}
