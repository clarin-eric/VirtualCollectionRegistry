package eu.clarin.cmdi.virtualcollectionregistry.rest;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionMarshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * REST resource representing the collection of the user's virtual collections
 * (public or private)
 *
 * @author twagoo
 */
@Path("/my-virtualcollections")
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
    @GET
    @Produces({MediaType.TEXT_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON})
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
            @Override
            public void write(OutputStream output) throws IOException,
                    WebApplicationException {
                final VirtualCollectionMarshaller.Format format = RestUtils.getOutputFormat(headers.getAcceptableMediaTypes());
                marshaller.marshal(output, format, vcs);
                output.close();
            }
        };
        return Response.ok(writer).build();
    }
}
