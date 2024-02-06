package eu.clarin.cmdi.virtualcollectionregistry.core.reference;

import eu.clarin.cmdi.virtualcollectionregistry.core.DataStore;
import eu.clarin.cmdi.virtualcollectionregistry.core.TxManager;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class VirtualCollectionRegistryReferenceValidatorImpl extends TxManager implements VirtualCollectionRegistryReferenceValidator, InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionRegistryReferenceValidatorImpl.class);

    private final transient List<VirtualCollectionRegistryReferenceValidatorWorker> workers = new CopyOnWriteArrayList<>();

    @Autowired
    private VcrConfig vcrConfig;

    private boolean running = false;

    public VirtualCollectionRegistryReferenceValidatorImpl() { }

    // called by Spring directly after Bean construction
    @Override
    public void afterPropertiesSet() { }

    @Override
    public void shutdown() { }

    @Override
    public synchronized void perform(long now, DataStore datastore) throws VirtualCollectionRegistryException {
        try {
            if(!running) {
                running = true;

                //Fetch and process list of work items
                List<ResourceScan> scans = fetchListOfWork(datastore.getEntityManager());
                if(scans.size() > 0) {
                    logger.debug("Found {} resource scan(s) requiring analysing.", scans.size());
                }
                try {
                    beginTransaction(datastore.getEntityManager());

                    for (ResourceScan scan : scans) {
                        logger.trace("Marking start for scan with ref = {}", scan.getRef());
                        markScanStarted(datastore.getEntityManager(), scan);
                        logger.trace("Starting work for scan with ref = {}", scan.getRef());
                        VirtualCollectionRegistryReferenceValidatorWorker.WorkerResult result =
                                new VirtualCollectionRegistryReferenceValidatorWorker().doWork(scan.getRef());

                        //Update and persist scan
                        scan.setMimeType(result.getMimeType());
                        scan.setHttpResponseCode(result.getHttpResponseCode());
                        scan.setHttpResponseMessage(result.getHttpResponseMsg());
                        scan.setLastScanEnd(new Date());
                        scan.setException(result.getException());
                        scan.setNameSuggestion(result.getNameSuggestion());
                        scan.setDescriptionSuggestion(result.getDescriptionSuggestion());
                        datastore.getEntityManager().merge(scan);

                        logger.info("Finished resource scan for ref = {} in {}s, http response = {}", scan.getRef(), scan.getDurationInSeconds(), scan.getHttpResponseCode());
                    }
                } catch(Exception ex) {
                    rollbackActiveTransaction(datastore.getEntityManager(), "Failed to update resource scan", ex);
                } finally {
                    commitActiveTransaction(datastore.getEntityManager());
                }
            }
        } catch (Exception ex) {
            logger.info("Perform failed", ex);
        } finally {
            running = false;
        }
    }

    /**
     * Fetch list of resources that require scanning from the database.
     *
     * @param em
     * @return
     * @throws VirtualCollectionRegistryException
     */
    private synchronized List<ResourceScan>  fetchListOfWork(final EntityManager em) throws VirtualCollectionRegistryException {
        List<ResourceScan> scans = new ArrayList<>();
        try {
            beginTransaction(em);
            TypedQuery<ResourceScan> q = em.createNamedQuery("ResourceScan.findScanRequired", ResourceScan.class);
            scans = q.getResultList();
        } catch (Exception ex) {
            rollbackActiveTransaction(em, "Failed to fetch scan resources", ex);
        } finally {
            commitActiveTransaction(em);
        }
        return scans;
    }

    /**
     * Update the properties of the ResourceScan to mark it as being analysed to prevent other workers of processing this
     * item.
     *
     * @param em
     * @param scan
     */
    private synchronized void markScanStarted(final EntityManager em, ResourceScan scan) {//throws VirtualCollectionRegistryException {
        beginTransaction(em);
        scan.setLastScan(new Date());
        scan.setLastScanEnd(null);
        em.merge(scan);
        commitActiveTransaction(em);
    }
}