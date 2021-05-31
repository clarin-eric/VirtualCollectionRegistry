package eu.clarin.cmdi.virtualcollectionregistry.rest;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionValidationException;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.IValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Purpose;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Reproducibility;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Type;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionBuilder;
import eu.clarin.cmdi.wicket.PiwikConfig;
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
import org.apache.wicket.spring.injection.annot.SpringBean;
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
//@Path("/submit")
public class VirtualCollectionFormSubmissionResource {

    @Autowired
    private VirtualCollectionRegistry registry;
    
    @Context
    private SecurityContext security;
    @Context
    private UriInfo uriInfo;

    //TODO: how to properly wire this?
    @SpringBean
    private PiwikConfig piwikConfig;
     
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
                return submitNewIntensionalVc(name, description, intensionalDescription, intensionalUri, intensionalQueryProfile, intensionalQueryValue, keywords, purpose, reproducibility, reproducibilityNotice, creationDate);            
        }
        
        //Return error if type was not handled
        final Response.Status response = Response.Status.BAD_REQUEST;
        final String error = new ErrorPageBuilder().buildErrorPage(response.getStatusCode(), response.toString(), new Exception(String.format("Could not create virtual collection with unkown type: %s.", type.toString())));//String.format("<html>\n<body>\nCould not create virtual collection with unkown type: %s.</body>\n</html>\n", type.toString());
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
            final VirtualCollection vc = new VirtualCollectionBuilder()
                .setName(name)
                .setType(VirtualCollection.Type.EXTENSIONAL)
                .addCreator(principal)
                .addMetadataResources(metadataUris, null, null)
                .addResourceResources(resourceUris, null,null)
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
                    .path("../edit/{arg1}")
                    .build(id);
            return Response.seeOther(uri).build();
//        } catch (VirtualCollectionValidationException ex) {
//            //TODO: wrap in friendly HTML page
//            final Response.Status response = Response.Status.BAD_REQUEST;
//            
//            String errorList = "";
//            if(ex.hasErrorMessages()) {
//                for(IValidationFailedMessage errorMessage : ex.getErrorMessages()) {
//                    errorList += errorMessage.toString()+"<br />";
//                }
//            }
//            final String error = String.format("<html>\n<body>\n<h1>%d %s</h1>\nCould not create virtual collection. Error(s):<br/>%s\n</body>\n</html>\n", response.getStatusCode(), response.toString(), errorList);
//            return Response.status(response).entity(error).build();
        } catch (VirtualCollectionRegistryException ex) {
            //TODO: wrap in friendly HTML page
            final Response.Status response = Response.Status.BAD_REQUEST;
            final String error = new ErrorPageBuilder().buildErrorPage(response.getStatusCode(), response.toString(), ex);//String.format("<html>\n<body>\n<h1>%d %s</h1>\nCould not create virtual collection. Error(s):<br/>%s\n</body>\n</html>\n", response.getStatusCode(), response.toString(), ex.getMessage());
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
                    .path("../edit/{arg1}")
                    .build(id);
            return Response.seeOther(uri).build();
        } catch (Exception ex) {
            //TODO: wrap in friendly HTML page
            final Response.Status response = Response.Status.BAD_REQUEST;
            final String error = new ErrorPageBuilder().buildErrorPage(response.getStatusCode(), response.toString(), ex);//String.format("<html>\n<body>\n<h1>%d %s</h1>\nCould not create virtual collection. Error(s):<br/>%s\n</body>\n</html>\n", response.getStatusCode(), response.toString(), ex.getMessage());
            return Response.status(response).entity(error).build();
        }
    }
    
    private class ErrorPageBuilder {
        public String buildErrorPage(int statusCode, String statusMessage, Exception ex) {
            String html = "";
            html += "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n";
            
            html += "<head>";
            html += "<title>CLARIN Virtual Collection Registry</title>";
            html += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />";
            html += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />";
            html += "<link rel=\"canonical\" href=\"http://localhost:8080/vcr/public\"/>";
            html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/vlo.css\"/>";
            html += "<link rel=\"stylesheet\" type=\"text/css\" href=\"../css/vcr.css\"/>";
            html += "<link href=\"https://fonts.googleapis.com/css?family=Source+Sans+Pro:400,400i,700,700i&amp;subset=latin-ext,vietnamese\" rel=\"stylesheet\" />";        
            html += "<link rel=\"stylesheet\" href=\"../css/font-awesome.min.css\"/>";
            html += "</head>";          
            
            html += "<body>\n";
            
            html += "<header id=\"header\" role=\"banner\">\n";
            html += "<div class=\"clarin-top visible-xs\">\n";
                html += "<a class=\"logo center-block\" href=\"http://www.clarin.eu\"></a>\n";
            html += "</div>\n";
            
            html += "<div class=\"navbar-static-top  navbar-default navbar\" role=\"navigation\">\n";
            html += "<div class=\"container\">\n";
                html += "<div class=\"navbar-header\">\n";
                    html += "<button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\"#collapse1\">\n";
                        html += "<span class=\"sr-only\">Toggle Navigation</span>\n";
                        html += "<span class=\"icon-bar\"></span>\n";
                        html += "<span class=\"icon-bar\"></span>\n";
                        html += "<span class=\"icon-bar\"></span>\n";
                    html += "</button>\n";
                    html += "<a class=\"navbar-brand\" href=\"./public\" id=\"brandName1a\">\n";
                        html += "<span><i class=\"glyphicon glyphicon-book\" aria-hidden=\"true\"></i> Virtual Collection Registry</span>\n";
                    html += "</a>\n";
                html += "</div>\n";

                html += "<div class=\"collapse navbar-collapse\" role=\"navigation\" id=\"collapse1\">\n";
                    html += "<ul class=\"nav navbar-nav\" id=\"navLeftListEnclosure1b\">\n";
                    
                       html += "<li>\n";
                            html += "<a href=\"http://www.clarin.eu/\" class=\"clarin-logo hidden-xs\">\n";
            html += "<span>CLARIN</span>\n";
            html += "</a>\n";
                            html += "</li>\n";
                        html += "</ul>\n";

                    html += "</div>\n";
                html += "</div>\n";
            html += "</div>\n";
            html += "</header>\n";

            if(piwikConfig != null) {
                String mode = piwikConfig.getMode();
                if(mode.equalsIgnoreCase("alpha")) {
                    html += "<div class=\"vcr-badge alpha\"></div>\n";
                } else if(mode.equalsIgnoreCase("beta")) {
                    html += "<div class=\"vcr-badge beta\"></div>\n";
                }
            }
            
            html += "<div class=\"container-fluid\" id=\"content\">\n";           
                html += String.format("<h1>%d %s</h1>\n", statusCode, statusMessage);
                if(ex != null) {
                    if(ex instanceof VirtualCollectionValidationException) {
                        VirtualCollectionValidationException vcve = (VirtualCollectionValidationException)ex;
                        String errorList = "";
                        if(vcve.hasErrorMessages()) {
                            for(IValidationFailedMessage errorMessage : vcve.getErrorMessages()) {
                                errorList += errorMessage.toString()+"<br />";
                            }
                        }
                        html += String.format("Could not create virtual collection. Error(s):<br/>%s\n", errorList);
                    } else {
                        html += String.format("Could not create virtual collection. Error(s):<br/>%s\n", ex.getMessage());
                    }
                }            
            html += "</div>";
        
            html += getFooter();
            html += "</body>\n";
            html += "</html>\n";
            return html;
        }
        private String getFooter() {
            String html = "";
            html += "<footer id=\"footer\">\n";
            html += "<div class=\"container\">\n";
                html += "<div class=\"row\">\n";
                    html += "<div class=\"col-sm-6 col-sm-push-3 col-xs-12\">\n";
                        
                        html += "<div class=\"text-center\">\n";
                            html += "<span class=\"footer-fineprint\">\n";
                                html += "Service provided by <a href=\"https://www.clarin.eu\">CLARIN</a>\n";
                            html += "</span>\n";
                        html += "<span><br />Service hosted by IDS</span>\n";

                        html += "</div>\n";
                    html += "</div>\n";
                    html += "<div class=\"col-sm-3 col-sm-pull-6 col-xs-12\">\n";
                        html += "<div class=\"hidden-xs\">\n";
                            
                            html += "<a href=\"./about\">About</a>\n";
                        html += "</div>\n";
                        html += "<div class=\"version-info text-center-xs\">v1.1.1-alpha2</div>\n";
                    html += "</div>\n";
                    html += "<div class=\"col-sm-3 hidden-xs text-right\">\n";
                        
                        html += "<a href=\"mailto:vcr@clarin.eu\">Contact</a>\n";
                    html += "</div>\n";
                    html += "<div class=\"visible-xs-block text-center\">\n";
                        
                        html += "<a href=\"./about\">About</a>\n";
                        html += "&nbsp;<a href=\"mailto:vcr@clarin.eu\">Contact</a>\n";
                    html += "</div>\n";
                html += "</div>\n";
            html += "</div>\n";
        html += "</footer>\n";
            return html;
        }
    }
}
