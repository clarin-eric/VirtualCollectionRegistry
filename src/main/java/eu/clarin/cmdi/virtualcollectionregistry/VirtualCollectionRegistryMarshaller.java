package eu.clarin.cmdi.virtualcollectionregistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.model.ClarinVirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;

public class VirtualCollectionRegistryMarshaller {
	public static enum Format {
		XML, JSON, UNSUPPORTED
	} // public enum Format
	private static final String ENCODING = "UTF-8";
	private static final String VERSION = "1.0";
	private static final Logger logger =
		LoggerFactory.getLogger(VirtualCollectionRegistryMarshaller.class);
	private Schema schema = null;
	private XMLInputFactory xmlReaderFactory;
	private XMLOutputFactory xmlWriterFactory;
	private MappedXMLInputFactory jsonReaderFactory;
	private MappedXMLOutputFactory jsonWriterFactory;

	VirtualCollectionRegistryMarshaller()
			throws VirtualCollectionRegistryException {
		logger.debug("initializing schemas for marshaller ...");
		try {
			SchemaFactory sf = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			URL url = VirtualCollectionRegistryMarshaller.class
					.getClassLoader().getResource(
							"META-INF/VirtualCollection.xsd");
			if (url == null) {
				throw new NullPointerException("schema not found!");
			}
			schema = sf.newSchema(url);
			xmlReaderFactory = XMLInputFactory.newInstance();
			xmlWriterFactory = XMLOutputFactory.newInstance();
			HashMap<String, String> mapping = new HashMap<String,String>();
			jsonReaderFactory = new MappedXMLInputFactory(mapping);
			jsonWriterFactory = new MappedXMLOutputFactory(mapping);
		} catch (Exception e) {
			logger.error("error initializing marshaller", e);
			throw new VirtualCollectionRegistryException(
					"error initializing marshaller", e);
		}
	}

	public void marshal(OutputStream output, Format format,
			VirtualCollection vc) throws IOException {
		if (output == null) {
			throw new NullPointerException("output == null");
		}
		try {
			JAXBContext ctx = JAXBContext.newInstance(VirtualCollection.class);
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			m.setSchema(schema);
			XMLStreamWriter writer = createWriter(output, format);
			writer.writeStartDocument(ENCODING, VERSION);
			m.marshal(vc, writer);
			writer.writeEndDocument();
			writer.close();
		} catch (Exception e) {
			logger.error("error marshalling virtual collection", e);
			throw new IOException("error marshalling virtual collection", e);
		}
	}

	public VirtualCollection unmarshal(InputStream input, Format format,
			String encoding) throws VirtualCollectionRegistryException, IOException {
		if (input == null) {
			throw new NullPointerException("input == null");
		}
		try {
			JAXBContext ctx = JAXBContext.newInstance(VirtualCollection.class);
			Unmarshaller m = ctx.createUnmarshaller();
			m.setSchema(schema);
			XMLStreamReader reader = createReader(input, format, encoding);
			VirtualCollection vc = (VirtualCollection) m.unmarshal(reader);
			return vc;
		} catch (UnmarshalException e) {
			throw new VirtualCollectionRegistryUsageException("invalid " +
					"virtual collection format", e.getLinkedException());
		} catch (Exception e) {
			logger.error("error unmarshalling virtual collection", e);
			throw new IOException("error unmarshalling virtual collection", e);
		}
	}

	public void marshal(OutputStream output, Format format,
			VirtualCollectionList vcs) throws IOException {
		if (output == null) {
			throw new IllegalArgumentException("output == null");
		}
		try {
			JAXBContext ctx = JAXBContext.newInstance(VirtualCollection.class);
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			m.setSchema(schema);
			XMLStreamWriter writer = createWriter(output, format);
			writer.writeStartDocument(ENCODING, VERSION);
			writer.writeStartElement("VirtualCollections");
			writer.writeAttribute("totalCount",
					Integer.toString(vcs.getTotalCount()));
			writer.writeAttribute("offset",
					Integer.toString(vcs.getOffset()));
			writer.writeAttribute("result",
					(vcs.isPartialList() ? "partial" : "full"));
			for (VirtualCollection vc : vcs.getItems()) {
				m.marshal(vc, writer);
				writer.flush();
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.close();
		} catch (Exception e) {
			logger.error("error marshalling virtual collections", e);
			throw new IOException("error marshalling virtual collections", e);
		}
	}

	public void marshal(OutputStream output, Format format,
			ClarinVirtualCollection vc) throws IOException {
		if (output == null) {
			throw new NullPointerException("output == null");
		}
		try {
			JAXBContext ctx =
				JAXBContext.newInstance(ClarinVirtualCollection.class);
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			XMLStreamWriter writer = createWriter(output, format);
			writer.writeStartDocument(ENCODING, VERSION);
			m.marshal(vc, writer);
			writer.writeEndDocument();
			writer.close();
		} catch (Exception e) {
			logger.error("error marshalling clarin virtual collections", e);
			throw new IOException("error marshalling clarin virtual collections", e);
		}
	}

	private XMLStreamWriter createWriter(OutputStream output, Format format)
			throws Exception {
		if (format == null) {
			throw new NullPointerException("format == null");
		}
		switch (format) {
		case XML:
			return xmlWriterFactory.createXMLStreamWriter(output, ENCODING);
		case JSON:
			return jsonWriterFactory.createXMLStreamWriter(output, ENCODING);
		default:
			// should never happen
			throw new IllegalArgumentException("output format " + format
					+ " is not supported");
		} // switch
	}

	private XMLStreamReader createReader(InputStream input, Format format,
			String encoding) throws Exception {
		if (format == null) {
			throw new NullPointerException("format == null");
		}
		if (encoding == null) {
			throw new NullPointerException("encoding == null");
		}

		XMLStreamReader reader = null;
		switch (format) {
		case XML:
			reader = xmlReaderFactory.createXMLStreamReader(input, encoding);
			break;
		case JSON:
			reader = jsonReaderFactory.createXMLStreamReader(input, encoding);
			break;
		default:
			// should never happen
			throw new IllegalArgumentException("input format " + format
					+ " is not supported");
		} // switch

		// custom filter. clears all attributes from VirtualCollection elements
		return new StreamReaderDelegate(reader) {
			public int getAttributeCount() {
				if (super.getEventType() == XMLStreamConstants.START_ELEMENT) {
					if (super.getLocalName().equals("VirtualCollection")) {
						return 0;
					}
				}
				return super.getAttributeCount();
			}
		};
	}

} // class VirtualCollectionRegistryMarshaller
