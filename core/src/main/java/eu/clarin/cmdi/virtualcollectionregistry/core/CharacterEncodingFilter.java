package eu.clarin.cmdi.virtualcollectionregistry.core;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterEncodingFilter implements Filter {
    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final Logger logger = LoggerFactory.getLogger(CharacterEncodingFilter.class);
    
    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        logger.debug("Request encoding = "+request.getCharacterEncoding());
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(DEFAULT_ENCODING);
        }
        chain.doFilter(request, response);
    }

} // class CharacterEncodingFilter
