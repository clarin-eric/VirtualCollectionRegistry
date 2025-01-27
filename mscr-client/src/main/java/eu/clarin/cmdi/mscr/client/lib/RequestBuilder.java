package eu.clarin.cmdi.mscr.client.lib;

import java.util.List;

/**
 *
 * @author wilelb
 */
public abstract class RequestBuilder {
    
    protected boolean exists(List<String> list, String value) {            
        for(String v : list) {
            if(v.compareTo(value) == 0) {
                return true;
            }
        } 
        return false;
    }
}
