package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

import javax.persistence.EntityManager;
import java.util.List;

public interface VirtualCollectionDao {

    int getVirtualCollectionCount(EntityManager em, QueryFactory factory) throws VirtualCollectionRegistryException;
    List<VirtualCollection> getVirtualCollections(EntityManager em, int first, int count, QueryFactory factory);
    List<VirtualCollection> getVirtualCollections(EntityManager em, QueryFactory factory);

}
