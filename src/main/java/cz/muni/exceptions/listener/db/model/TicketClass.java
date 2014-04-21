package cz.muni.exceptions.listener.db.model;

/**
 *
 * @author Jan Ferko
 * @sa.date 2014-04-15T05:51:32+0100
 */
public enum TicketClass {
    
    DATABASE(1),
    INTEGRATION(2),
    WEB(3),
    UTILS(4),
    NETWORK(5),
    FILE(6),
    JVM(7),
    UNKNOWN(8);
    
    private int id;
    
    private TicketClass(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
    
    public static TicketClass find(int id) {        
        for (TicketClass tc : values()) {
            if (tc.getId() == id) {
                return tc;
            }
        }
        
        return UNKNOWN;
    }

}
