package eu.clarin.cmdi.virtualcollectionregistry.rest;

import com.sun.jersey.api.core.InjectParam;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionValidationException;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.IValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Purpose;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Reproducibility;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Type;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionBuilder;
import java.net.URI;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * This is a specific endpoint to support creation of virtual connections from 
 * other applications. This is different from the standard REST endpoint in that
 * the user-agent is actually sent to this endpoint via a form submit. This will 
 * trigger authentication (especially relevant in a SAML workflow) and requires
 * the response to be HTML formatted.
 * 
 * Example curl call (assuming the webapp is deployed at http://localhost:8080/vcr): 
 * 
   curl -v \
       -u user1:user1 \
       -d 'type=EXTENSIONAL&name=test&metadataUri=http://www.clarin.eu&resourceUri=http://www.clarin.eu/&&description=test-collection&keyword=&purpose=&reproducibility=&creationDate=&queryDescription=&queryUri=&queryProfile=&queryValue=' \
       http://localhost:8080/vcr/service/submit
 * 
 * form-multipart not supported. Example: 
 * 
   curl -v \
       -u user1:user1 \
       -F 'type=EXTENSIONAL' \
       -F 'name=test' \
       -F 'metadataUri=http://www.clarin.eu/metadata/1' \
       -F 'resourceUri=http://www.clarin.eu/resource/1' \
       -F 'description=test collection' \
       -F 'keyword=' \
       -F 'purpose=' \
       -F 'reproducibility=' \
       -F 'creationDate=' \
       -F 'queryDescription=' \
       -F 'queryUri=' \
       -F 'queryProfile=' \
       -F 'queryValue=' \
       http://localhost:8080/vcr/service/submit
 * 
 * 
 * @author twagoo
 */
@Path("/submit")
public class VirtualCollectionFormSubmissionResource {

    @InjectParam
    private VirtualCollectionRegistry registry;
    @Context
    private SecurityContext security;
    @Context
    private UriInfo uriInfo;

    public VirtualCollectionFormSubmissionResource() {}
    
    // for testing
    protected VirtualCollectionFormSubmissionResource(VirtualCollectionRegistry registry, SecurityContext security, UriInfo uriInfo) {
        this.registry = registry;        
        this.security = security;        
        this.uriInfo = uriInfo;        
    }
    
    //TODO: this doesn't seem to work very well for intensional collections since
    //these cannot contain resources.
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.TEXT_HTML})
    public Response submitNewVc(
            @FormParam("type") @DefaultValue("EXTENSIONAL") Type type,
            @FormParam("name") String name,
            @FormParam("metadataUri") List<String> metadataUris,
            @FormParam("resourceUri") List<String> resourceUris,
            @FormParam("description") String description,
            //optional params
            @FormParam("keyword") List<String> keywords,
            @FormParam("purpose") Purpose purpose,
            @FormParam("reproducibility") Reproducibility reproducibility,
            @FormParam("reproducibilityNotice") String reproducibilityNotice,
            @FormParam("creationDate") Date creationDate,
            @FormParam("queryDescription") String intensionalDescription,
            @FormParam("queryUri") String intensionalUri,
            @FormParam("queryProfile") String intensionalQueryProfile,
            @FormParam("queryValue") String intensionalQueryValue
    ) {
        switch(type) {
            case EXTENSIONAL:
                return submitNewExtensionalVc(name, metadataUris, resourceUris, description, keywords, purpose, reproducibility, reproducibilityNotice, creationDate);
            case INTENSIONAL:
                return submitNewIntensionalVc(name, description, keywords, purpose, reproducibility, reproducibilityNotice, creationDate, intensionalDescription, intensionalUri, intensionalQueryProfile, intensionalQueryValue);            
        }
        
        //Return error if type was not handled
        final Response.Status response = Response.Status.BAD_REQUEST;
        final String error = String.format("<html>\n<body>\nCould not create virtual collection with unkown type: %s.</body>\n</html>\n", type.toString());
        return Response.status(response).entity(error).build();
    }
    
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.TEXT_HTML})
    @Path("/extenstional")
    public Response submitNewExtensionalVc(            
            @FormParam("name") String name,
            @FormParam("metadataUri") List<String> metadataUris,
            @FormParam("resourceUri") List<String> resourceUris,
            @FormParam("description") String description,
            //optional params
            @FormParam("keyword") List<String> keywords,
            @FormParam("purpose") Purpose purpose,
            @FormParam("reproducibility") Reproducibility reproducibility,
            @FormParam("reproducibilityNotice") String reproducibilityNotice,
            @FormParam("creationDate") Date creationDate
    ) {
        final Principal principal = security.getUserPrincipal();
        if (principal == null) {
            /*
             * should never happen, because servlet container should supply a
             * valid principal
             */
            throw new AssertionError("principal == null");
        }

        try {
            // construct a proto-VC from the form parameters
            final VirtualCollection vc = new VirtualCollectionBuilder()
                .setName(name)
                .setType(VirtualCollection.Type.EXTENSIONAL)
                .addCreator(principal)
                .addMetadataResources(metadataUris)
                .addResourceResources(resourceUris)
                .addKeywords(keywords)
                .setDescription(description)
                .setPurpose(purpose)
                .setReproducibility(reproducibility)
                .setReproducibilityNotice(reproducibilityNotice)
                .setCreationDate(creationDate)                
                .build();
            
            // create the VC in the registry (persist)
            long id = registry.createVirtualCollection(principal, vc);

            // respond with redirect to editor
            final URI uri = uriInfo.getBaseUriBuilder()
                    .path("../app/edit/{arg1}")
                    .build(id);
            return Response.seeOther(uri).build();
        } catch (VirtualCollectionValidationException ex) {
            //TODO: wrap in friendly HTML page
            final Response.Status response = Response.Status.BAD_REQUEST;
            
            String errorList = "";
            if(ex.hasErrorMessages()) {
                for(IValidationFailedMessage errorMessage : ex.getErrorMessages()) {
                    errorList += errorMessage.toString()+"<br />";
                }
            }
            final String error = String.format("<html>\n<body>\n<h1>%d %s</h1>\nCould not create virtual collection. Error(s):<br/>%s\n</body>\n</html>\n", response.getStatusCode(), response.toString(), errorList);
            return Response.status(response).entity(error).build();
        } catch (VirtualCollectionRegistryException ex) {
            //TODO: wrap in friendly HTML page
            final Response.Status response = Response.Status.BAD_REQUEST;
            final String error = String.format("<html>\n<body>\n<h1>%d %s</h1>\nCould not create virtual collection. Error(s):<br/>%s\n</body>\n</html>\n", response.getStatusCode(), response.toString(), ex.getMessage());
            return Response.status(response).entity(error).build();
        }
    }
    
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.TEXT_HTML})
    @Path("/intensional")
    public Response submitNewIntensionalVc(
            @FormParam("name") String name,            
            @FormParam("description") String description,
            //optional params
            @FormParam("keyword") List<String> keywords,
            @FormParam("purpose") Purpose purpose,
            @FormParam("reproducibility") Reproducibility reproducibility,
            @FormParam("reproducibilityNotice") String reproducibilityNotice,
            @FormParam("creationDate") Date creationDate,
            @FormParam("queryDescription") String intensionalDescription,
            @FormParam("queryUri") String intensionalUri,
            @FormParam("queryProfile") String intensionalQueryProfile,
            @FormParam("queryValue") String intensionalQueryValue
    ) {
        final Principal principal = security.getUserPrincipal();
        if (principal == null) {
            /*
             * should never happen, because servlet container should supply a
             * valid principal
             */
            throw new AssertionError("principal == null");
        }

        try {
            // construct a proto-VC from the form parameters
            final VirtualCollection vc = new VirtualCollectionBuilder()
                .setName(name)
                .setType(VirtualCollection.Type.INTENSIONAL)
                .addCreator(principal)
                .addKeywords(keywords)
                .setDescription(description)
                .setPurpose(purpose)
                .setReproducibility(reproducibility)
                .setReproducibilityNotice(reproducibilityNotice)
                .setCreationDate(creationDate)
                .setIntenstionalQuery(intensionalDescription, intensionalUri, intensionalQueryProfile, intensionalQueryValue)
                .build();
            
            // create the VC in the registry (persist)
            long id = registry.createVirtualCollection(principal, vc);

            // respond with redirect to editor
            final URI uri = uriInfo.getBaseUriBuilder()
                    .path("../app/edit/{arg1}")
                    .build(id);
            return Response.seeOther(uri).build();
        } catch (VirtualCollectionValidationException ex) {
            //TODO: wrap in friendly HTML page
            final Response.Status response = Response.Status.BAD_REQUEST;
            
            String errorList = "";
            if(ex.hasErrorMessages()) {
                for(IValidationFailedMessage errorMessage : ex.getErrorMessages()) {
                    errorList += errorMessage.toString()+"<br />";
                }
            }
            final String error = String.format("<html>\n<body>\n<h1>%d %s</h1>\nCould not create virtual collection. Error(s):<br/>%s\n</body>\n</html>\n", response.getStatusCode(), response.toString(), errorList);
            return Response.status(response).entity(error).build();
        } catch (VirtualCollectionRegistryException ex) {
            //TODO: wrap in friendly HTML page
            final Response.Status response = Response.Status.BAD_REQUEST;
            final String error = String.format("<html>\n<body>\n<h1>%d %s</h1>\nCould not create virtual collection. Error(s):<br/>%s\n</body>\n</html>\n", response.getStatusCode(), response.toString(), ex.getMessage());
            return Response.status(response).entity(error).build();
        }
    }
}
