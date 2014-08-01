package eu.clarin.cmdi.virtualcollectionregistry.rest;

import com.sun.jersey.api.container.filter.UriConnegFilter;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;

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
public class VirtualCollectionMediaTypeFilter extends UriConnegFilter {

    private static final Map<String, MediaType> mediaTypes = new HashMap<>();

    static {
        mediaTypes.put("xml", MediaType.APPLICATION_XML_TYPE);
        mediaTypes.put("json", MediaType.APPLICATION_JSON_TYPE);
        mediaTypes.put("html", MediaType.TEXT_HTML_TYPE);
        mediaTypes.put("cmdi", VirtualCollectionResource.MediaTypes.CMDI_TYPE);
    }

    public VirtualCollectionMediaTypeFilter() {
        super(mediaTypes);
    }

}
