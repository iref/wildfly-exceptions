package cz.muni.exceptions;

/**
 * Enum that contains all model elements names.
 * 
 * @author Jan Ferko
 */
public enum ModelElement {
    
    /** Logging source model elements */
    LOGGING_SOURCE("logging-source"),
    LOGGING_SOURCE_ENABLED("enabled"),
    
    /** Debugger source model elements */
    DEBUGGER_SOURCE("debugger-source"),
    DEBUGGER_SOURCE_ENABLED("enabled"),
    DEBUGGER_SOURCE_PORT("port");
    
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
