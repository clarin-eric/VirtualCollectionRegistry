package eu.clarin.cmdi.virtualcollectionregistry.core;

import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.EntityManager;
import java.util.ArrayDeque;
import java.util.Deque;

public abstract class TxManager {

    private static final Logger logger = LoggerFactory.getLogger(TxManager.class);

    private final Deque<String> txStack = new ArrayDeque<>();

    private boolean txEnabled = true;

    public void setTxEnable(boolean txEnabled) {
        this.txEnabled = txEnabled;
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

    protected void beginTransaction(final EntityManager em) {
        logger.debug("Current thread = {}", Thread.currentThread().getName());

        if(!txEnabled) {
            return;
        }

        txStack.push(getNewStackTraceLine(new Exception()));
        printTxStack("Begin tx");

        logger.trace(getNewStackTraceLine(new Exception()));
        if(!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    protected void commitActiveTransaction(final EntityManager em) {
        logger.debug("Current thread = {}", Thread.currentThread().getName());
        if(!txEnabled) {
            return;
        }

        if(!txStack.isEmpty()) {
            txStack.pop();
            printTxStack("Commit tx");

            if (em.getTransaction().isActive() && txStack.isEmpty()) {
                em.getTransaction().commit();
                logger.trace("Performing commit");
            }
        }
    }

    protected void rollbackActiveTransaction(final EntityManager em, String msg, Exception cause) throws VirtualCollectionRegistryException {
        logger.debug("Current thread = {}", Thread.currentThread().getName());

        if(!txEnabled) {
            return;
        }

        printTxStack("Rollback tx");

        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        txStack.clear();
        throw new VirtualCollectionRegistryException(msg, cause);
    }
}
