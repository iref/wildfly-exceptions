package cz.muni.exceptions.listener.db;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import cz.muni.exceptions.listener.db.model.Ticket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;

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
    public void remove(Long ticketId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Optional<Ticket> get(Long ticketId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Ticket> all() {
        EntityManager em = creator.createEntityManager();
        List<Ticket> tickets = em.createQuery("SELECT t FROM Ticket t", Ticket.class).getResultList();
        em.close();
        
        return new HashSet<>(tickets);
    }

}
