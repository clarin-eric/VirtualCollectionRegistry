package eu.clarin.cmdi.virtualcollectionregistry.rest;

import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.api.core.ResourceContext;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller.Format;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

/**
 * REST resource representing the collection of virtual collections
 *
 * @author twagoo
 */
@Path("/virtualcollections")
public class VirtualCollectionsResource {

    @Context
    private ResourceContext resourceContext;
    @InjectParam
    private VirtualCollectionRegistry registry;    
    @InjectParam
    private VirtualCollectionMarshaller marshaller;
    @Context
    private SecurityContext security;
    @Context
    private UriInfo uriInfo;
    @Context
    private HttpHeaders headers;

    /**
     * All virtual collections will be retrieved; if a query expression is used,
     * only the virtual collections satisfying the query will be retrieved.
     *
     * @param query a Virtual Collection Query Language expression
     * @param offset
     * @param count
     * @return a serialised list of all public virtual collections. If no
     * virtual collection are found an empty list will be returned.
     * @throws VirtualCollectionRegistryException if the collections could not
     * be retrieved
     * @see VirtualCollectionRegistry#getVirtualCollections(java.lang.String,
     * int, int)
     */
    @GET
    @Produces({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    public Response getVirtualCollections(@QueryParam("q") String query,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("-1") @QueryParam("count") int count)
            throws VirtualCollectionRegistryException {
        final VirtualCollectionList vcs = registry.getVirtualCollections(query,
                (offset > 0) ? offset : 0, count);
        StreamingOutput writer = new StreamingOutput() {
            public void write(OutputStream output) throws IOException,
                    WebApplicationException {
                final Format format = RestUtils.getOutputFormat(headers);
                marshaller.marshal(output, format, vcs);
                output.close();
            }
        };
        return Response.ok(writer).build();
    }

    /**
     * A virtual collection will be created based on the representation of the
     * virtual collection sent in the request body.
     *
     * @param input Depending on Content-Type header either a valid XML instance
     * or the JSON representation of a virtual collection conforming to the
     * above mentioned XML schema. The root element is expected to be
     * "VirtualCollection"
     * @return A response containing a {@link RestResponse}. If successful, the
     * result will contain the ID of the created virtual collection
     *
     * @throws VirtualCollectionRegistryException
     */
    @POST
    @Consumes({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
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
            VirtualCollectionMarshaller.Format format = RestUtils.getInputFormat(headers);
            String encoding = RestUtils.getInputEncoding(headers);
            VirtualCollection vc
                    = marshaller.unmarshal(input, format, encoding);
            long id = registry.createVirtualCollection(principal, vc);
            RestResponse response = new RestResponse();
            response.setIsSuccess(true);
            response.setInfo("created");
            response.setId(id);
            URI uri
                    = uriInfo.getRequestUriBuilder().path(Long.toString(id)).build();
            return Response.created(uri).entity(response).build();
        } catch (IOException e) {
            throw new VirtualCollectionRegistryException("create", e);
        }
    }

    @Path("/{id}")
    public VirtualCollectionResource getVirtualCollection(@PathParam("id") long id) {
        final VirtualCollectionResource resource = resourceContext.getResource(VirtualCollectionResource.class);
        resource.setId(id);
        return resource;
    }

}
