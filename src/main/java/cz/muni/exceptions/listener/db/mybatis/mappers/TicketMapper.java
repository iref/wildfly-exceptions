package cz.muni.exceptions.listener.db.mybatis.mappers;

import cz.muni.exceptions.listener.db.model.Ticket;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * Interface for accessing database queries of {@link Ticket}.
 *
 * @author Jan Ferko
 */
public interface TicketMapper {

    /**
     * Inserts new Ticket to database.
     *
     * @param ticket ticket, that should be inserted.
     */
    void insert(@Param("ticket") Ticket ticket);

    /**
     * Updates existing ticket in database.
     *
     * @param ticket ticket, that should be updated.
     */
    void update(@Param("ticket") Ticket ticket);

    /**
     * Deletes ticket with given id from database.
     *
     * @param id identifier of ticket, that should be removed.
     */
    void delete(@Param("ticketId") Long id);

    /**
     * Returns set of all existing tickets from database.
     *
     * @return set of all tickets from database or empty set if no ticket exists.
     */
    Set<Ticket> selectAllTickets();

    /**
     * Returns ticket with given id.
     *
     * @param id identifer of ticket, that should be retrieved.
     * @return ticket with given id or {@code null} if ticket does not exist
     */
    Ticket selectTicketById(@Param("ticketId") Long id);
}
