package eu.clarin.cmdi.virtualcollectionregistry.rest;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionMarshaller;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionNotFoundException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author twagoo
 */
public class VirtualCollectionResource {

    private final VirtualCollectionRegistry registry
            = VirtualCollectionRegistry.instance();

    private final long id;
    private final SecurityContext security;
    private final HttpHeaders headers;

    public VirtualCollectionResource(long id, SecurityContext security, HttpHeaders headers) {
        this.id = id;
        this.security = security;
        this.headers = headers;
    }

    @GET
    @Produces({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response getVirtualCollection()
            throws VirtualCollectionRegistryException {
        final VirtualCollection vc = registry.retrieveVirtualCollection(id);
        // XXX: what about non-public VCs?
        StreamingOutput writer = new StreamingOutput() {
            public void write(OutputStream output) throws IOException,
                    WebApplicationException {
                final VirtualCollectionMarshaller.Format format = RestUtils.getOutputFormat(headers);
                registry.getMarshaller().marshal(output, format, vc);
                output.close();
            }
        };
        return Response.ok(writer).build();
    }

    @PUT
    @Consumes({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response updateVirtualCollection(InputStream input) throws VirtualCollectionRegistryException {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            throw new NullPointerException("princial == null");
        }
        try {
            VirtualCollectionMarshaller.Format format = RestUtils.getInputFormat(headers);
            String encoding = RestUtils.getInputEncoding(headers);
            VirtualCollection vc
                    = registry.getMarshaller().unmarshal(input, format, encoding);
            registry.updateVirtualCollection(principal, id, vc);
            RestResponse response = new RestResponse();
            response.setIsSuccess(true);
            response.setInfo("updated");
            response.setId(id);
            return Response.ok(response).build();
        } catch (IOException e) {
            throw new VirtualCollectionRegistryException("update", e);

        }
    }

    @DELETE
    @Produces({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response deleteVirtualCollection()
            throws VirtualCollectionRegistryException {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            throw new NullPointerException("principal == null");
        }
        registry.deleteVirtualCollection(principal, id);
        RestResponse response = new RestResponse();
        response.setIsSuccess(true);
        response.setInfo("deleted");
        response.setId(id);
        return Response.ok(response).build();
    }

    @GET
    @Path("/cmdi")
    @Produces({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML})
    public Response getClarinVirtualCollection()
            throws VirtualCollectionRegistryException {
        final VirtualCollection vc = registry.retrieveVirtualCollection(id);
        if (!vc.isPublic() || (vc.getPersistentIdentifier() == null)) {
            throw new VirtualCollectionNotFoundException(id);
        }
        StreamingOutput writer = new StreamingOutput() {
            public void write(OutputStream output) throws IOException,
                    WebApplicationException {
                registry.getMarshaller().marshalAsCMDI(output, VirtualCollectionMarshaller.Format.XML, vc);
                output.close();
            }
        };
        return Response.ok(writer).build();
    }

    @GET
    @Path("/state")
    @Produces({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response getVirtualCollectionState()
            throws VirtualCollectionRegistryException {
        VirtualCollection.State state = registry.getVirtualCollectionState(id);
        State result = null;
        switch (state) {
            case PUBLIC_PENDING:
            /* FALL-THROUGH */
            case PUBLIC:
                result = State.PUBLIC;
                break;
            default:
                result = State.PRIVATE;
        } // switch
        return Response.ok(result).build();
    }

    @POST
    @Path("/state")
    @Consumes({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response setVirtualCollectionState(@PathParam("id") long id,
            State state)
            throws VirtualCollectionRegistryException {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            throw new NullPointerException("principal == null");
        }
        if (state == null) {
            throw new VirtualCollectionRegistryUsageException("invalid state");
        }
        VirtualCollection.State vc_state = null;
        switch (state) {
            case PUBLIC:
                vc_state = VirtualCollection.State.PUBLIC_PENDING;
                break;
            case PRIVATE:
                vc_state = VirtualCollection.State.PRIVATE;
                break;
            default:
                throw new VirtualCollectionRegistryUsageException("invalid state");
        }
        registry.setVirtualCollectionState(principal, id, vc_state);
        RestResponse response = new RestResponse();
        response.setIsSuccess(true);
        response.setInfo("updated state to '" + state + "'");
        response.setId(id);
        return Response.ok(response).build();
    }
}
