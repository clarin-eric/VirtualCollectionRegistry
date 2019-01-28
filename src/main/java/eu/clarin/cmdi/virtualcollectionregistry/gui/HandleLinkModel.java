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
package eu.clarin.cmdi.virtualcollectionregistry.gui;

import eu.clarin.cmdi.wicket.components.pid.PidType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model that takes a link from an inner model and in case of a handle (any link
 * starting with "hdl:"), will replace the scheme with the handle proxy base URL
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class HandleLinkModel implements IModel<String> {

    private final static Logger logger = LoggerFactory.getLogger(HandleLinkModel.class);
    
    private final IModel<String> linkModel;
    public static final Pattern HANDLE_PATTERN = Pattern.compile("^(hdl):(.*)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern HANDLE_WITH_RESOLVER_PATTERN = Pattern.compile("^(http[s]?://hdl.handle.net/)(.*)(@.*)?$", Pattern.CASE_INSENSITIVE);
    public static final Pattern DOI_PATTERN = Pattern.compile("^doi:(.*)$", Pattern.CASE_INSENSITIVE);
    public static final Pattern DOI_WITH_RESOLVER_PATTERN = Pattern.compile("^http[s]?://dx.doi.org/(.*)$", Pattern.CASE_INSENSITIVE);
    public static final String HANDLE_PROXY = "https://hdl.handle.net/";
    public static final String DOI_PROXY = "https://dx.doi.org/";
    public static final String URN_NBN_PREFIX = "urn:nbn";
    public static final String HDL_PREFIX = "hdl";
    public static final String DOI_PREFIX = "doi";
    public static final String URN_NBN_RESOLVER_URL = "http://www.nbn-resolving.org/redirect/";
    private static final int HANDLE_ID_GROUP = 2;
    private static final int DOI_ID_GROUP = 1;

    public HandleLinkModel(IModel<String> linkModel) {
        this.linkModel = linkModel;
    }

    public static String getActionableUri(String pidUri) {
        if(pidUri.startsWith("http") || pidUri.startsWith("https")) {
            return pidUri; //already actionable
        }
        
        String result = pidUri;
        switch(getPidType(pidUri)) {
            case DOI: 
                result = DOI_PROXY+pidUri.replaceFirst(DOI_PREFIX+":", "");
                break;
            case HANDLE: 
                result = HANDLE_PROXY+pidUri.replaceFirst(HDL_PREFIX+":", "");
                break;
            case NBN: 
                result = URN_NBN_RESOLVER_URL+pidUri;
                break;
            case UNKOWN:
            default:
                logger.warn("Failed to make actionable URI for unkown PID type: "+pidUri);
        }
        
        return result;        
    }
    public static boolean isHandle(String link) {
        final Matcher handleMatcher = HANDLE_PATTERN.matcher(link);
        if(handleMatcher.matches()) {
            return true;
        }
        final Matcher handleWithResolverMatcher = HANDLE_WITH_RESOLVER_PATTERN.matcher(link);
        if(handleWithResolverMatcher.matches()) {
            return true;
        }
        return false;
    }
    
    public static boolean isDoi(String link) {
        final Matcher handleMatcher = DOI_PATTERN.matcher(link);
        if(handleMatcher.matches()) {
            return true;
        }
        final Matcher handleWithResolverMatcher = DOI_WITH_RESOLVER_PATTERN.matcher(link);
        if(handleWithResolverMatcher.matches()) {
            return true;
        }
        return false;
    }
    
    public static boolean isNbn(String link) {
        return link.toLowerCase().startsWith(URN_NBN_PREFIX);
    }
    
    public static String getHandleIdentifier(String link) {
        //logger.info("getHandleIdentifier : link = " + link);
        
        final Matcher handleMatcher = HANDLE_PATTERN.matcher(link);
        if(handleMatcher.matches()) {
            //logger.info("Return handle id 1:"+handleMatcher.group(HANDLE_ID_GROUP));
            return handleMatcher.group(HANDLE_ID_GROUP);
        }
        final Matcher handleWithResolverMatcher = HANDLE_WITH_RESOLVER_PATTERN.matcher(link);
        if(handleWithResolverMatcher.matches()) {
            //logger.info("Return handle id 2:"+handleWithResolverMatcher.group(HANDLE_ID_GROUP));
            return handleWithResolverMatcher.group(HANDLE_ID_GROUP);
        }
        return link;
    }
    
    public static String getDoiIdentifier(String link) {
        //logger.info("getDoiIdentifier : link = " + link);
        
        final Matcher doiMatcher = DOI_PATTERN.matcher(link);
        if(doiMatcher.matches()) {
            //logger.info("Return doi id 1:"+doiMatcher.group(DOI_ID_GROUP));
            return doiMatcher.group(DOI_ID_GROUP);
        }
        final Matcher doiWithResolverMatcher = DOI_WITH_RESOLVER_PATTERN.matcher(link);
        if(doiWithResolverMatcher.matches()) {
            //logger.info("Return doi id 1:"+doiWithResolverMatcher.group(DOI_ID_GROUP));
            return doiWithResolverMatcher.group(DOI_ID_GROUP);
        }
        return link;
    }
    
    public static PidType getPidType(String link) {
        if(isHandle(link)) {
            return PidType.HANDLE;
        } else if(isDoi(link)) {
            return PidType.DOI;
        } else if(isNbn(link)) {
            return PidType.NBN;
        }
        return PidType.UNKOWN;
    }
    
    @Override
    public String getObject() {
        return getActionableUri( linkModel.getObject());
    }

    @Override
    public void setObject(String object) {
        linkModel.setObject(object);
    }

    @Override
    public void detach() {
        linkModel.detach();
    }
}
