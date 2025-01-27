package eu.clarin.cmdi.mscr.client.lib;

import jakarta.ws.rs.client.WebTarget;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author wilelb
 */
public class SearchBuilder {

    public enum Type {
        SCHEMA,
        CROSSWALK
    }
    
    private Type type = Type.SCHEMA;
    private String query;
    private String namespace;
    private String sourceSchema;
    private String targetSchema;
    
    public WebTarget addQueryParameters(WebTarget target) throws UnsupportedEncodingException {       
        WebTarget x = target.queryParam("type", type.toString());
        if(query!= null) {
            x = x.queryParam("query", URLEncoder.encode(query, "UTF-8"));
        }
        if(namespace != null) {
            x = x.queryParam("namespace", URLEncoder.encode(namespace, "UTF-8"));
        }
        if(sourceSchema != null) {
            x = x.queryParam("sourceSchemas", URLEncoder.encode(sourceSchema, "UTF-8"));
        }
        if(targetSchema != null) {
            x = x.queryParam("targetSchemas", URLEncoder.encode(targetSchema, "UTF-8"));
        }
        return x;
    } 
    
    public SearchBuilder type(Type type) {
        this.type = type;
        return this;
    }
    
    public SearchBuilder query(String query) {
        this.query = query;
        return this;
    }
    
    public SearchBuilder sourceSchema(String sourceSchema) {
        this.sourceSchema = sourceSchema;
        return this;
    }
    
    public SearchBuilder targetSchema(String targetSchema) {
        this.targetSchema = targetSchema;
        return this;
    }
            
    
    public SearchBuilder namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }
}
