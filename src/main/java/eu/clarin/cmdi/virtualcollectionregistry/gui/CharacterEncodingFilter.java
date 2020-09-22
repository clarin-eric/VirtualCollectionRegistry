package eu.clarin.cmdi.virtualcollectionregistry.gui;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
        logger.trace("Request encoding = "+request.getCharacterEncoding());
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(DEFAULT_ENCODING);
        }
        chain.doFilter(request, response);
    }

} // class CharacterEncodingFilter
