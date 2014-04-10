package cz.muni.exceptions.service;

import cz.muni.exceptions.dispatcher.ExceptionDispatcher;
import cz.muni.exceptions.source.DebuggerExceptionSource;
import cz.muni.exceptions.source.DebuggerReferenceTranslator;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by johnny on 4/10/14.
 */
public class DebuggerService implements Service<DebuggerExceptionSource> {

    /** Service name. */
    private static final String SERVICE_NAME = "DebuggerExceptionSource";

    /** Translator to translate Debugger API classes into model. */
    private final DebuggerReferenceTranslator translator;

    private DebuggerExceptionSource instance;

    private final InjectedValue<ExceptionDispatcher> exceptionDispatcher = new InjectedValue<>();

    public DebuggerService(DebuggerReferenceTranslator translator) {
        if (translator == null) {
            throw new IllegalArgumentException("[Translator] is required and should not be null.");
        }
        this.translator = translator;
    }

    public static ServiceName createServiceName(String alias) {
        return ServiceName.JBOSS.append(SERVICE_NAME, alias);
    }

    @Override
    public void start(StartContext context) throws StartException {
        ExceptionDispatcher dispatcher = this.exceptionDispatcher.getValue();
        instance = new DebuggerExceptionSource(dispatcher, translator);
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
