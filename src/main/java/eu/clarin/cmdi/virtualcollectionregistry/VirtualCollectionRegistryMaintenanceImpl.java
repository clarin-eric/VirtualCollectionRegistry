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
package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.pid.PersistentIdentifier;
import eu.clarin.cmdi.virtualcollectionregistry.pid.PersistentIdentifierProvider;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import org.apache.commons.httpclient.HttpException;
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
    
    @Autowired
    private DataStore datastore; //TODO: replace with Spring managed EM?
    
    @Autowired
   // @Qualifier("EPICPersistentIdentifierProvider")
    private PersistentIdentifierProvider pid_provider;
    
    @Override
    public void perform(long now) {
        logger.trace("Maintenance check");
        long t1 = System.nanoTime();
        
        // allocate persistent identifier roughly after 30 seconds
        final Date nowDateAlloc = new Date(now - 30 * 1000);
        // (for now) purge deleted collection roughly after 30 seconds
        final Date nowDatePurge = new Date(now - 30 * 1000);
        // handle collections in error immediatly
        final Date nowDateError = new Date(now);

        EntityManager em = datastore.getEntityManager();
        try {
            allocatePersistentIdentifiers(em, nowDateAlloc);
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
        em.getTransaction().begin();
        TypedQuery<VirtualCollection> q
                = em.createNamedQuery("VirtualCollection.findAllByStates",
                        VirtualCollection.class);
        List<VirtualCollection.State> states = new LinkedList<>();
        states.add(VirtualCollection.State.PUBLIC_PENDING);
        states.add(VirtualCollection.State.PUBLIC_FROZEN_PENDING);
        q.setParameter("states", states);
        q.setParameter("date", nowDateAlloc);
        q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        for (VirtualCollection vc : q.getResultList()) {
            allocatePersistentIdentifier(em, vc); 
        }
        em.getTransaction().commit();
    }   
    
    /**
     * Assign a PID for this VirtualCollection.
     * 
     * @param em
     * @param vc 
     */
    protected void allocatePersistentIdentifier(EntityManager em, VirtualCollection vc) {
        VirtualCollection.State currentState = vc.getState();
        logger.info("Found {} with state {}", vc.getName(), currentState);

        if(!vc.hasPersistentIdentifier()) {
            try {
                PersistentIdentifier pid = pid_provider.createIdentifier(vc);
                vc.setPersistentIdentifier(pid);
            } catch (VirtualCollectionRegistryException ex) {
                logger.error("Failed to mint PID, setting vc to error state");
                vc.setState(VirtualCollection.State.ERROR);
                if(ex.getCause() instanceof HttpException) {                                                  
                    vc.setProblem(VirtualCollection.Problem.PID_MINTING_UNKOWN);
                } else {
                    vc.setProblem(VirtualCollection.Problem.PID_MINTING_UNKOWN);
                }
            }
        }

        em.persist(vc);
        if(vc.hasPersistentIdentifier()) {
            logger.info("assigned pid (identifer='{}') to virtual"
                    + "collection (id={})",
                    vc.getPersistentIdentifier().getIdentifier(),
                    vc.getId());
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
        for (VirtualCollection vc : q.getResultList()) {
            VirtualCollection.State currentState = vc.getState();
            logger.info("Found [{}] in error state.", vc.getName(), currentState);
            //TODO: handle virtual collections in error state
        }
        em.getTransaction().commit();
    }
    
    
}
