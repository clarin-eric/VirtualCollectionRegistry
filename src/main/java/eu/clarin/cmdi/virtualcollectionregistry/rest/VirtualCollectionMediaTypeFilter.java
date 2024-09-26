package eu.clarin.cmdi.virtualcollectionregistry.rest;

import java.util.HashMap;
import java.util.Map;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

/**
 * Extension of the Jersey filter that allows "content negotation" by means of
 * URL extensions, {
 *
 * @lit e.g.} /service/virtualcollections/7.cmdi will call
 * /service/virtualcollections/7 with content type application/x-cmdi+xml
 *
 * Based on description at {@link http://stackoverflow.com/a/8765749}
 * @author twagoo
 */
@Provider //Allow auto discovery, see: https://stackoverflow.com/questions/17300218/jersey-containerrequestfilter-not-triggered
public class VirtualCollectionMediaTypeFilter extends CustomUriConnegFilter {

    private static final Map<String, MediaType> mediaTypes = new HashMap<>();

    static {
        mediaTypes.put("xml", MediaType.APPLICATION_XML_TYPE);
        mediaTypes.put("json", MediaType.APPLICATION_JSON_TYPE);
        mediaTypes.put("html", MediaType.TEXT_HTML_TYPE);
        mediaTypes.put("cmdi", VirtualCollectionResource.MediaTypes.CMDI_TYPE);
    }

    public VirtualCollectionMediaTypeFilter() {
        super(mediaTypes, null);
    }
}
    

