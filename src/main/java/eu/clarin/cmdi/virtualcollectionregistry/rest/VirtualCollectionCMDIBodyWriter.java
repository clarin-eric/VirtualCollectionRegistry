package eu.clarin.cmdi.virtualcollectionregistry.rest;

import com.sun.jersey.api.core.InjectParam;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Body writer that outputs a CMDI representation of a virtual collection
 *
 * @author twagoo
 */
@Provider
@Produces(VirtualCollectionResource.MediaTypes.CMDI)
public class VirtualCollectionCMDIBodyWriter implements MessageBodyWriter<VirtualCollection> {

    @InjectParam
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
        if (!vc.isPublic() || (!vc.isPublicFrozen()) || (!vc.hasPersistentIdentifier())) {
            throw new WebApplicationException(Response.status(Status.NOT_ACCEPTABLE).entity("CMDI not available for unpublished profiles. Please request XML or JSON").build());
        }
        marshaller.marshalAsCMDI(stream, VirtualCollectionMarshaller.Format.XML, vc);
    }

}
