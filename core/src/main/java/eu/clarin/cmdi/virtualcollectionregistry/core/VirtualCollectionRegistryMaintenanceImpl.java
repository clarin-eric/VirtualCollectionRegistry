/*
 * Copyright (C) 2016 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.core;

import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifier;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import org.apache.commons.httpclient.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author wilelb
 */
@Component
public class VirtualCollectionRegistryMaintenanceImpl implements VirtualCollectionRegistryMaintenance {
    
    private static final Logger logger
            = LoggerFactory.getLogger(VirtualCollectionRegistryMaintenanceImpl.class);

    private final boolean maintenance_enabled = true;

    @Autowired
    private DataStore datastore; //TODO: replace with Spring managed EM?

    @Autowired
    private PidProviderService pidProviderService;

    @Autowired
    private PermaLinkService permaLinkService;

    @Override
    public void perform(long now) {
        if(!maintenance_enabled) {
            return;
        }

        logger.debug("Maintenance check (now={})", now);
        long t1 = System.nanoTime();
        
        // allocate persistent identifier roughly after 30 seconds
        final Date nowDateAlloc = new Date(now - 30 * 1000);
        // (for now) purge deleted collection roughly after 30 seconds
        final Date nowDatePurge = new Date(now - 30 * 1000);
        // handle collections in error immediately
        final Date nowDateError = new Date(now);

        EntityManager em = datastore.getEntityManager();
        try {
            allocatePersistentIdentifiers(em, nowDateAlloc);
            updatePersistentIdentifiers(em, nowDateAlloc);
            purgeDeletedCollections(em, nowDatePurge);
            handleCollectionsInError(em, nowDateError);
        } catch (RuntimeException e) {
            logger.error("unexpected error while doing maintenance", e);
        } finally {
            datastore.closeEntityManager();
        }
        
        long t2 = System.nanoTime();
        logger.debug(String.format("Maintenance check finished in %.3fms", (t2-t1)/1000000.0));
    }
    
    /*
     * Select a list of VirtualCollections that qualify to get a PID assigned (delayed).
     *
     * @param em
     * @param nowDateAlloc
     */
    protected void allocatePersistentIdentifiers(EntityManager em, final Date nowDateAlloc) {
        TypedQuery<VirtualCollection> q
                = em.createNamedQuery("VirtualCollection.findAllByStates",
                        VirtualCollection.class);
        List<VirtualCollection.State> states = new LinkedList<>();
        states.add(VirtualCollection.State.PUBLIC_PENDING);
        states.add(VirtualCollection.State.PUBLIC_FROZEN_PENDING);
        q.setParameter("states", states);
        q.setParameter("date", nowDateAlloc);
        q.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        em.getTransaction().begin();
        List<VirtualCollection> collectionsToUpdate = q.getResultList();
        em.getTransaction().commit();

        if(collectionsToUpdate.size() > 0) {
            logger.info("Assigning pid to #{} collections", collectionsToUpdate.size());
            for (VirtualCollection vc : collectionsToUpdate) {
                try {
                    em.getTransaction().begin();
                    allocatePersistentIdentifierForCollection(em, vc);
                    em.merge(vc);
                } catch(Exception ex) {
                    em.getTransaction().rollback();
                } finally {
                    em.getTransaction().commit();
                }
            }
        }
    }   
    
    /**
     * Assign a PID for this VirtualCollection.
     * 
     * @param em
     * @param vc 
     */
    protected void allocatePersistentIdentifierForCollection(EntityManager em, VirtualCollection vc) {
        if(!vc.hasPersistentIdentifier()) {
            try {
                //Mint identifiers and update collection
                List<PersistentIdentifier> pids = pidProviderService.createIdentifiers(vc, permaLinkService);
                for(PersistentIdentifier pid : pids) {
                    vc.setPersistentIdentifier(pid);
                }

                if(vc.getParent() == null) {
                    //new collection, mint new latest pid
                    List<PersistentIdentifier> latestPids = pidProviderService.createLatestIdentifiers(vc, permaLinkService);
                    for(PersistentIdentifier latestPid : latestPids) {
                        vc.setPersistentIdentifier(latestPid);
                    }
                } else {
                    //update latest pid to point to this collection
                    Set<PersistentIdentifier> latestPids = vc.getParent().getLatestIdentifiers();
                    for(PersistentIdentifier latestPid : latestPids) {
                        latestPid.setVirtualCollection(vc);
                    }
                }

                //If no errors occured, update collection state
                switch(vc.getState()) {
                    case PUBLIC_PENDING:
                        vc.setState(VirtualCollection.State.PUBLIC);
                        vc.setPublicLeaf(true);
                        if(vc.getParent() != null) {
                            vc.getParent().setPublicLeaf(false);
                        }
                        break;
                    case PUBLIC_FROZEN_PENDING:
                        vc.setState(VirtualCollection.State.PUBLIC_FROZEN);
                        vc.setPublicLeaf(true);
                        if(vc.getParent() != null) {
                            vc.getParent().setPublicLeaf(false);
                        }
                        break;
                    default: throw new VirtualCollectionRegistryException("Invalid state transition: "+vc.getState()+" --> PUBLIC or PUBLIC_FROZEN");
                }

                logger.info("Assigned all pids, state = {}, id = {}", vc.getState(), vc.getId());

            } catch (VirtualCollectionRegistryException ex) {
                logger.error("Failed to mint PID, setting vc to error state", ex);
                vc.setState(VirtualCollection.State.ERROR);
                vc.setProblemDetails(ex.getMessage());
                if(ex.getCause() instanceof HttpException) {                                                  
                    vc.setProblem(VirtualCollection.Problem.PID_MINTING_HTTP_ERROR);
                } else {
                    vc.setProblem(VirtualCollection.Problem.PID_MINTING_UNKOWN);
                }
                logger.info("Set problem details: {}", vc.getProblemDetails());
            } catch(Exception ex) {
                logger.error("Failed to mint PID, setting vc to error state", ex);
                vc.setState(VirtualCollection.State.ERROR);
                vc.setProblem(VirtualCollection.Problem.UNKOWN);
                vc.setProblemDetails(ex.getMessage());
                logger.info("Set problem details: {}", vc.getProblemDetails());
            }
        } else {
            try {
                switch (vc.getState()) {
                    case PUBLIC_PENDING:
                        vc.setState(VirtualCollection.State.PUBLIC);
                        break;
                    case PUBLIC_FROZEN_PENDING:
                        vc.setState(VirtualCollection.State.PUBLIC_FROZEN);
                        break;
                    default:
                        throw new VirtualCollectionRegistryException("Invalid state transition: " + vc.getState() + " --> PUBLIC or PUBLIC_FROZEN");
                }
            } catch(Exception ex) {
                logger.error("Failed to update collection state", ex);
                vc.setState(VirtualCollection.State.ERROR);
                vc.setProblem(VirtualCollection.Problem.UNKOWN);
                vc.setProblemDetails(ex.getMessage());
                logger.info("Set problem details: {}", vc.getProblemDetails());
            }
        }

        //em.persist(vc); //update collection
        //if(parent != null) {
        //    em.merge(parent);
        //}

        if(vc.hasPersistentIdentifier()) {
            logger.info("assigned pid (identifer='{}') to virtual"
                    + "collection (id={})",
                    vc.getPrimaryIdentifier().getIdentifier(),
                    vc.getId());
        }
    }

    protected void updatePersistentIdentifiers(EntityManager em, final Date nowDateAlloc) {
        TypedQuery<VirtualCollection> q
                = em.createNamedQuery("VirtualCollection.findAllByStates",
                VirtualCollection.class);
        List<VirtualCollection.State> states = new LinkedList<>();
        states.add(VirtualCollection.State.PUBLIC);
        states.add(VirtualCollection.State.PUBLIC_FROZEN);
        q.setParameter("states", states);
        q.setParameter("date", nowDateAlloc);
        q.setLockMode(LockModeType.PESSIMISTIC_WRITE);

        em.getTransaction().begin();
        List<VirtualCollection> collections = q.getResultList();
        em.getTransaction().commit();

        HttpClient client = HttpClientBuilder.create().build();
        for(VirtualCollection vc : collections) {
            if(vc.isPublicLeaf()) {
                for(PersistentIdentifier latestPid : vc.getLatestIdentifiers()) {
                    if(latestPid.getType() != PersistentIdentifier.Type.DUMMY) {
                        try {
                            HttpHead request = new HttpHead(latestPid.getActionableURI());
                            HttpResponse response = client.execute(request);
                            String pidUrl = response.getFirstHeader("Location").getValue();
                            String latestVersionUrl = permaLinkService.getCollectionUrl(vc);

                            if(!pidUrl.equalsIgnoreCase(latestVersionUrl)) {
                                //try {
                                    logger.info("Updating pid = {}, url = {} --> {}", latestPid, pidUrl, latestVersionUrl);
                                    //pidProviderService.updateLatestIdentifierUrl(latestPid, latestVersionUrl);
                                //} catch(VirtualCollectionRegistryException ex) {
                                //    logger.error("Failed to update latest version pid ("+latestPid.getActionableURI()+") url to "+latestVersionUrl);
                                //}
                            }
                        } catch (IOException ex) {
                            logger.info("Http request failed", ex);
                        }
                    }
                }
            }
        }
    }

    /*
     * delayed purging of deleted virtual collections
     *
     * @param em
     * @param nowDatePurge
     */
    protected void purgeDeletedCollections(EntityManager em, final Date nowDatePurge) {
        em.getTransaction().begin();
        TypedQuery<VirtualCollection> q
                = em.createNamedQuery("VirtualCollection.findAllByState", VirtualCollection.class);
        q.setParameter("state", VirtualCollection.State.DELETED);
        q.setParameter("date", nowDatePurge);
        q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        for (VirtualCollection vc : q.getResultList()) {
            vc.setState(VirtualCollection.State.DEAD);
            em.remove(vc);
            logger.debug("purged virtual collection (id={})", vc.getId());
        }
        em.getTransaction().commit();
    }
    
    /*
     * Handle virtualcollections in error
     * 
     * @param em
     * @param nowDateError
     */
    protected void handleCollectionsInError(EntityManager em, final Date nowDateError) {   
        em.getTransaction().begin();
        TypedQuery<VirtualCollection> q
                = em.createNamedQuery("VirtualCollection.findAllByState", VirtualCollection.class);
        q.setParameter("state", VirtualCollection.State.ERROR);
        q.setParameter("date", nowDateError);
        q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        List<VirtualCollection> resultList = q.getResultList();
        if(resultList.size() > 0) {
            logger.debug("Found #{} collections in error state.", resultList.size());
            for (VirtualCollection vc : resultList) {
                VirtualCollection.State currentState = vc.getState();
                logger.trace("Found [{}] in error state.", vc.getName(), currentState);
                //TODO: handle virtual collections in error state
            }
        }
        em.getTransaction().commit();
    }
    
    
}
