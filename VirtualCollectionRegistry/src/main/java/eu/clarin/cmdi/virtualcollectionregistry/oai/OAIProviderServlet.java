package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAIProviderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger =
		LoggerFactory.getLogger(OAIProviderServlet.class);
	private OAIProvider provider;

	private static final String NS_OAI =
		"http://www.openarchives.org/OAI/2.0/";
	private static final String NS_OAI_SCHEMA_LOCATION =
		"http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
	
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

	private void handleRequest(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			resp.setCharacterEncoding("utf-8");
			VerbContextImpl ctx = new VerbContextImpl(provider, req, resp);
			provider.process(ctx);
		} catch (OAIException e) {
			logger.error("OAI-ERROR: {}", e.getMessage());
			resp.setContentType("application/xml");
			foo(resp.getWriter());
		}
	}

	private void foo(Writer writer) throws ServletException {
		try {
			SimpleDateFormat df =
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter out = factory.createXMLStreamWriter(writer);
			out.writeStartDocument("utf-8", "1.0");
			out.setDefaultNamespace(NS_OAI);
			out.writeStartElement("OAI-PMH");
			out.writeNamespace("xsi",
							   XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			out.writeAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
							   "schemaLocation",
							   NS_OAI + " " + NS_OAI_SCHEMA_LOCATION);
			out.writeStartElement("responseDate");
			out.writeCharacters(df.format(new Date()));
			out.writeEndElement();
			out.writeEndElement();
			out.writeEndDocument();
		} catch (Exception e) {
			throw new ServletException("foo failed", e);
		}
	}

	@Override
	public void init() throws ServletException {
		super.init();
		this.provider = OAIProvider.instance();
	}

} // class OAIPMHProviderServlet
