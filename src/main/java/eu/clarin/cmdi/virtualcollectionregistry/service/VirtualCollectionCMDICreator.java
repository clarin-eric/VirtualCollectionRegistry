package eu.clarin.cmdi.virtualcollectionregistry.service;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD;

/**
 * A service that converts virtual collection instances to CMDI
 * object hierarchies
 *
 * @author twagoo
 */
public interface VirtualCollectionCMDICreator {

    /**
     *
     * @param vc collection to generate a CMDI hierarchy for
     * @return a CMDI hierarchy representing the provided collection
     */
    CMD createMetadataStructure(VirtualCollection vc);

    /**
     *
     * @return the location of the XSD that specifies the schema of the profile
     * on which the object hierarchy returned by {@link #createMetadataStructure(eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection)
     * } is based
     */
    String getSchemaLocation();

}
