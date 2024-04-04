/*
 * Copyright (C) 2014 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.apache.wicket.util.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author twagoo
 */
@Path("")
@OpenAPIDefinition(
    info = @Info(
        title = "Virtual Collection Registry REST API",
        version = "1.0.0",
        description = "The Virtual Collection Registry (VCR) REST API is documented following the open API specification. The UI on this page."
                + "allows you to test and play with the various resources."
                + "<br />Description of the API protocol is available <a href=\"../Protocol.txt\">here</a>."
                + "<br /><br />In addition to the REST API there are submission endpoints available to provide a low "
                + "friction integration with external data catalogues. Documentation is available <a href=\"https://github.com/clarin-eric/VirtualCollectionRegistry/blob/master/doc/Integration.md\">here</a>."
                + "<br />Submission endpoint test paages:"
                + "<ul>"
                + "<li><a target=\"_new\" href=\"../test_vc_submission_extensional.html\">Extensional collections</a></li>"
                + "<li><a target=\"_new\" href=\"../test_vc_submission_intensional.html\">Intensional collections</a></li>"
                + "</ul>"
                + "<br />Code repository is hosted on <a href=\"https://github.com/clarin-eric/VirtualCollectionRegistry\">GitHub</a>."
    ),
    servers = {
        @Server(
            description = "Local API endpoint",
            url = "http://localhost:8080/vcr/service"
        )
    }
)
public class BaseResource {

    private final static Logger logger = LoggerFactory.getLogger(BaseResource.class);
    
    @Context
    private ResourceContext resourceContext;

    /**
     * Serves a short description HTML page at the service root
     * @return 
     */
    @GET
    @Produces({MediaType.TEXT_HTML})
    public Response getDescription() {
        logger.info("HTTP GET / -- getDescription()");
        final StreamingOutput writer = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try (InputStream is = getClass().getResourceAsStream("/restIndex.html")) {
                    IOUtils.copy(is, output);
                } catch(IOException ex) {
                    logger.error("Failed to load restIndex.html", ex);
                } finally {
                    output.close();
                }
            }
        };
        return Response.ok(writer).type(MediaType.TEXT_HTML).build();
    }

    /**
     * Server api v1
     * @return 
     */
    @Path("/v1/collections")
    public VirtualCollectionsResource getCollectionsV1() {
        final VirtualCollectionsResource resource =
                resourceContext.getResource(VirtualCollectionsResource.class);
        return resource;
    }

    /**
     * Server api v2
     */
    /*
    @Path("/v2/collections")
    public VirtualCollectionsResource getCollectionsV2() {
        final VirtualCollectionsResource resource =
                resourceContext.getResource(VirtualCollectionsResource.class);
        return resource;
    }
     */
}
