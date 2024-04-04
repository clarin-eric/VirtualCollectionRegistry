package eu.clarin.cmdi.virtualcollectionregistry.core.rest;

//import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection.Format;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.glassfish.jersey.message.internal.AcceptableMediaType;
import org.glassfish.jersey.message.internal.HttpHeaderReader;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * REST input/ouput utilities for the Virtual Collection Registry REST resources
 *
 * @author twagoo
 */
public final class RestUtils {

    private final static Logger logger = LoggerFactory.getLogger(RestUtils.class);

    private static final List<MediaType> WILDCARD_ACCEPTABLE_TYPE_SINGLETON_LIST;

    static {
        WILDCARD_ACCEPTABLE_TYPE_SINGLETON_LIST = Collections.singletonList(MediaTypes.WILDCARD_ACCEPTABLE_TYPE);
    }

    public static class ClarinMediaType extends MediaType {
        //CMDI mimetype: https://www.clarin.eu/faq/what-mimetype-should-i-use-cmdi-files
        public static final String APPLICATION_CMDI_XML = "application/x-cmdi+xml";
        public static final MediaType APPLICATION_CMDI_XML_TYPE = new MediaType("application", "x-cmdi+xml");
    }

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
        Format format = getMediaType(headers.getMediaType(), true);
        return (format != null) ? format : Format.UNSUPPORTED;
    }

    public static Format getOutputFormatNoWildcard(List<MediaType> mediaTypes) {
        for (MediaType type : mediaTypes) {
            Format format = getMediaType(type, false);
            if (format != null) {
                return format;
            }
        }
        return Format.UNSUPPORTED;
    }

    /**
     *
     * @param mediaTypes
     * @return the first accepted output format specified in the headers that
     * can be normalised to {@link Format#XML} or {@link Format#JSON}, or
     * defaults to {@link Format#UNSUPPORTED} if none is specified in the
     * headers
     * @see HttpHeaders#getAcceptableMediaTypes()
     */
    public static Format getOutputFormat(List<MediaType> mediaTypes) {
        for (MediaType type : mediaTypes) {
            Format format = getMediaType(type, true);
            if (format != null) {
                return format;
            }
        }
        return Format.UNSUPPORTED;
    }

    private static Format getMediaType(MediaType type, boolean includeWildcard) {
        if(!includeWildcard && type.isWildcardType()) {
            return null;
        }

        if (type.isCompatible(MediaType.APPLICATION_XML_TYPE)
                || type.isCompatible(MediaType.TEXT_XML_TYPE)
                || type.isCompatible(ClarinMediaType.APPLICATION_CMDI_XML_TYPE)) {
            return Format.XML;
        }
        if (type.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            return Format.JSON;
        }

        return null;
    }

    /**
     * Do not redirect if HTML or wildcard media type is accepted.
     * Otherwise redirect to redirectLocation if application/xml or application/json is accepted.
     *
     * @param req
     * @param redirectLocation
     */
    public static void checkRestApiRedirection(HttpServletRequest req, String redirectLocation) {
        List<MediaType> acceptableMediaTypes = getAcceptableMediaTypes(req);
        if(!acceptsHtml(acceptableMediaTypes)) {
            Format outputFormat = RestUtils.getOutputFormatNoWildcard(acceptableMediaTypes);
            logger.info("Requested format=" + outputFormat.name());
            if (outputFormat == Format.JSON || outputFormat == Format.XML) {
                logger.info("Redirecting to " + redirectLocation);
                throw new RedirectToUrlException(redirectLocation);
            }
        }
    }

    public static boolean acceptsHtml(List<MediaType> mediaTypes) {
        for (MediaType type : mediaTypes) {
            if(type.isCompatible(MediaType.TEXT_HTML_TYPE) || type.isWildcardType()) {
                return true;
            }
        }
        return false;
    }

    public static List<MediaType> getAcceptableMediaTypes(HttpServletRequest req) {
        List<Object> values = new LinkedList<>();

        //HttpServletRequest req =
        Enumeration valuesEnum = req.getHeaders("Accept");
        while(valuesEnum.hasMoreElements()) {
            values.add(valuesEnum.nextElement());
        }

        if (values != null && !values.isEmpty()) {
            List<MediaType> result = new ArrayList(values.size());
            Iterator it = values.iterator();
            while(it.hasNext()) {
                Object value = it.next();
                try {
                    if (value instanceof MediaType) {
                        AcceptableMediaType _value = AcceptableMediaType.valueOf((MediaType)value);
                        result.add(_value);
                    } else {
                        result.addAll(HttpHeaderReader.readAcceptMediaType(String.valueOf(value)));
                    }
                } catch (ParseException ex) {
                    logger.warn("Failed to parse media type from header.", ex);
                }
            }
            return Collections.unmodifiableList(result);
        } else {
            return WILDCARD_ACCEPTABLE_TYPE_SINGLETON_LIST;
        }
    }
}
