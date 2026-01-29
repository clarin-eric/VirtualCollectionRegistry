package eu.clarin.cmdi.virtualcollectionregistry.rest;


import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.rest.utils.RestUtils;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import eu.clarin.cmdi.virtualcollectionregistry.service.impl.VirtualCollectionMarshallerImpl;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Body writer that outputs an XML representation of a virtual collection in the
 * internal representation
 *
 * @author twagoo
 */
@Provider
@Produces({
    MediaType.TEXT_XML,
    MediaType.APPLICATION_XML,
    MediaType.APPLICATION_JSON})
public class VirtualCollectionXMLBodyWriter implements MessageBodyWriter<VirtualCollection> {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionXMLBodyWriter.class);
    
    @Autowired
    private VirtualCollectionMarshaller marshaller;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.equals(VirtualCollection.class);
    }

    @Override
    public long getSize(VirtualCollection t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(VirtualCollection vc, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream stream) throws IOException, WebApplicationException {
        final VirtualCollection.Format format = RestUtils.getOutputFormat(Collections.singletonList(mediaType));
        if(marshaller == null) {
            //TODO: Needed for client based usage where marshaller is not set via spring injection
            try {
                marshaller = new VirtualCollectionMarshallerImpl();
            } catch(VirtualCollectionRegistryException ex) {
                logger.error("Failed to create VirtualCollectionMarshallerImpl", ex);
            }
        }
        marshaller.marshal(stream, format, vc);
    }

}
