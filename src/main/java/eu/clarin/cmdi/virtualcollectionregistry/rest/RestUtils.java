package eu.clarin.cmdi.virtualcollectionregistry.rest;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionMarshaller.Format;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author twagoo
 */
public final class RestUtils {

    public static String getInputEncoding(HttpHeaders headers) {
        String encoding
                = headers.getMediaType().getParameters().get("encoding");
        return (encoding != null) ? encoding : "utf-8";
    }

    public static Format getInputFormat(HttpHeaders headers) {
        Format format = getMediaType(headers.getMediaType());
        return (format != null) ? format : Format.UNSUPPORTED;
    }

    public static Format getOutputFormat(HttpHeaders headers) {
        for (MediaType type : headers.getAcceptableMediaTypes()) {
            Format format = getMediaType(type);
            if (format != null) {
                return format;
            }
        }
        return Format.UNSUPPORTED;
    }

    private static Format getMediaType(MediaType type) {
        if (type.isCompatible(MediaType.APPLICATION_XML_TYPE)
                || type.isCompatible(MediaType.TEXT_XML_TYPE)) {
            return Format.XML;
        }
        if (type.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            return Format.JSON;
        }
        return null;
    }
}
