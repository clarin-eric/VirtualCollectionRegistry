/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.persistence;

import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionDao;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollectionFactory;

/**
 *
 * @author wilelb
 */
public class VirtualCollectionFactoryWithPersistence {
    
    private final VirtualCollectionFactory f;
    
    public VirtualCollectionFactoryWithPersistence(VirtualCollectionFactory f) {
        this.f = f;
    }
    
    public VirtualCollectionFactoryWithPersistence persist(VirtualCollectionDao dao) throws VirtualCollectionRegistryException {
        dao.persist(f.getCollection());
        f.setDirty(false);
        return this;
    }

    public VirtualCollection getPersistedCollection(VirtualCollectionDao dao) throws VirtualCollectionRegistryException  {
        if(f.isDirty()) {
            persist(dao);
        }
        return f.getCollection();
    }
    
    public VirtualCollectionFactoryWithPersistence startPublish(VirtualCollectionDao dao) throws VirtualCollectionRegistryException {
        f.setState(VirtualCollection.State.PUBLIC_PENDING);
        persist(dao);
        return this;
    }

    public VirtualCollectionFactoryWithPersistence finishPublish(VirtualCollectionDao dao) throws VirtualCollectionRegistryException {
        f.setState(VirtualCollection.State.PUBLIC);
        f.setPublicLeaf(true);
        if(f.getParent() != null) {
            f.getParent().setPublicLeaf(false);
        }
        persist(dao);
        return this;
    }
    
    public VirtualCollectionFactory getFactory() {
        return f;
    }
    
    public VirtualCollectionFactoryWithPersistence setState(VirtualCollection.State state) {
        f.setState(state);
        return this;
    }
}
