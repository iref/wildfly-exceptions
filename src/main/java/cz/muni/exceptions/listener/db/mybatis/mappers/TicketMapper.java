package cz.muni.exceptions.listener.db.mybatis.mappers;

import cz.muni.exceptions.listener.db.model.Ticket;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

/**
 * @author Jan Ferko
 */
public interface TicketMapper {

    void insert(@Param("ticket") Ticket ticket);

    void update(@Param("ticket") Ticket ticket);

    void delete(@Param("ticketId") Long id);

    Set<Ticket> selectAllTickets();

    Ticket selectTicketById(@Param("ticketId") Long id);
}
