package eu.clarin.cmdi.virtualcollectionregistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Parameter;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class QueryFactory implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(QueryFactory.class);

    private Map<String, Object> params = new HashMap<>();

    private QueryOptions options = new QueryOptions();
    private QueryOptions.Filter andFilter = new QueryOptions().and();

    public QueryOptions getQueryOptions() {
        return options;
    }

    public void applyParamsToQuery(TypedQuery query) {
        for(Parameter p : query.getParameters()) {
            logger.trace("Param name={}, type={}, position={}", p.getName(), p.getParameterType().toString(), p.getPosition());
            if(!params.containsKey(p.getName())) {
                throw new RuntimeException("Missing query parameter: "+p.getName());
            }
            query.setParameter(p.getName(), params.get(p.getName()));
        }
        /*
        for(String key: params.keySet()) {
            query.setParameter(key, params.get(key));
        }
        */
    }

    public void addParam(String key, Object value) {
        if(!params.containsKey(key)) {
            params.put(key, value);
        }
    }

    public QueryFactory and(QueryOptions.Property property, QueryOptions.Relation relation, Object value) {
        addParam(property.toString().toLowerCase(), value);
        andFilter.add(property, relation, value);
        options.setFilter(andFilter);
        return this;
    }

    public QueryFactory addSortProperty(QueryOptions.Property property, boolean asc) {
        options.addSortProperty(property, asc);
        return this;
    }
}
