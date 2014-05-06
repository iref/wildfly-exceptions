package cz.muni.exceptions.listener.db.mybatis.mappers;

import cz.muni.exceptions.listener.db.model.TicketOccurence;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Jan Ferko
 */
public interface TicketOccurrenceMapper {

    void insert(@Param("ticketOccurrence") TicketOccurence ticketOccurence, @Param("ticketId") Long ticketId);

    void deleteTicketOccurrences(@Param("ticketId") Long ticketId);

    List<TicketOccurence> selectTicketOccurences(@Param("ticketId") Long ticketId);

}
