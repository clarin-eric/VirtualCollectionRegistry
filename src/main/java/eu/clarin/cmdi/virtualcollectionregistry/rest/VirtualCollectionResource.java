package eu.clarin.cmdi.virtualcollectionregistry.rest;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Variant;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * REST resource representing an individual virtual collection.
 *
 * This was designed to act as a managed subresource of
 * {@link VirtualCollectionsResource}. The user of this class is responsible for
 * calling {@link #setId(long) } before handing it over or doing anything else
 * with it.
 *
 * @author twagoo
 */
public final class VirtualCollectionResource {

    public static class MediaTypes {

        public static final String CMDI = "application/x-cmdi+xml";
        public static final MediaType CMDI_TYPE = new MediaType("application", "x-cmdi+xml");
    }

    @Autowired
    private VirtualCollectionRegistry registry;
    @Autowired
    private VirtualCollectionMarshaller marshaller;
    @Context
    private SecurityContext security;
    @Context
    private HttpHeaders headers;
    @Context
    private UriInfo uriInfo;

    private Long id;

    /**
     * Default constructor needed so that it can act as a managed subresource of
     * {@link VirtualCollectionsResource}, remember to
     * {@link #setId(long) set the id} after construction!
     */
    public VirtualCollectionResource() {
    }

    // for testing
    protected VirtualCollectionResource(VirtualCollectionRegistry registry, VirtualCollectionMarshaller marshaller, SecurityContext security, HttpHeaders headers, UriInfo uriInfo, Long id) {
        this.registry = registry;
        this.marshaller = marshaller;
        this.security = security;
        this.headers = headers;
        this.uriInfo = uriInfo;
        this.id = id;
    }

    
    
    /**
     * Sets the id for this resource; should be called exactly once per
     * instance; <strong>mandatory call</strong>, not setting will lead to
     * NullPointerExceptions
     *
     * @param id
     */
    public synchronized void setId(long id) {
        if (this.id != null) {
            throw new IllegalStateException("Id was already set for Virtual Collection resource! Resource is recycled (by Jersey)?");
        }
        this.id = id;
    }

    /**
     * The virtual collection referenced by the URI will be retrieved
     *
     * @param request request object, to be injected by JAX-RS context
     * @return A response containing a representation of the requested Virtual
     * Collection. If the virtual collection is not found the appropriate HTTP
     * status code is issued and an error message is returned.
     * @throws VirtualCollectionRegistryException
     */
    @GET
    @Produces({VirtualCollectionResource.MediaTypes.CMDI,
        MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response getVirtualCollection(@Context Request request)
            throws VirtualCollectionRegistryException {
        final VirtualCollection vc = registry.retrieveVirtualCollection(id);
        // CMDI's should not be returned for non-public VC's, so check this...
        if (!(vc.isPublic() || vc.isPublicFrozen()) || (!vc.hasPersistentIdentifier())) {
            // exclude CMDI from the options and check if this is ok for request
            final List<Variant> variants = Variant.mediaTypes(
                    MediaType.TEXT_XML_TYPE,
                    MediaType.APPLICATION_XML_TYPE,
                    MediaType.APPLICATION_JSON_TYPE).add().build();
            final Variant selectVariant = request.selectVariant(variants);
            if (selectVariant != null) {
                // alternative option is accepted, return this
                return Response.ok(vc, selectVariant).build();
            }
            // else proceed anyway, will probably fail on writing CMDI body
        }
        return Response.ok(vc).build();
    }

    /**
     * Redirects the client to the VC's details page in the Wicket frontend
     *
     * @return
     * @throws VirtualCollectionRegistryException
     */
    @GET
    @Produces({MediaType.TEXT_HTML})
    public Response getVirtualCollectionDetailsRedirect()
            throws VirtualCollectionRegistryException {
        final UriBuilder pathBuilder = uriInfo.getBaseUriBuilder().path("../details/{arg1}");
        final URI detailsUri = pathBuilder.build(id);
        return Response.seeOther(detailsUri).build();
    }

    /**
     * The virtual collection identified by the URI will be updated, actually
     * replaced, with the representation of the virtual collection sent in the
     * request body.
     *
     * @param input Depending on Content-Type header either a valid XML instance
     * or the JSON representation of a virtual collection conforming to the
     * above mentioned XML schema. The root element is expected to be
     * "VirtualCollection"
     * @return A response containing a {@link RestResponse}
     * @throws VirtualCollectionRegistryException
     */
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
                    = marshaller.unmarshal(input, format, encoding);
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

    /**
     * The virtual collection referenced by the URI will be deleted.
     *
     * @return A response containing a {@link RestResponse}
     * @throws VirtualCollectionRegistryException
     */
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

    /**
     * The publication state of the virtual collection referenced by the URI
     *
     * @return a response containing the {@link State} of the identified Virtual
     * Collection
     * @throws VirtualCollectionRegistryException
     */
    @GET
    @Path("/state")
    @Produces({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response getVirtualCollectionState()
            throws VirtualCollectionRegistryException {
        VirtualCollection.State state = registry.getVirtualCollectionState(id);
        final State result;
        switch (state) {
            case PUBLIC_PENDING:
            /* FALL-THROUGH */
            case PUBLIC:
                result = State.PUBLIC;
                break;
            case PUBLIC_FROZEN_PENDING:
            case PUBLIC_FROZEN:
                result = State.PUBLIC_FROZEN;
                break;
            default:
                result = State.PRIVATE;
        } // switch
        return Response.ok(result).build();
    }

    /**
     * Updates the publication state of the virtual collection referenced by the
     * URI
     *
     * @param id
     * @param state
     * @return a response containg a {@link RestResponse}
     * @throws VirtualCollectionRegistryException
     */
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
            case PUBLIC_FROZEN:
                vc_state = VirtualCollection.State.PUBLIC_FROZEN_PENDING;
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
