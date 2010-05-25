package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIProvider;

public class OAIProviderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger =
		LoggerFactory.getLogger(OAIProviderServlet.class);
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
			                   HttpServletResponse response)
		throws ServletException, IOException {
		try {
			if (provider.isAvailable()) {
				VerbContextImpl ctx = new VerbContextImpl(request, response);
				provider.process(ctx);
			} else {
				response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				response.setHeader("Retry-After", "3600");
				response.setContentType("text/plain");
				PrintWriter out = response.getWriter();
				out.println("The OAI provider is not available.");
				out.close();
			}
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
