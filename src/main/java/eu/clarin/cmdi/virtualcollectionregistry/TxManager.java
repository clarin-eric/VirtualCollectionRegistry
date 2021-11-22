package eu.clarin.cmdi.virtualcollectionregistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManager;
import java.util.ArrayDeque;
import java.util.Deque;

public abstract class TxManager {

    private static final Logger logger = LoggerFactory.getLogger(TxManager.class);

    private final Deque<String> txStack = new ArrayDeque<>();

    protected final DataStore datastore;

    public TxManager(DataStore datastore) {
        this.datastore = datastore;
    }

    protected String getNewStackTraceLine(Exception ex) {
        return getNewStackTraceLine(ex.getStackTrace());
    }

    protected String getNewStackTraceLine(StackTraceElement[] trace) {
        String msg = String.format(
            "%s %s:%d",
            trace[1].getClassName(),
            trace[1].getMethodName(),
            trace[1].getLineNumber()
        );
        return msg;
    }

    protected void printTxStack(String prefix) {
        logger.trace("{} (size={})", prefix, txStack.size());
        for(String s : txStack) {
            logger.trace("   {}", s);
        }
    }

    protected void beginTransaction() {
        txStack.push(getNewStackTraceLine(new Exception()));
        printTxStack("Begin tx");

        final EntityManager em = datastore.getEntityManager();
        logger.trace(getNewStackTraceLine(new Exception()));
        if(!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    protected void commitActiveTransaction() {
        if(!txStack.isEmpty()) {
            txStack.pop();
            printTxStack("Commit tx");

            final EntityManager em = datastore.getEntityManager();
            if (em.getTransaction().isActive() && txStack.isEmpty()) {
                em.getTransaction().commit();
                logger.trace("Performing commit");
            }
        }
    }

    protected void rollbackActiveTransaction(String msg, Exception cause) throws VirtualCollectionRegistryException {
        printTxStack("Rollback tx");

        final EntityManager em = datastore.getEntityManager();
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        txStack.clear();
        throw new VirtualCollectionRegistryException(msg, cause);
    }
}
