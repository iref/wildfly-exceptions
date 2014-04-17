package cz.muni.exceptions.listener.db;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import cz.muni.exceptions.listener.db.model.Ticket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

/**
 *
 * @author Jan Ferko
 * @date 2014-04-16T03:56:45+0100
 */
public class JPATicketRepository implements TicketRepository {
    
    private final PersistenceUnitCreator creator;
    
    public JPATicketRepository(PersistenceUnitCreator creator) {
        this.creator = creator;
    }

    @Override
    public void add(Ticket ticket) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update(Ticket ticket) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(final Long ticketId) {
        if (ticketId == null) {
            throw new IllegalArgumentException("[TicketId] should not be null.");
        }

        runInTransaction(new TransactionTemplate<Void>() {
            @Override
            public Void executeInTransaction(EntityManager em) {
                Ticket ticket = em.find(Ticket.class, ticketId);
                if (ticket != null) {
                    em.remove(ticket);
                }

                return null;
            }
        });
    }

    @Override
    public Optional<Ticket> get(Long ticketId) {
        if (ticketId == null) {
            throw new IllegalArgumentException("[TicketId] should not be null.");
        }

        EntityManager em = creator.createEntityManager();
        Ticket ticket = em.find(Ticket.class, ticketId);
        Optional<Ticket> result = Optional.fromNullable(ticket);
        em.close();

        return result;
    }

    @Override
    public Set<Ticket> all() {
        EntityManager em = creator.createEntityManager();
        List<Ticket> tickets = em.createQuery("SELECT t FROM Ticket t", Ticket.class).getResultList();
        em.close();
        
        return ImmutableSet.copyOf(tickets);
    }

    private <T> T runInTransaction(TransactionTemplate<T> transactionQuery) {
        EntityManager em = creator.createEntityManager();
        EntityTransaction tx = null;

        try {
            tx = em.getTransaction();
            tx.begin();

            return transactionQuery.executeInTransaction(em);
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException("Error while executing transaction", ex);
        } finally {
            if (tx != null && tx.isActive()) {
                tx.commit();
            }
            em.close();
        }
    }

}
