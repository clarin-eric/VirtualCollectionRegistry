package eu.clarin.cmdi.virtualcollectionregistry.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class ServletUtils {

    private final static Logger logger = LoggerFactory.getLogger(ServletUtils.class);

    public static Map<String, String> createParameterMap(ServletContext servletContext) {
        if (servletContext == null) {
            logger.warn("Could not create parameters map, no servlet context provided");
            return Collections.emptyMap();
        } else {
            @SuppressWarnings("unchecked")
            final List<Object> params = Collections.list(servletContext.getInitParameterNames());
            final Map<String, String> paramMap = new HashMap<String, String>(params.size());
            for (Object param : params) {
                final String paramName = param.toString();
                final String paramValue = servletContext.getInitParameter(paramName);
                if (paramValue != null) {
                    logger.debug("Found context parameter: {} = '{}'", paramName, paramValue);
                    paramMap.put(paramName, paramValue);
                }
            }
            return paramMap;
        }
    }

}
