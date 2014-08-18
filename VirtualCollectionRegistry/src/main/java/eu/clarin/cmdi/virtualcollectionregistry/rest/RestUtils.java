package eu.clarin.cmdi.virtualcollectionregistry.rest;

import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller.Format;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * REST input/ouput utilities for the Virtual Collection Registry REST resources
 *
 * @author twagoo
 */
public final class RestUtils {

    /**
     *
     * @param headers
     * @return the encoding specified in the headers, defaults to "utf-8" if
     * none is specified
     * @see HttpHeaders#getMediaType()
     */
    public static String getInputEncoding(HttpHeaders headers) {
        String encoding
                = headers.getMediaType().getParameters().get("encoding");
        return (encoding != null) ? encoding : "utf-8";
    }

    /**
     *
     * @param headers
     * @return the input format specified in the headers, normalises to
     * {@link Format#XML} or {@link Format#JSON}, or defaults to
     * {@link Format#UNSUPPORTED} if none is specified in the headers
     * @see HttpHeaders#getMediaType()
     */
    public static Format getInputFormat(HttpHeaders headers) {
        Format format = getMediaType(headers.getMediaType());
        return (format != null) ? format : Format.UNSUPPORTED;
    }

    /**
     *
     * @param headers
     * @return the first accepted output format specified in the headers that
     * can be normalised to {@link Format#XML} or {@link Format#JSON}, or
     * defaults to {@link Format#UNSUPPORTED} if none is specified in the
     * headers
     * @see HttpHeaders#getAcceptableMediaTypes()
     */
    public static Format getOutputFormat(List<MediaType> mediaTypes) {
        for (MediaType type : mediaTypes) {
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
