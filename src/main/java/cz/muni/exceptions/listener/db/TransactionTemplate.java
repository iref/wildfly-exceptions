package cz.muni.exceptions.listener.db;

import javax.persistence.EntityManager;

/**
 * Simple template for queries, that have to run in transaction.
 *  
 * @param <T> expected return type of query that should run in transaction
 * 
 * @author Jan Ferko
 * @date 2014-04-16T03:52:02+0100
 */
interface TransactionTemplate<T> {

    /**
     * Defines operations, that are required to run in transaction.
     * 
     * @param em entity manager, that provides access to database for this transaction
     * @return result of operations, that run in transaction
     */
    T executeInTransaction(EntityManager em);
}
