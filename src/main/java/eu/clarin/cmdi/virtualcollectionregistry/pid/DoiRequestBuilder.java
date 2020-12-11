package eu.clarin.cmdi.virtualcollectionregistry.pid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

import java.util.LinkedList;

public class DoiRequestBuilder {

    public static DoiRequest createGenerateDoiRequest(String prefix, VirtualCollection vc, String baseUri) {
        DoiRequest request = new DoiRequest();
        request.setPrefix(prefix);
        request.setUrl(EPICPersistentIdentifierProvider.makeCollectionURI(vc, baseUri));
        request.setPublicationYear(vc.getYear());
        request.addTitle(vc.getTitle());
        for(Creator vc_creator: vc.getCreators()) {
            if(vc_creator.getOrganisation() != null && !vc_creator.getOrganisation().isEmpty()) {
                request.addCreator(vc_creator.getFamilyName(), vc_creator.getGivenName(), vc_creator.getOrganisation());
            } else {
                request.addCreator(vc_creator.getFamilyName(), vc_creator.getGivenName());
            }
        }
        return request;
    }
}
