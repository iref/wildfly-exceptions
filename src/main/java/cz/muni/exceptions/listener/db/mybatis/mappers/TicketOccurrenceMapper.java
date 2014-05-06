package cz.muni.exceptions.listener.db.mybatis.mappers;

import cz.muni.exceptions.listener.db.model.TicketOccurence;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Interface for accessing database queries of {@link cz.muni.exceptions.listener.db.model.TicketOccurence}.
 *
 * @author Jan Ferko
 */
public interface TicketOccurrenceMapper {

    /**
     * Inserts ticket occurrence to database and associates it with ticket with given id.
     *
     * @param ticketOccurence ticket occurrence, that should be created.
     * @param ticketId identifier of ticket, that new occurrence is associated with
     */
    void insert(@Param("ticketOccurrence") TicketOccurence ticketOccurence, @Param("ticketId") Long ticketId);

    /**
     * Deletes all ticket occurrences of ticket with given identifier.
     *
     * @param ticketId identifier of ticket, which occurrences should be removed.
     */
    void deleteTicketOccurrences(@Param("ticketId") Long ticketId);

    /**
     * Returns list of all occurrences of ticket with given identifier.
     *
     * @param ticketId identifier of ticket, which occurrences should be retrieved.
     * @return list of all ticket occurrences or empty list if ticket has no occurrence.
     */
    List<TicketOccurence> selectTicketOccurrences(@Param("ticketId") Long ticketId);

}
