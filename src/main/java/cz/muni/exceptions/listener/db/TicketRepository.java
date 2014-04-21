package cz.muni.exceptions.listener.db;

import com.google.common.base.Optional;
import cz.muni.exceptions.listener.db.model.Ticket;
import java.util.Set;

/**
 * Interface for accessing tickets in data store.
 * 
 * @author Jan Ferko
 * @date 2014-04-16T03:40:28+0100
 */
public interface TicketRepository {
    
    /**
     * Adds new ticket into data store.
     * 
     * @param ticket ticket to add in datastore
     * @throws IllegalArgumentException if ticket's id is set
     */
    void add(Ticket ticket);
    
    /**
     * Updates existing ticket in data store.
     * 
     * @param ticket ticket thats is supposed to be updated
     * @throws IllegalArgumentException if ticket doesn't have id
     */
    void update(Ticket ticket);
    
    /**
     * Removes ticket with given id from data store.
     * 
     * @param ticketId id of ticket, that is supposed to be removed
     * @throws IllegalArgumentException if ticketId is {@code null}
     */
    void remove(Long ticketId);

    /**
     * Retrieves ticket with given id from data store.
     * 
     * @param ticketId id of ticket
     * @return option with given ticket if ticket exists, otherwise empty option
     * @throws IllegalArgumentException if ticketId is {@code null}
     */
    Optional<Ticket> get(Long ticketId);
    
    /**
     * Retrieves all tickets, that are stored in data store.
     * 
     * @return set of all tickets in data store or empty set if 
     * there isn't any ticket in data store.
     */
    Set<Ticket> all();
}
