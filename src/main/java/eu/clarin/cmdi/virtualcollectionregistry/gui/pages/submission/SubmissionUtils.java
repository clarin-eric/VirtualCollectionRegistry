/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionBuilder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.string.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class SubmissionUtils {
    
    private static Logger logger = LoggerFactory.getLogger(SubmissionUtils.class);
    
    private final static String COLLECTION_ATTRIBUTE_NAME="submitted_collection";
    //private final static String RETURN_ATTRIBUTE_NAME="return";
    
    private static void debugHttpHeaders(WebRequest request) {
         HttpServletRequest r = (HttpServletRequest)request.getContainerRequest();
        Enumeration e = r.getHeaderNames();
        while(e.hasMoreElements()) {
            String name = e.nextElement().toString();
            logger.info("Header: {}={}", name, r.getHeader(name));
        }
    }
    
    /**
     * Try to obtain a meaningful username from the headers sent by the client in
     * case of basic or shibboleth based authentication.
     * 
     * @param request
     * @return 
     */
    private static String getUserAuthWorkaround(WebRequest request) {       
        //Basic authentication
        String authz = request.getHeader("authorization");
        if(authz != null) {
            String[] p = authz.split(" ");
            if(p[0].equalsIgnoreCase("basic")) {
                byte[] decodedBytes = Base64.getDecoder().decode(p[1]);
                String decodedString = new String(decodedBytes);
                String[] p2 = decodedString.split(":");
                return p2[0];
            }
        }
        
        //SAML authentication        
        authz = request.getHeader("auth_type");
        if(authz != null && authz.equalsIgnoreCase("shibboleth")) {
            String username = request.getHeader("oid-edupersonprincipalname");
            if(username != null) {
                return username;
            }
            username = request.getHeader("mace-edupersonprincipalname");
            if(username != null) {
                return username;
            }
            username = request.getHeader("edupersontargetedid");
            if(username != null) {
                return username;
            }
        }
        
        return null;
    }
    
    /**
     * Supported input
     * //required params
     * 
     * FormParam("name") String name,            
     * FormParam("description") String description,            
     * 
     * //optional params
     * FormParam("keyword") List<String> keywords,
     * FormParam("purpose") Purpose purpose,
     * FormParam("reproducibility") Reproducibility reproducibility,
     * FormParam("reproducibilityNotice") String reproducibilityNotice,
     * 
     * //required Extensional params
     * FormParam("metadataUri") List<String> metadataUris,
     * FormParam("resourceUri") List<String> resourceUris,
     * 
     * //required Intensional params
     * FormParam("queryDescription") String intensionalDescription,
     * FormParam("queryUri") String intensionalUri,
     * FormParam("queryProfile") String intensionalQueryProfile,
     * FormParam("queryValue") String intensionalQueryValue
     * 
     * @param request
     * @param response
     * @param session
     * @param type
     * @return 
     */
    public static VirtualCollection checkSubmission(WebRequest request, WebResponse response, ApplicationSession session, VirtualCollection.Type type) {          
        final String username = getUserAuthWorkaround(request);

        //Get user principal from the server context. If this is null, try the username
        //fallback to workaround the issue where the principal is available in the 
        //filter chain yet
        Principal principal = session.getPrincipal();
        if(principal == null && username != null) {
            principal = new Principal() {
                @Override
                public String getName() {
                    return username;
                }        
            };
        } else if (principal != null) {
            logger.info("Using username={} from principal", principal.getName());
        }
        
        logger.info("Request charset="+request.getCharset());
        logger.info("Container request="+request.getContainerRequest().getClass());
        HttpServletRequest req = (HttpServletRequest)request.getContainerRequest();
        Enumeration e = req.getHeaderNames();
        
        for(Object name : Collections.list(req.getHeaderNames())) {
            String values = "";
            for(Object value : Collections.list(req.getHeaders(name.toString()))) {
                if(!values.isEmpty()) {
                    values += "; ";
                }
                values += value.toString();
            }
            logger.info("Header, name="+name.toString()+", values="+values);
        }
        
        IRequestParameters params = request.getPostParameters();
        for(String name : params.getParameterNames()) {
            String values = "";
            for(StringValue value : params.getParameterValues(name)) {
                if(!values.isEmpty()) {
                    values += "; ";
                }
                values += value.toString();
            }
            logger.info("param name="+name+", value(s)="+values);
        }
        
        String name = params.getParameterValue("name").toString();
        String description = params.getParameterValue("description").toString();
        String reproducibilityNotice = params.getParameterValue("reproducibilityNotice").toString();       
        
        String val = params.getParameterValue("reproducibility").toString();
        VirtualCollection.Reproducibility reproducibility = VirtualCollection.Reproducibility.INTENDED;
        if(val != null && !val.isEmpty()) {
            reproducibility = VirtualCollection.Reproducibility.valueOf(val);
        }
        
        val = params.getParameterValue("purpose").toString();
        VirtualCollection.Purpose purpose = VirtualCollection.Purpose.REFERENCE;
        if(val != null && !val.isEmpty()) {
            purpose = VirtualCollection.Purpose.valueOf(val);
        }
        
        VirtualCollection vc = null; 
        try {
            
            switch(type) {
                case EXTENSIONAL:                 
                    vc = new VirtualCollectionBuilder()
                        .setName(name)
                        .setOwner(principal) 
                        .setType(VirtualCollection.Type.EXTENSIONAL)
                        .addCreator(principal)
                        .addMetadataResources(getAsStringList(params.getParameterValues("metadataUri")))
                        .addResourceResources(getAsStringList(params.getParameterValues("resourceUri")))
                        .addKeywords(getAsStringList(params.getParameterValues("keyword")))
                        .setDescription(description)
                        .setPurpose(purpose)
                        .setReproducibility(reproducibility)
                        .setReproducibilityNotice(reproducibilityNotice)
                        .setCreationDate(new Date())     
                        .build();
                    break;
                case INTENSIONAL: 
                    params.getParameterValue("queryDescription");
                    params.getParameterValue("queryUri");
                    params.getParameterValue("queryProfile");
                    params.getParameterValue("queryValue");
                     vc = new VirtualCollectionBuilder()
                        .setName(name)
                        .setOwner(principal) 
                        .setType(VirtualCollection.Type.INTENSIONAL)
                        .addCreator(principal)                    
                        .addKeywords(getAsStringList(params.getParameterValues("keyword")))
                        .setDescription(description)
                        .setPurpose(purpose)
                        .setReproducibility(reproducibility)
                        .setReproducibilityNotice(reproducibilityNotice)
                        .setCreationDate(new Date())
                        .build();
                    break;
            }
            
                storeCollection(session, vc);      //Serialize the collection to the current session           
            logger.info("Build virtual collection");
        } catch(VirtualCollectionRegistryUsageException ex) {
            logger.error("Failed to build virtual collection", ex);
        }
        
        if(principal == null) {
            //Not authenticated
            logger.warn("Not authenticated");
            return null;
        }
        
        return vc;
    }
    
    private static List<String> getAsStringList(List<StringValue> input) {
        List<String> result = new ArrayList<>();
        if(input != null) {
            for(StringValue p : input) {
                result.add(p.toString());
            }
        }
        return result;
    }
    
    public static void clearCollectionFromSession(ApplicationSession session) {
        if(session.getAttribute(COLLECTION_ATTRIBUTE_NAME) != null) {
            logger.debug("Clearing virtual collection from session");
            session.setAttribute(COLLECTION_ATTRIBUTE_NAME, null); 
        }
    }
    
    protected static void storeCollection(ApplicationSession session, VirtualCollection vc) {
        logger.info("Storing collection into session: "+session.getId());
        session.setAttribute(COLLECTION_ATTRIBUTE_NAME, vc); 
        //storeCollectionInCookie
    }
    
    public static VirtualCollection retrieveCollection(ApplicationSession session) {
        logger.info("Loading collection from session: "+session.getId());
        VirtualCollection vc = (VirtualCollection)session.getAttribute(COLLECTION_ATTRIBUTE_NAME);        
        //VirtualCollection vc = readCollectionFromCookie();
        return vc;
    }
    
     /*
    private void storeCollectionInCookie() {       
        String cookieValue = serializeCollection();
        WebResponse webResponse = (WebResponse)RequestCycle.get().getResponse();
        Cookie cookie = new Cookie("collection", cookieValue);
        cookie.setPath("/vcr");
        webResponse.addCookie(cookie);
        Cookie cookie2 = new Cookie("return", "extensional"); //TODO: how to make this dynamic
        cookie2.setPath("/vcr");
        webResponse.addCookie(cookie2);
        
    }
    
    private VirtualCollection readCollectionFromCookie() {
        Cookie cookie = ((WebRequest)RequestCycle.get().getRequest()).getCookie("collection");
        if (cookie == null) {
            return null;
        }
        return deserializeCollection(cookie.getValue());
    }
    
     private String getEncodedValue(StringValue val) {
        String result = "";
        if(val != null && val.toString() != null) {             
            result = getEncodedValue(val.toString());
        }
        return result;
    }
    
    private String getEncodedValue(String val) {
        String result = "";
        if(val != null) {             
            try {
                result = URLEncoder.encode(val, "UTF-8");
            } catch(UnsupportedEncodingException ex) {
                logger.warn("Failed to encode value.", ex);
            }
        }
        return result;
    }
    
   
    
    private String serializeCollection() {
        VirtualCollection vc = checkSubmission(VirtualCollection.Type.EXTENSIONAL);
        if(vc == null) {
            return null;
        }
        IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();

        String serialized = "name="+getEncodedValue(vc.getName());
        serialized += "&description="+getEncodedValue(vc.getDescription());
        serialized += "&reproducibilityNotice="+getEncodedValue(vc.getReproducibilityNotice());
        if(vc.getPurpose() == null) {
            serialized += "&purpose=";
        } else {
            serialized += "&purpose="+getEncodedValue(vc.getPurpose().toString());
        }
        for(String val : vc.getKeywords()) {
            serialized += "&keyword="+getEncodedValue(val);
        }
        for(Resource val : vc.getResources()) {
            switch(val.getType()) {
                case METADATA:
                    serialized += "&metadataUri="+getEncodedValue(val.getRef());
                    break;
                case RESOURCE:
                    serialized += "&resourceUri="+getEncodedValue(val.getRef());
                    break;
            } 
        }
        return serialized;
    }
    
    private VirtualCollection deserializeCollection(String serialized) {
         VirtualCollectionBuilder builder = new VirtualCollectionBuilder();        
        try {
            List<NameValuePair> params = URLEncodedUtils.parse(serialized, Charset.forName("UTF-8"));
            for(NameValuePair param : params) {
                switch(param.getName()) {
                    case "name": builder.setName(param.getValue()); break;
                    case "description": builder.setDescription(param.getValue()); break;
                    case "reproducibilityNotice": builder.setReproducibilityNotice(param.getValue()); break;
                    case "purpose": 
                            if(param.getValue() != null && !param.getValue().isEmpty()) {
                                builder.setPurpose(VirtualCollection.Purpose.valueOf(param.getValue())); 
                            }
                        break;
                    case "keyword": builder.addKeyword(param.getValue()); break;
                    case "metadataUri": builder.addMetadataResource(param.getValue()); break;
                    case "resourceUri": builder.addResourceResource(param.getValue()); break;
                }
            }
        } catch(VirtualCollectionRegistryUsageException ex) {
            logger.warn("Failed to build vc", ex);
        }
        return builder.build();
    }
*/
}
