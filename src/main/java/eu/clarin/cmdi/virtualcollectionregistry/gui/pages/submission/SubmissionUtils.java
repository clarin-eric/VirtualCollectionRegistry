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
    private final static String COLLECTION_MERGE_ID_ATTRIBUTE_NAME="submitted_collection_merge_id";

    private final static VirtualCollection.Reproducibility DEFAULT_REPRODUCIBILITY = VirtualCollection.Reproducibility.INTENDED;
    private final static VirtualCollection.Purpose DEFAULT_PURPOSE = VirtualCollection.Purpose.REFERENCE;
    
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
     * FormParam("origin") String origin //Associated with new collection and all resources, to support tracking of origin of resources.
     *
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
    public static void checkSubmission(WebRequest request, WebResponse response, ApplicationSession session, VirtualCollection.Type type) {
        debugWebRequest(request);

        final IRequestParameters params = request.getPostParameters();
        final String username = getUserAuthWorkaround(request);

        //Get user principal from the server context. If this is null, try the username
        //fallback to workaround the issue where the principal is not available in the
        //filter chain yet
        Principal principal = session.getPrincipal();
        if(principal == null && username != null) {
            logger.debug("Principal is null, using username={} instead", username);
            principal = new Principal() {
                @Override
                public String getName() {
                    return username;
                }        
            };
        } else if (principal != null) {
            logger.debug("Using principal={}", principal.getName());
        } else {
            logger.warn("Both username and principal are null. Aborting submission");
            return;
        }

        try {
            //Add shared fields to builder
            VirtualCollectionBuilder vcBuilder = new VirtualCollectionBuilder()
                .setType(type)
                .setName(params.getParameterValue("name").toString())
                .setDescription(params.getParameterValue("description").toString())
                .setOwner(principal)
                .addCreator(principal)
                .addKeywords(getAsStringList(params.getParameterValues("keyword")))
                .setPurpose(getPurposeFromParams(params, "purpose"))
                .setReproducibility(getReproducibilityFromParams(params, "reproducibility"))
                .setReproducibilityNotice(params.getParameterValue("reproducibilityNotice").toString())
                .setCreationDate(new Date());

            //Add extensional or intenstional specific fields to builder
            switch(type) {
                case EXTENSIONAL:
                    vcBuilder = vcBuilder
                        .addMetadataResources(getAsStringList(params.getParameterValues("metadataUri")))
                        .addResourceResources(getAsStringList(params.getParameterValues("resourceUri")));
                    break;
                case INTENSIONAL:
                    vcBuilder = vcBuilder
                            .setIntenstionalQuery(
                                params.getParameterValue("queryDescription").toString(),
                                params.getParameterValue("queryUri").toString(),
                                params.getParameterValue("queryProfile").toString(),
                                params.getParameterValue("queryValue").toString()
                            );
                    break;
            }

            //Build collection and serialize to the current session
            storeCollection(session, vcBuilder.build());
        } catch(VirtualCollectionRegistryUsageException ex) {
            logger.error("Failed to build virtual collection", ex);
        }
    }

    private static VirtualCollection.Reproducibility getReproducibilityFromParams(IRequestParameters params, String paramName) {
        VirtualCollection.Reproducibility result = DEFAULT_REPRODUCIBILITY;
        String val = params.getParameterValue(paramName).toString();
        if(val != null && !val.isEmpty()) {
            result = VirtualCollection.Reproducibility.valueOf(val);
        }
        return result;
    }

    private static VirtualCollection.Purpose getPurposeFromParams(IRequestParameters params, String paramName) {
        VirtualCollection.Purpose result = DEFAULT_PURPOSE;
        String val = params.getParameterValue(paramName).toString();
        if(val != null && !val.isEmpty()) {
            result = VirtualCollection.Purpose.valueOf(val);
        }
        return result;
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
            logger.debug("Clearing virtual collection ("+COLLECTION_ATTRIBUTE_NAME+") from session");
            session.setAttribute(COLLECTION_ATTRIBUTE_NAME, null); 
        }
        if(session.getAttribute(COLLECTION_MERGE_ID_ATTRIBUTE_NAME) != null) {
            logger.debug("Clearing collection merge id ("+COLLECTION_MERGE_ID_ATTRIBUTE_NAME+") from session");
            session.setAttribute(COLLECTION_MERGE_ID_ATTRIBUTE_NAME, null);
        }
    }
    
    public static void storeCollection(ApplicationSession session, VirtualCollection vc) {
        logger.debug("Storing collection into session: "+session.getId());
        session.setAttribute(COLLECTION_ATTRIBUTE_NAME, vc); 
    }
    
    public static VirtualCollection retrieveCollection(ApplicationSession session) {
        logger.debug("Loading collection from session: "+session.getId());
        Object obj = session.getAttribute(COLLECTION_ATTRIBUTE_NAME);
        if(obj == null) {
            return null;
        }
        return (VirtualCollection)obj;
    }

    public static void storeCollectionMergeId(ApplicationSession session, Long id) {
        session.setAttribute(COLLECTION_MERGE_ID_ATTRIBUTE_NAME, id);
    }

    public static Long retrieveCollectionMergeId(ApplicationSession session) {
        Object obj = session.getAttribute(COLLECTION_MERGE_ID_ATTRIBUTE_NAME);
        if(obj == null) {
            return null;
        }
        return (Long)obj;
    }

    public static void debugWebRequest(WebRequest request) {
        logger.trace("Request charset="+request.getCharset());
        logger.trace("Container request="+request.getContainerRequest().getClass());
        HttpServletRequest req = (HttpServletRequest)request.getContainerRequest();

        logger.trace("HttpServletRequest:");
        for(Object name : Collections.list(req.getHeaderNames())) {
            String values = "";
            for(Object value : Collections.list(req.getHeaders(name.toString()))) {
                if(!values.isEmpty()) {
                    values += "; ";
                }
                values += value.toString();
            }
            logger.trace("\tHeader, name="+name.toString()+", values="+values);
        }

        for(Object name : Collections.list(req.getParameterNames())) {
            String values = "";
            for(Object value : req.getParameterValues(name.toString())) {
                if(!values.isEmpty()) {
                    values += "; ";
                }
                values += value.toString();
            }
            logger.trace("\tParam, name="+name.toString()+", values="+values);
        }

        logger.trace("Wicket WebRequest:");
        IRequestParameters params = request.getPostParameters();
        for(String name : params.getParameterNames()) {
            String values = "";
            for(StringValue value : params.getParameterValues(name)) {
                if(!values.isEmpty()) {
                    values += "; ";
                }
                values += value.toString();
            }
            logger.trace("\tParam name="+name+", value(s)="+values);
        }
    }
}
