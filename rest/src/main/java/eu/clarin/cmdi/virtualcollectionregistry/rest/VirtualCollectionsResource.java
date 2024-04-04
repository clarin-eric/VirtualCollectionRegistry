package eu.clarin.cmdi.virtualcollectionregistry.rest;

import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.core.rest.RestUtils;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.rest.auth.Secured;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
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
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * REST resource representing the collection of virtual collections
 *
 * @author twagoo
 */
//@Path("/virtualcollections")
public class VirtualCollectionsResource {

    @Autowired
    private VirtualCollectionRegistry registry;
    @Autowired
    private VirtualCollectionMarshaller marshaller;

    @Context
    private ResourceContext resourceContext;
    @Context
    private SecurityContext security;
    @Context
    private UriInfo uriInfo;
    @Context
    private HttpHeaders headers;

    public VirtualCollectionsResource() {}

    public VirtualCollectionsResource(
            VirtualCollectionRegistry registry,
            VirtualCollectionMarshaller marshaller,
            ResourceContext resourceContext,
            SecurityContext security,
            UriInfo uriInfo,
            HttpHeaders headers) {
        this.registry = registry;
        this.marshaller = marshaller;
        this.resourceContext = resourceContext;
        this.security = security;
        this.uriInfo= uriInfo;
        this.headers = headers;
    }

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
    @Operation(
        summary = "Retrieve a list of public collections",
        description = "Retrieve a list of public collections.")
    @Parameters( value = {
        @Parameter(name = "query", description = "Query to filter specific collections"),
        @Parameter(name = "offset", description = "Start with this index. Default = 0"),
        @Parameter(name = "count", description = "Include this many results. Use -1 for all results. Default = -1")
    })
    public Response getVirtualCollections(
            @QueryParam("q") String query,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @DefaultValue("-1") @QueryParam("count") int count)
            throws VirtualCollectionRegistryException {
        final VirtualCollectionList vcs = registry.getVirtualCollections(query,
                (offset > 0) ? offset : 0, count);
        StreamingOutput writer = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException,
                    WebApplicationException {
                final VirtualCollection.Format format = RestUtils.getOutputFormat(headers.getAcceptableMediaTypes());
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
    @Secured
    @POST
    @Consumes({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @Produces({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
    @Operation(
            security = { @SecurityRequirement(name = "apiKey") },
            summary = "Create a new collection",
            description = "A virtual collection will be created based on the representation of the virtual collection sent in the request body. ID and state, if provided, will be ignored so this will always result in a private collection with a new identifier.")
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "401",
                            description = "Missing or invalid API key in "+HttpHeaders.AUTHORIZATION+" header.",
                            content = @Content(mediaType = "text/html")
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Unexpected server side error.",
                            content = {@Content(mediaType = "text/html")}
                    ),
                    @ApiResponse(
                            responseCode = "200",
                            description = "Representation of the created collection.",
                            content = {@Content(mediaType = "application/json"), @Content(mediaType = "application/xml")}
                    )
            })
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
            VirtualCollection.Format format = RestUtils.getInputFormat(headers);
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
