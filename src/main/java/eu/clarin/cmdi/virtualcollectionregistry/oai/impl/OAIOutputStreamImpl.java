package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument;


public class OAIOutputStreamImpl implements OAIOutputStream {
	private static final String NS_OAI =
		"http://www.openarchives.org/OAI/2.0/";
	private static final String NS_OAI_SCHEMA_LOCATION =
		"http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
	private static final ThreadLocal<SimpleDateFormat> sdf =
		new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdf =
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf;
		}
	};
	private static final ThreadLocal<XMLOutputFactory> writerFactory =
		new ThreadLocal<XMLOutputFactory>() {
		protected XMLOutputFactory initialValue() {
			return XMLOutputFactory.newInstance();
		}
	};
	private final XMLStreamWriter writer;
	
	OAIOutputStreamImpl(VerbContext ctx, OutputStream stream)
		throws OAIException {
		try {
			writer = writerFactory.get().createXMLStreamWriter(stream, "utf-8");
			writer.writeStartDocument("utf-8", "1.0");
			writer.setDefaultNamespace(NS_OAI);
			writer.writeStartElement("OAI-PMH");
			writer.writeDefaultNamespace(NS_OAI);
			writer.writeNamespace("xsi",
					XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
			writer.writeAttribute(
					XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
					"schemaLocation",
					NS_OAI + " " + NS_OAI_SCHEMA_LOCATION);
			
			writer.writeStartElement("responseDate");
			writeDateAsCharacters(new Date());
			writer.writeEndElement(); // responseDate element

			writer.writeStartElement("request");
			if (ctx.getVerb() != null) {
				writer.writeAttribute("verb", ctx.getVerb());
				Map<Argument.Name, String> args = ctx.getArguments();
				for (Argument.Name key : args.keySet()) {
					writer.writeAttribute(key.toXmlString(), args.get(key));
				}
			}
			writer.writeCharacters(ctx.getRequestURI());
			writer.writeEndElement(); // request element
		} catch (XMLStreamException e) {
			throw new OAIException("error while serializing response", e);
		}
	}

	@Override
	public void close() throws OAIException {
		try {
			writer.writeEndElement(); // OAI-PMH (root) element
			writer.writeEndDocument();
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			throw new OAIException("error while serializing response", e);
		}
	}

	@Override
	public void flush() throws OAIException {
		try {
			writer.flush();
		} catch (XMLStreamException e) {
			throw new OAIException("error while serializing response", e);
		}
	}

	@Override
	public void writeStartElement(String localName) throws OAIException {
		try {
			writer.writeStartElement(localName);
		} catch (XMLStreamException e) {
			throw new OAIException("error while serializing response", e);
		}
	}
	
	@Override
	public void writeStartElement(String namespaceURI, String localName)
			throws OAIException {
		try {
			writer.writeStartElement(namespaceURI, localName);
		} catch (XMLStreamException e) {
			throw new OAIException("error while serializing response", e);
		}
	}

	@Override
	public void writeStartElement(String namespaceURI, String localName,
			List<NamespaceDecl> decls) throws OAIException {
		try {
			for (NamespaceDecl decl : decls) {
				writer.setPrefix(decl.getPrefix(), decl.getNamespaceURI());
			}
			writer.writeStartElement(namespaceURI, localName);
			for (NamespaceDecl decl : decls) {
				writer.writeNamespace(decl.getPrefix(), decl.getNamespaceURI());
				if (decl.hasSchemaLocation()) {
					writer.writeAttribute(
							XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
							"schemaLocation",
							decl.getNamespaceURI() + " " +
							decl.getSchemaLocation());
				}
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
			throw new OAIException("error while serializing response", e);
		}
	}

	@Override
	public void writeEndElement() throws OAIException {
		try {
			writer.writeEndElement();
		} catch (XMLStreamException e) {
			throw new OAIException("error while serializing response", e);
		}
	}

	@Override
	public void writeAttribute(String localName, String value)
			throws OAIException {
		try {
			writer.writeAttribute(localName, value);
		} catch (XMLStreamException e) {
			throw new OAIException("error while serializing response", e);
		}
	}

	@Override
	public void writeAttribute(String namespaceURI, String localName,
			String value) throws OAIException {
		try {
			writer.writeAttribute(namespaceURI, localName, value);
		} catch (XMLStreamException e) {
			throw new OAIException("error while serializing response", e);
		}
	}

	@Override
	public void writeCharacters(String text) throws OAIException {
		try {
			writer.writeCharacters(text);
		} catch (XMLStreamException e) {
			throw new OAIException("error while serializing response", e);
		}
	}

	@Override
	public void writeDateAsCharacters(Date date) throws OAIException {
		try {
			writer.writeCharacters(sdf.get().format(date));
		} catch (XMLStreamException e) {
			throw new OAIException("error while serializing response", e);
		}
	}

} // class OAIOutputStreamImpl
