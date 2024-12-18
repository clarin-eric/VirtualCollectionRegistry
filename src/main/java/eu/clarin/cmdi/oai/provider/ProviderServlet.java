package eu.clarin.cmdi.oai.provider;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.oai.provider.impl.OAIProvider;

public class ProviderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger logger =
        LoggerFactory.getLogger(ProviderServlet.class);
    private OAIProvider provider;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        handleRequest(req, resp);
    }

    private void handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            provider.process(request, response);
        } catch (Exception e) {
            logger.error("OAI provider error", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal OAI provider error");
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        this.provider = OAIProvider.instance();
    }

} // class OAIPMHProviderServlet
