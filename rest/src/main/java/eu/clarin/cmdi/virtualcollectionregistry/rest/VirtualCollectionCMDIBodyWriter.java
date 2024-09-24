package eu.clarin.cmdi.virtualcollectionregistry.rest;

//import com.sun.jersey.api.core.InjectParam;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Body writer that outputs a CMDI representation of a virtual collection
 *
 * @author twagoo
 */
@Provider
@Produces(VirtualCollectionResource.MediaTypes.CMDI)
public class VirtualCollectionCMDIBodyWriter implements MessageBodyWriter<VirtualCollection> {

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
        if (!(vc.isPublic() || vc.isPublicFrozen()) || !vc.hasPersistentIdentifier()) {
            throw new WebApplicationException(Response.status(Status.NOT_ACCEPTABLE).entity("CMDI not available for unpublished collections. Please request XML or JSON").build());
        }
        marshaller.marshalAsCMDI(stream, VirtualCollection.Format.XML, vc);
    }

}
