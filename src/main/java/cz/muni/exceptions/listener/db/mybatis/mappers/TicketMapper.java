package cz.muni.exceptions.listener.db.mybatis.mappers;

import cz.muni.exceptions.listener.db.model.Ticket;

import java.util.List;
import java.util.Set;

/**
 * @author Jan Ferko
 */
public interface TicketMapper {

    void insert(Ticket ticket);

    void update(Ticket ticket);

    void remove(Long id);

    Set<Ticket> selectAllTickets();

    Ticket selectTicket(Long id);
}
