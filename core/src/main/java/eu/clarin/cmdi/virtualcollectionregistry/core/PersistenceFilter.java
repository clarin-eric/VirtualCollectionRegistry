package eu.clarin.cmdi.virtualcollectionregistry.core;

import java.io.IOException;
import java.util.Collection;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class PersistenceFilter implements Filter {

    private DataStore dataStore;

    @Override
    public void init(FilterConfig config) throws ServletException {
        final WebApplicationContext springContext
                = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());
        Collection<DataStore> values = springContext.getBeansOfType(DataStore.class).values();
        if (values.isEmpty()) {
            throw new ServletException("No data store bean found");
        }
        this.dataStore = values.iterator().next();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(req, res);
        } catch (IOException e) {
            throw e;
        } finally {
            dataStore.closeEntityManager();
        }
    }

    @Override
    public void destroy() {
        // DO NOTHING
    }

} // class PersistenceFilter
