package eu.clarin.cmdi.virtualcollectionregistry.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;

public class PersistenceFilter implements Filter {

	public void init(FilterConfig config) throws ServletException {
		// DO NOTHING
	}

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		try {
			chain.doFilter(req, res);
		} catch (IOException e) {
			throw e;
		} catch (Throwable e) {
			throw new ServletException("error while processing request", e);
		} finally {
			VirtualCollectionRegistry.instance().getDataStore()
					.closeEntityManager();
		}
	}

	public void destroy() {
		// DO NOTHING
	}

} // class PersistenceFilter
