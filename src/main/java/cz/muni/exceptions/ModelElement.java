package cz.muni.exceptions;

/**
 * Enum that contains all model elements names.
 * 
 * @author Jan Ferko
 */
public enum ModelElement {

    /** Grouping element for all sources */
    SOURCES("sources"),

    /** Grouping element for all listeners. */
    LISTENERS("listeners"),
    
    /** Logging source model elements */
    LOGGING_SOURCE("logging-source"),
    LOGGING_SOURCE_ENABLED("enabled"),
    
    /** Debugger source model elements */
    DEBUGGER_SOURCE("debugger-source"),
    DEBUGGER_SOURCE_ENABLED("enabled"),
    DEBUGGER_SOURCE_PORT("port"),

    /** Database listener model elements */
    DATABASE_LISTENER("database-listener"),
    DATABASE_LISTENER_DATA_SOURCE("dataSource"),
    DATABASE_LISTENER_JTA("isJta"),

    /** Exception dispatcher model elements */
    DISPATCHER("dispatcher"),
    DISPATCHER_ASYNC("async"),
    DISPATCHER_BLACKLIST("blacklist"),
    DISPATCHER_BLACKLIST_CLASS("class");
    
    /** Name of element */
    private final String name;
    
    /**
     * 
     * @param name element name
     */
    private ModelElement(String name) {
        this.name = name;
    }
    
    /**
     * Finds element with given name.
     * 
     * @param name name of the element
     * @return element with given name or {@code null} if element does not exists.
     */
    public static ModelElement forName(String name) {
        ModelElement found = null;
        for (ModelElement modelElement : values()) {
            if (modelElement.getName().equals(name)) {
                found = modelElement;
                break;
            }
        }
        
        return found;
    }
    
    /**
     * Returns name of the element.
     * 
     * @return name of the element.
     */
    public String getName() {
        return this.name;
    }
}
