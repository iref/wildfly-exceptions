package cz.muni.exceptions.service;

import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import cz.muni.exceptions.source.DebuggerExceptionSource;
import cz.muni.exceptions.source.DebuggerReferenceTranslator;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;

/**
 * Service, that launches debugger exception source.
 *
 * @author Jan Ferko
 */
public class DebuggerService implements Service<DebuggerExceptionSource> {

    /** Service name. */
    private static final String SERVICE_NAME = "Exceptions-DebuggerExceptionSource";

    /** Translator to translate Debugger API classes into model. */
    private final DebuggerReferenceTranslator translator;

    /** Port, where debugger agent is running. */
    private final int port;

    /** Created instance of debugger exception source. */
    private DebuggerExceptionSource instance;

    /** Exception dispatcher registered in AS. */
    private final InjectedValue<ExceptionDispatcher> exceptionDispatcher = new InjectedValue<>();

    /**
     * Constructor
     */
    public DebuggerService(DebuggerReferenceTranslator translator, int port) {
        if (translator == null) {
            throw new IllegalArgumentException("[Translator] is required and should not be null.");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("[Port] should be positive integer.");
        }
        this.translator = translator;
        this.port = port;
    }

    public static ServiceName createServiceName() {
        return ServiceName.JBOSS.append(SERVICE_NAME);
    }

    @Override
    public void start(StartContext context) throws StartException {
        ExceptionDispatcher dispatcher = this.exceptionDispatcher.getValue();
        instance = new DebuggerExceptionSource(dispatcher, translator, port);
        instance.start();
    }

    @Override
    public void stop(StopContext context) {
        if (instance != null) {
            instance.stop();
        }
    }

    public InjectedValue<ExceptionDispatcher> getExceptionDispatcher() {
        return this.exceptionDispatcher;
    }

    @Override
    public DebuggerExceptionSource getValue() throws IllegalStateException, IllegalArgumentException {
        return this.instance;
    }
}
