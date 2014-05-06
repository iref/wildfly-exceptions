package cz.muni.exceptions.listener.db.mybatis.mappers;

import cz.muni.exceptions.listener.db.model.TicketOccurence;

import java.util.List;

/**
 * @author Jan Ferko
 */
public interface TicketOccurrenceMapper {

    void insert(TicketOccurence ticketOccurence, Long ticketId);

    void update(TicketOccurence ticketOccurence);

    void delete(Long id);

    List<TicketOccurence> selectTicketOccurences(Long ticketId);

}
