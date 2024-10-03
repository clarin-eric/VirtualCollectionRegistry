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

import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.impl.ReferenceValidator;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author wilelb
 */
@Component
public class VirtualCollectionRegistryReferenceCheckImpl implements VirtualCollectionRegistryReferenceCheck{
    private static final Logger logger
            = LoggerFactory.getLogger(VirtualCollectionRegistryReferenceCheckImpl.class);
    
    private final static String name = "Reference Link validity check";
    
    @Autowired
    private DataStore datastore; //TODO: replace with Spring managed EM?
    
    private final ReferenceValidator validator = new ReferenceValidator();
    
    @Override
    public void perform(long now) {
        logger.debug("{}", name);        
        EntityManager em = datastore.getEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<VirtualCollection> q
                    = em.createNamedQuery("VirtualCollection.findAllPublic", VirtualCollection.class);
            q.setLockMode(LockModeType.PESSIMISTIC_WRITE);
            for (VirtualCollection vc : q.getResultList()) {                
                checkValidityOfReferences(vc);                 
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            logger.error("unexpected error while doing "+name, e);
        } finally {
            datastore.closeEntityManager();
        }
    }
    
    /**
     * Check all resources of this vc against the reference validator.
     * Log an error message if validation fails.
     * 
     * @param vc
     * @return 
     */
    private ReferenceValidator checkValidityOfReferences(VirtualCollection vc) {
        logger.debug("Validing references for collection with id = {}", vc.getId());
        
        for(Resource resource : vc.getResources()) {
            final Validatable<String> validatable = 
                    new Validatable<>(resource.getRef());
            validator.validate(validatable);
            if(!validatable.isValid()) {
                for(IValidationError error : validatable.getErrors()) {
                    //TODO: Does this actually print anything meaningful?
                    logger.error("[RESOURCE ERROR] vc={}, error={}", vc.getId(), error.toString());
                }
            } else {
                logger.debug("Successfully validated reference id={}, url={} for collection with id = {}",resource.getId(), resource.getRef(), vc.getId());
            }
        }
        return validator;
    }
}
