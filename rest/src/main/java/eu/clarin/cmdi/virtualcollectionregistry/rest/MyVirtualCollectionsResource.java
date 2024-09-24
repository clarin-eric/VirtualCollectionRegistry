
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.core.rest.RestUtils;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.rest.auth.Secured;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;

/**
 * REST resource representing the collection of the user's virtual collections
 * (public or private)
 *
 * https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations
 *
 * @author twagoo
 */
@Path("/my-virtualcollections")
@SecurityScheme(
    name = "apiKey",
    type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER,
    paramName = HttpHeaders.AUTHORIZATION
)
public class MyVirtualCollectionsResource {

    @Autowired
    private VirtualCollectionRegistry registry;
    @Autowired
    private VirtualCollectionMarshaller marshaller;
    @Context
    private SecurityContext security;
    @Context
    private UriInfo uriInfo;
    @Context
    private HttpHeaders headers;

    /**
     * All virtual collections owned by the authenticated user will be
     * retrieved; if a query expression is used, only the virtual collections
     * satisfying the query will be retrieved.
     *
     * @param query a Virtual Collection Query Language expression
     * @param offset
     * @param count
     * @return a serialised list of all virtual collections created by the
     * authenticated user
     * @throws VirtualCollectionRegistryException if the collections could not
     * be retrieved
     * @see
     * VirtualCollectionRegistry#getVirtualCollections(java.security.Principal,
     * java.lang.String, int, int)
     */
    @Secured
    @GET
    @Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
        security = { @SecurityRequirement(name = "apiKey") },
        summary = "Retrieve a list of private collections",
        description = "Retrieve a list of private collections for the user identified via the supplied API key.")
    @Parameters( value = {
        @Parameter(name = "query", description = "Query to filter specific collections"),
        @Parameter(name = "offset", description = "Start with this index. Default = 0"),
        @Parameter(name = "count", description = "Include this many results. Use -1 for all results.")
    })
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
                            description = "List of all collections for the authenticated user.",
                            content = {@Content(mediaType = "application/json"), @Content(mediaType = "application/xml")}
                    )
            })
    public Response getMyVirtualCollections(
            @QueryParam("q") String query,
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
}
