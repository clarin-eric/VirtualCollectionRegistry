package eu.clarin.cmdi.virtualcollectionregistry.rest.v1;

import eu.clarin.cmdi.virtualcollectionregistry.core.AdminUsersService;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollectionFactory;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.VirtualCollectionValidationException;
import eu.clarin.cmdi.virtualcollectionregistry.core.validation.feedback.IValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection.Purpose;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection.Reproducibility;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection.Type;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.PiwikConfig;
import eu.clarin.cmdi.virtualcollectionregistry.persistence.VirtualCollectionFactoryWithPersistence;
import java.net.URI;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.springframework.beans.factory.annotation.Autowired;

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
public class VirtualCollectionFormSubmissionResource extends AbstractVcrResource {
    private final ErrorBuilder errorBuilder;
    
    @Autowired
    private PiwikConfig piwikConfig;
     
    public VirtualCollectionFormSubmissionResource() {
        this.errorBuilder = new ErrorJsonBuilder();
    }
    
    // for testing
    protected VirtualCollectionFormSubmissionResource(VirtualCollectionRegistry registry, AdminUsersService adminUsersService, ResourceContext resourceContext, SecurityContext security, UriInfo uriInfo, HttpHeaders headers) {
        super(registry, adminUsersService, resourceContext, security, uriInfo, headers);
        this.errorBuilder = new ErrorJsonBuilder();
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
                return submitNewIntensionalVc(name, description, intensionalDescription, intensionalUri, intensionalQueryProfile, intensionalQueryValue, keywords, purpose, reproducibility, reproducibilityNotice, creationDate);            
        }
        
        //Return error if type was not handled
        final Response.Status response = Response.Status.BAD_REQUEST;
        final String error = errorBuilder.buildErrorPage(response.getStatusCode(), response.toString(), new Exception(String.format("Could not create virtual collection with unkown type: %s.", type.toString())));//String.format("<html>\n<body>\nCould not create virtual collection with unkown type: %s.</body>\n</html>\n", type.toString());
        return Response.status(response).entity(error).build();
    }
    
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @Produces({MediaType.TEXT_HTML})
    @Path("/extensional")
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
            VirtualCollectionFactoryWithPersistence vcf = 
                new VirtualCollectionFactoryWithPersistence(
                    VirtualCollectionFactory
                        .createNew(new User(principal.getName()), name, description)
                        .setType(Type.EXTENSIONAL)
                        .addCreator(new Creator(principal.getName(), ""))
                        .addAllKeywords(keywords)
                        .setPurpose(purpose)
                        .setReproducibility(reproducibility, reproducibilityNotice)
                );

            for(String uri : metadataUris) {
                vcf.getFactory().addResource(Resource.Type.METADATA, uri, null, null);
            }
            for(String uri : resourceUris) {
                vcf.getFactory().addResource(Resource.Type.RESOURCE, uri, null, null);
            }

            // create the VC in the registry (persist)
            long id = vcf.getPersistedCollection(registry.getVirtualCollectionDao()).getId();

            // respond with redirect to editor
            final URI uri = uriInfo.getBaseUriBuilder()
                    .path("../edit/{arg1}")
                    .build(id);
            return Response.seeOther(uri).build();
        } catch (VirtualCollectionRegistryException ex) {
            //TODO: wrap in friendly HTML page
            final Response.Status response = Response.Status.BAD_REQUEST;
            final String error = errorBuilder.buildErrorPage(response.getStatusCode(), response.toString(), ex);//String.format("<html>\n<body>\n<h1>%d %s</h1>\nCould not create virtual collection. Error(s):<br/>%s\n</body>\n</html>\n", response.getStatusCode(), response.toString(), ex.getMessage());
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
            @FormParam("queryDescription") String intensionalDescription,
            @FormParam("queryUri") String intensionalUri,
            @FormParam("queryProfile") String intensionalQueryProfile,
            @FormParam("queryValue") String intensionalQueryValue,
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
            VirtualCollectionFactoryWithPersistence vcf =
                new VirtualCollectionFactoryWithPersistence(
                    VirtualCollectionFactory
                        .createNew(new User(principal.getName()), name, description)
                        .setType(Type.INTENSIONAL)
                        .addCreator(new Creator(principal.getName(), ""))
                        .addAllKeywords(keywords)
                        .setPurpose(purpose)
                        .setReproducibility(reproducibility, reproducibilityNotice)
                        .setGeneratedBy(intensionalDescription, intensionalUri, intensionalQueryProfile, intensionalQueryValue)
                );

            // create the VC in the registry (persist)
            long id = vcf.getPersistedCollection(registry.getVirtualCollectionDao()).getId();

            // respond with redirect to editor
            final URI uri = uriInfo.getBaseUriBuilder()
                    .path("../edit/{arg1}")
                    .build(id);
            return Response.seeOther(uri).build();
        } catch (Exception ex) {
            //TODO: wrap in friendly HTML page
            final Response.Status response = Response.Status.BAD_REQUEST;
            final String error = errorBuilder.buildErrorPage(response.getStatusCode(), response.toString(), ex);//String.format("<html>\n<body>\n<h1>%d %s</h1>\nCould not create virtual collection. Error(s):<br/>%s\n</body>\n</html>\n", response.getStatusCode(), response.toString(), ex.getMessage());
            return Response.status(response).entity(error).build();
        }
    }
    
    private interface ErrorBuilder {
        public String buildErrorPage(int statusCode, String statusMessage, Exception ex);
    }
    
    private class ErrorJsonBuilder implements ErrorBuilder {
        @Override
        public String buildErrorPage(int statusCode, String statusMessage, Exception ex) {
            String json = "[";
            if(ex != null) {
                if(json.length() > 1) {
                    json += ",";
                }
                
                if(ex instanceof VirtualCollectionValidationException) {
                    VirtualCollectionValidationException vcve = (VirtualCollectionValidationException)ex;
                    String errorList = "";
                    if(vcve.hasErrorMessages()) {
                        for(IValidationFailedMessage errorMessage : vcve.getErrorMessages()) {
                            errorList += errorMessage.toString()+"<br />";
                        }
                    }
                    json += String.format("Could not create virtual collection. Error(s):<br/>%s\n", errorList);
                } else {
                    json += String.format("Could not create virtual collection. Error(s):<br/>%s\n", ex.getMessage());
                }
            }
            json += "]";
            return json;
        }
    }
}
