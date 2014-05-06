package cz.muni.exceptions.listener.db.mybatis;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import cz.muni.exceptions.listener.db.TicketRepository;
import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketOccurence;
import cz.muni.exceptions.listener.db.mybatis.mappers.TicketMapper;
import cz.muni.exceptions.listener.db.mybatis.mappers.TicketOccurrenceMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Set;

/**
 * @author Jan Ferko
 */
public class MybatisTicketRepository implements TicketRepository {

    private final SqlSessionFactory sqlSessionFactory;

    public MybatisTicketRepository(SqlSessionFactory sqlSessionFactory) {
        if (sqlSessionFactory == null) {
            throw new IllegalArgumentException("[SqlSessionFactory] is required and should not be null.");
        }
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public void add(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("[Ticket] is required and should not be null.");
        }
        if (ticket.getId() != null) {
            throw new IllegalArgumentException("[Ticket] was already added.");
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(false);

        try {
            TicketMapper ticketMapper = sqlSession.getMapper(TicketMapper.class);
            TicketOccurrenceMapper ticketOccurrenceMapper = sqlSession.getMapper(TicketOccurrenceMapper.class);

            ticketMapper.insert(ticket);

            for (TicketOccurence ticketOccurence : ticket.getOccurences()) {
                ticketOccurrenceMapper.insert(ticketOccurence, ticket.getId());
            }
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public void update(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("[Ticket] is required and should not be null");
        }
        if (ticket.getId() == null) {
            throw new IllegalArgumentException("[Ticket] has not been stored yet, so it cannot be updated");
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(false);

        try {
            TicketMapper ticketMapper = sqlSession.getMapper(TicketMapper.class);
            TicketOccurrenceMapper ticketOccurrenceMapper = sqlSession.getMapper(TicketOccurrenceMapper.class);

            ticketMapper.update(ticket);
            ticketOccurrenceMapper.deleteTicketOccurences(ticket.getId());

            for (TicketOccurence ticketOccurence: ticket.getOccurences()) {
                ticketOccurrenceMapper.insert(ticketOccurence, ticket.getId());
            }
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public void remove(Long ticketId) {
        if (ticketId == null) {
            throw new IllegalArgumentException("[TicketId] is required and should not be null");
        }

        SqlSession sqlSession = sqlSessionFactory.openSession(false);

        try {
            TicketMapper ticketMapper = sqlSession.getMapper(TicketMapper.class);
            ticketMapper.delete(ticketId);
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public Optional<Ticket> get(Long ticketId) {
        if (ticketId == null) {
            throw new IllegalArgumentException("[TicketId] is required and should not be null.");
        }

        SqlSession sqlSession = sqlSessionFactory.openSession();
        Optional<Ticket> result = Optional.absent();
        try {
            TicketMapper ticketMapper = sqlSession.getMapper(TicketMapper.class);
            Ticket ticket = ticketMapper.selectTicketById(ticketId);
            result = Optional.fromNullable(ticket);
        } finally {
            sqlSession.close();
        }

        return result;
    }

    @Override
    public Set<Ticket> all() {
        SqlSession sqlSession = sqlSessionFactory.openSession();

        ImmutableSet.Builder<Ticket> tickets = ImmutableSet.builder();
        try {
            TicketMapper ticketMapper = sqlSession.getMapper(TicketMapper.class);
            tickets.addAll(ticketMapper.selectAllTickets());
        } finally {
            sqlSession.close();
        }

        tickets.build();
    }
}
