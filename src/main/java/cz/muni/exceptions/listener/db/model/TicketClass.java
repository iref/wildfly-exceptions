package cz.muni.exceptions.listener.db.model;

/**
 *
 * @author Jan Ferko
 * @sa.date 2014-04-15T05:51:32+0100
 */
public enum TicketClass {
    
    DATABASE(1, "database"),
    INTEGRATION(2, "integration"),
    WEB(3, "web"),
    UTILS(4, "utils"),
    NETWORK(5, "network"),
    FILE(6, "file"),
    JVM(7, "jvm"),
    CONCURRENCY(8, "concurrency"),
    MESSAGING(9, "messaging"),
    XML(10, "xml"),
    TRANSACTION(11, "transaction"),
    SECURITY(12, "security"),
    UNKNOWN(13, "unknown");
    
    private int id;

    private String key;
    
    private TicketClass(int id, String key) {
        this.id = id;
        this.key = key;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public static TicketClass find(int id) {
        for (TicketClass tc : values()) {
            if (tc.getId() == id) {
                return tc;
            }
        }
        
        return UNKNOWN;
    }

    public static TicketClass forKey(String key) {
        for (TicketClass tc: values()) {
            if (tc.getKey().equalsIgnoreCase(key)) {
                return tc;
            }
        }

        return UNKNOWN;
    }

}
