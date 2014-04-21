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
 * Implementation of {@link cz.muni.exceptions.listener.db.TicketRepository}, that uses JPA
 * to access database.
 *
 * @author Jan Ferko
 * @date 2014-04-16T03:56:45+0100
 */
public class JPATicketRepository implements TicketRepository {

    /** PersistenceUnitCreator, that provides EntityManager for accessing database. */
    private final PersistenceUnitCreator creator;

    /**
     * Constructor creates new JPATicketRepository with given PersistenceUnitCreator.
     *
     * @param creator creator, that provides entity manager for accessing database
     * @throws java.lang.IllegalArgumentException if creator is {@code null}.
     */
    public JPATicketRepository(PersistenceUnitCreator creator) {
        if (creator == null) {
            throw new IllegalArgumentException("[Creator] is required and must not be null.");
        }
        this.creator = creator;
    }

    @Override
    public void add(final Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("[Ticket] should not be null.");
        }
        if (ticket.getId() != null) {
            throw new IllegalArgumentException("[Ticket] should have id.");
        }

        runInTransaction(new TransactionTemplate<Void>() {
            @Override
            public Void executeInTransaction(EntityManager em) {
                em.persist(ticket);
                return null;
            }
        });
    }

    @Override
    public void update(final Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("[Ticket] should not be null.");
        }
        if (ticket.getId() == null) {
            throw new IllegalArgumentException("[Ticket] should have id.");
        }

        runInTransaction(new TransactionTemplate<Void>() {
            @Override
            public Void executeInTransaction(EntityManager em) {
                em.merge(ticket);
                return null;
            }
        });
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

    /**
     * Method, that provides generic layout for running queries in transaction.
     *
     * @param transactionQuery query, that should be executed in transaction
     * @param <T> return type of query
     * @return result of {@code transactionQuery}
     * @throws java.lang.RuntimeException if any error occurs while query is running
     */
    private <T> T runInTransaction(TransactionTemplate<T> transactionQuery) {
        EntityManager em = creator.createEntityManager();
        EntityTransaction tx = null;

        try {
            // manage transactions only if entity manager isn't JTA managed
            if (!creator.isJtaManaged()) {
                tx = em.getTransaction();
                tx.begin();
            }

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
