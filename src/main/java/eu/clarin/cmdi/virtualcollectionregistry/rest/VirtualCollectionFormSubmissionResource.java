package eu.clarin.cmdi.virtualcollectionregistry.rest;

import com.sun.jersey.api.core.InjectParam;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionValidationException;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.IValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedByQuery;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Purpose;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Reproducibility;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Type;
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
            @FormParam("keyword") List<String> keyword,
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
            final VirtualCollection vc = constructVirtualCollection(type, name,
                    metadataUris, resourceUris, description, keyword, purpose,
                    reproducibility, reproducibilityNotice, creationDate,
                    intensionalDescription, intensionalUri, intensionalQueryProfile, intensionalQueryValue);

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

    private VirtualCollection constructVirtualCollection(Type type, String name,
            List<String> metadataUris, List<String> resourceUris, String description,
            List<String> keywords, Purpose purpose, Reproducibility reproducibility,
            String reproducibilityNotice, Date creationDate, String intensionalDescription,
            String intensionalUri, String intensionalQueryProfile, String intensionalQueryValue) throws VirtualCollectionRegistryException {
        final VirtualCollection vc = new VirtualCollection();

        if (type == null) {
            throw new VirtualCollectionRegistryUsageException("No type specified for collection");
        }
        if (name == null) {
            throw new VirtualCollectionRegistryUsageException("No name specified for collection");

        }

        vc.setType(type);
        vc.setName(name);

        // add resources: type metadata
        for (String uri : metadataUris) {
            vc.getResources().add(new Resource(Resource.Type.METADATA, uri));
        }
        // add resources: type resource
        for (String uri : resourceUris) {
            vc.getResources().add(new Resource(Resource.Type.RESOURCE, uri));
        }
        // set optional values
        for (String keyword : keywords) {
            final String trimmed = keyword.trim();
            if (!trimmed.isEmpty()) {
                vc.getKeywords().add(keyword);
            }
        }
        if (description != null) {
            vc.setDescription(description);
        }
        if (purpose != null) {
            vc.setPurpose(purpose);
        }
        if (reproducibility != null) {
            vc.setReproducibility(reproducibility);
        }
        if (reproducibilityNotice != null) {
            vc.setReproducibilityNotice(reproducibilityNotice);
        }
        if (creationDate == null) {
            vc.setCreationDate(new Date());
        } else {
            vc.setCreationDate(creationDate);
        }

        if (intensionalDescription != null || intensionalQueryProfile != null || intensionalQueryValue != null || intensionalUri != null) {
            final GeneratedBy generatedBy = new GeneratedBy();
            generatedBy.setDescription(intensionalDescription);
            generatedBy.setQuery(new GeneratedByQuery(intensionalQueryProfile, intensionalQueryValue));
            generatedBy.setURI(intensionalUri);
            vc.setGeneratedBy(generatedBy);
        }
        return vc;
    }

}
