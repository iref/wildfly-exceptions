package cz.muni.exceptions.listener.db.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jan Ferko
 * @sa.date 2014-04-15T05:40:59+0100
 */
@Entity
@Table(name = "tickets")
public class Ticket implements Serializable {
    
    @Id
    @SequenceGenerator(name = "ticketIdGenerator", sequenceName = "tickets_id_seq")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Column(name = "detail_message")
    private String detailMessage;

    @Column(name="class_name")
    private String className;
    
    @Lob
    @Column(name = "stack_trace")
    private String stackTrace;
        
    @Column(name = "ticket_class_id")
    private int ticketClassId;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_id", referencedColumnName = "id")
    private List<TicketOccurence> occurences;

    public Ticket() {
    }

    public Ticket(String detailMessage, String className, String stackTrace,
            TicketClass ticketClass, List<TicketOccurence> occurences) {        
        this.detailMessage = detailMessage;
        this.className = className;
        this.stackTrace = stackTrace;        
        this.occurences = occurences;
        this.ticketClassId = ticketClass == null 
                ? TicketClass.UNKNOWN.getId() : ticketClass.getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public void setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public List<TicketOccurence> getOccurences() {
        return occurences;
    }

    public void setOccurences(List<TicketOccurence> occurences) {
        this.occurences = occurences;
    }
    
    public TicketClass getTicketClass() {
        return TicketClass.find(ticketClassId);
    }
    
    public void setTicketClass(TicketClass ticketClass) {
        this.ticketClassId = ticketClass == null 
                ? TicketClass.UNKNOWN.getId() : ticketClass.getId();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Ticket)) {
            return false;
        }
        final Ticket other = (Ticket) obj;
        return Objects.equals(this.id, other.id);
    }
    
    @Override
    public String toString() {
        return String.format("Ticket {id=%1$s, detailMessage=%2$s, class=%3$s}", 
                id, detailMessage, getTicketClass());
    }

}
