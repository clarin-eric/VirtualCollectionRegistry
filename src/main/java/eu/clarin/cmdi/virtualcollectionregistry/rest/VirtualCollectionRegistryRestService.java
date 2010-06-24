package eu.clarin.cmdi.virtualcollectionregistry.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionNotFoundException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryMarshaller.Format;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;

@Path("/")
public class VirtualCollectionRegistryRestService {
    private final VirtualCollectionRegistry registry =
        VirtualCollectionRegistry.instance();
    @Context
    SecurityContext security;
    @Context
    UriInfo uriInfo;
    @Context
    HttpHeaders headers;

    @POST
    @Path("/virtualcollection")
    @Consumes({ MediaType.TEXT_XML,
                MediaType.TEXT_XML,
                MediaType.APPLICATION_JSON })
    @Produces({ MediaType.TEXT_XML,
                MediaType.APPLICATION_XML,
                MediaType.APPLICATION_JSON })
    public Response createVirtualCollection(InputStream input)
            throws VirtualCollectionRegistryException {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            String path = uriInfo.getPath();
            if (path.endsWith("/")) {
                /*
                 * fix bad client request and remove tailing slash
                 */
                path = path.substring(0, path.length() - 1);
                URI uri = uriInfo.getBaseUriBuilder().path(path).build();
                return Response.temporaryRedirect(uri).build();
            }
            /*
             * should never happen, because servlet container should supply a
             * valid principal
             */
            throw new AssertionError("principal == null");
        }
        try {
            Format format = getInputFormat();
            String encoding = getInputEncoding();
            VirtualCollection vc =
                registry.getMarshaller().unmarshal(input, format, encoding);
            long id = registry.createVirtualCollection(principal, vc);
            RestResponse response = new RestResponse();
            response.setIsSuccess(true);
            response.setInfo("created");
            response.setId(id);
            URI uri =
                uriInfo.getRequestUriBuilder().path(Long.toString(id)).build();
            return Response.created(uri).entity(response).build();
        } catch (IOException e) {
            throw new VirtualCollectionRegistryException("create", e);
        }
    }

    @GET
    @Path("/virtualcollection/{id}")
    @Produces({ MediaType.TEXT_XML,
                MediaType.APPLICATION_XML,
                MediaType.APPLICATION_JSON })
    public Response getVirtualCollection(@PathParam("id") long id)
            throws VirtualCollectionRegistryException {
        final VirtualCollection vc = registry.retrieveVirtualCollection(id);
        StreamingOutput writer = new StreamingOutput() {
            public void write(OutputStream stream) throws IOException,
                    WebApplicationException {
                Format format = getOutputFormat();
                registry.getMarshaller().marshal(stream, format, vc);
            }
        };
        return Response.ok(writer).build();
    }

    @PUT
    @Path("/virtualcollection/{id}")
    @Consumes({ MediaType.TEXT_XML,
                MediaType.TEXT_XML,
                MediaType.APPLICATION_JSON })
    @Produces({ MediaType.TEXT_XML,
                MediaType.APPLICATION_XML,
                MediaType.APPLICATION_JSON })
    public Response updateVirtualCollection(@PathParam("id") long id,
            InputStream input) throws VirtualCollectionRegistryException {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            throw new NullPointerException("princial == null");
        }
        try {
            Format format = getInputFormat();
            String encoding = getInputEncoding();
            VirtualCollection vc =
                registry.getMarshaller().unmarshal(input, format, encoding);
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
    @Path("/virtualcollection/{id}")
    @Produces({ MediaType.TEXT_XML,
                MediaType.APPLICATION_XML,
                MediaType.APPLICATION_JSON })
    public Response deleteVirtualCollection(@PathParam("id") long id)
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
    @Path("/virtualcollection/{id}/state")
    @Produces({ MediaType.TEXT_XML,
                MediaType.APPLICATION_XML,
                MediaType.APPLICATION_JSON })
    public Response getVirtualCollectionState(@PathParam("id") long id)
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
    @Path("/virtualcollection/{id}/state")
    @Consumes({ MediaType.TEXT_XML,
                MediaType.TEXT_XML,
                MediaType.APPLICATION_JSON })
    @Produces({ MediaType.TEXT_XML,
                MediaType.APPLICATION_XML,
                MediaType.APPLICATION_JSON })
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

    @GET
    @Path("/virtualcollections")
    @Produces({ MediaType.TEXT_XML,
                MediaType.APPLICATION_XML,
                MediaType.APPLICATION_JSON })
    public Response getVirtualCollections(@QueryParam("q") String query,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("-1") @QueryParam("count") int count)
            throws VirtualCollectionRegistryException {
        final VirtualCollectionList vcs = registry.getVirtualCollections(query,
                (offset > 0) ? offset : 0, count);
        StreamingOutput writer = new StreamingOutput() {
            public void write(OutputStream stream) throws IOException,
                    WebApplicationException {
                Format format = getOutputFormat();
                registry.getMarshaller().marshal(stream, format, vcs);
            }
        };
        return Response.ok(writer).build();
    }

    @GET
    @Path("/my-virtualcollections")
    @Produces({ MediaType.TEXT_XML,
                MediaType.APPLICATION_XML,
                MediaType.APPLICATION_JSON })
    public Response getMyVirtualCollections(@QueryParam("q") String query,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("-1") @QueryParam("count") int count)
            throws VirtualCollectionRegistryException {
        Principal principal = security.getUserPrincipal();
        if (principal == null) {
            String path = uriInfo.getPath();
            if (path.endsWith("/")) {
                /*
                 * fix bad client request and remove tailing slash
                 */
                path = path.substring(0, path.length() - 1);
                URI uri = uriInfo.getBaseUriBuilder().path(path).build();
                return Response.seeOther(uri).build();
            }
            /*
             * should never happen, because servlet container should supply a
             * valid principal
             */
            throw new AssertionError("principal == null");
        }
        final VirtualCollectionList vcs = registry.getVirtualCollections(
                principal, query, (offset > 0) ? offset : 0, count);
        StreamingOutput writer = new StreamingOutput() {
            public void write(OutputStream stream) throws IOException,
                    WebApplicationException {
                Format format = getOutputFormat();
                registry.getMarshaller().marshal(stream, format, vcs);
            }
        };
        return Response.ok(writer).build();
    }

    @GET
    @Path("/clarin-virtualcollection/{id}")
    @Produces({ MediaType.TEXT_XML,
                MediaType.APPLICATION_XML })
    public Response getClarinVirtualCollection(@PathParam("id") long id)
            throws VirtualCollectionRegistryException {
        final VirtualCollection vc = registry.retrieveVirtualCollection(id);
        if (!vc.isPublic() || (vc.getPersistentIdentifier() == null)) {
            throw new VirtualCollectionNotFoundException(id);
        }
        StreamingOutput writer = new StreamingOutput() {
            public void write(OutputStream output) throws IOException,
                    WebApplicationException {
                registry.getMarshaller().marshalAsCMDI(output, Format.XML, vc);
            }
        };
        return Response.ok(writer).build();
    }

    private Format getInputFormat() {
        Format format = getMediaType(headers.getMediaType());
        return (format != null) ? format : Format.UNSUPPORTED;
    }

    private String getInputEncoding() {
        String encoding =
            headers.getMediaType().getParameters().get("encoding");
        return (encoding != null) ? encoding : "utf-8";
    }

    private Format getOutputFormat() {
        for (MediaType type : headers.getAcceptableMediaTypes()) {
            Format format = getMediaType(type);
            if (format != null) {
                return format;
            }
        }
        return Format.UNSUPPORTED;
    }

    private static Format getMediaType(MediaType type) {
        if (type.isCompatible(MediaType.APPLICATION_XML_TYPE) ||
            type.isCompatible(MediaType.TEXT_XML_TYPE)) {
            return Format.XML;
        }
        if (type.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            return Format.JSON;
        }
        return null;
    }

} // class VirtualCollectionRegistryRestService
