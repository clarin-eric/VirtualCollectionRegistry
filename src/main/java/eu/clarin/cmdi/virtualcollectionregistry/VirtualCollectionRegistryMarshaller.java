package eu.clarin.cmdi.virtualcollectionregistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.util.StreamReaderDelegate;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;

public class VirtualCollectionRegistryMarshaller {
	private static final String NS_CMDI =
		"http://www.clarin.eu/cmd";
	private static final String NS_CMDI_PREFIX =
		"cmdi";
	// FIXME: use correct schema for CMDI virtual collections
	private static final String NS_CMDI_SCHEMA_LOCATION =
		"http://www.clarin.eu/cmd/xsd/minimal-cmdi.xsd";
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
			final Map<String, String> mapping = Collections.emptyMap();
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

	public void marshalAsCMDI(OutputStream output, Format format,
			VirtualCollection vc) throws IOException {
		if (output == null) {
			throw new NullPointerException("output == null");
		}
		try {
			XMLStreamWriter writer = createWriter(output, format);
			writer.writeStartDocument(ENCODING, VERSION);
			marshalAsCMDI(writer, vc);
			writer.writeEndDocument();
			writer.close();
		} catch (Exception e) {
			logger.error("error marshalling clarin virtual collections", e);
			throw new IOException("error marshalling clarin virtual collections", e);
		}
	}

	public void marshalAsCMDI(XMLStreamWriter output, VirtualCollection vc)
		throws XMLStreamException {
		if (output == null) {
			throw new NullPointerException("output == null");
		}
		if (vc == null) {
			throw new NullPointerException("vc == null");
		}
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		output.setPrefix(NS_CMDI_PREFIX, NS_CMDI);
		output.writeStartElement(NS_CMDI, "CMD");
		output.writeNamespace(NS_CMDI_PREFIX, NS_CMDI);
		/*
		 * FIXME: Use the correct schema for virtual collections here. For now,
		 *        just use minimal-cmdi.xsd. However, XML validation will
		 *        probably fail because minimal-cmdi.xsd does neither declare
		 *        the "targetNamespace" nor the "elementFormDefault" attributes. 
		 */
		output.writeNamespace("xsi",
				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		output.writeAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
				"schemaLocation", NS_CMDI + " " + NS_CMDI_SCHEMA_LOCATION);

		/*
		 * header
		 */
		output.writeStartElement(NS_CMDI, "Header");
		output.writeStartElement(NS_CMDI, "MdCreator");
		output.writeCharacters(vc.getOwner().getName());
		output.writeEndElement(); // "MdCreator" element
		output.writeStartElement(NS_CMDI, "MdCreationDate");
		output.writeCharacters(df.format(vc.getCreatedDate()));
		output.writeEndElement(); // "MdCreationDate" element
		output.writeStartElement(NS_CMDI, "MdSelfLink");
		output.writeCharacters(vc.getPersistentIdentifier().createURI());
		output.writeEndElement(); // "MdSelfLink" element
		output.writeStartElement(NS_CMDI, "MdProfile");
		output.writeCharacters(NS_CMDI_SCHEMA_LOCATION);
		output.writeEndElement(); // "MdProfile" element
		output.writeEndElement(); // "Header" element

		/*
		 * resources
		 */
		output.writeStartElement(NS_CMDI, "Resources");
		output.writeStartElement(NS_CMDI, "ResourceProxyList");
		for (Resource resource : vc.getResources()) {
			output.writeStartElement(NS_CMDI, "ResourceProxy");
			output.writeAttribute("id", resource.getIdForXml());
			output.writeStartElement(NS_CMDI, "ResourceType");
			switch (resource.getType()) {
			case METADATA:
				output.writeCharacters("Metadata");
				break;
			case RESOURCE:
				output.writeCharacters("Resource");
				break;
			} // switch
			output.writeEndElement(); // "ResourceType" element
			output.writeStartElement(NS_CMDI, "ResourceRef");
			output.writeCharacters(resource.getRef());
			output.writeEndElement(); // "ResourceRef" element
			output.writeEndElement(); // "ResourceProxy" element
		} // for (resource)
		output.writeEndElement(); // "ResourceProxyList" element
		output.writeEmptyElement(NS_CMDI, "JournalFileProxyList");
		output.writeEmptyElement(NS_CMDI, "ResourceRelationList");
		output.writeEndElement(); // "Resources"
		
		/*
		 * components
		 */
		output.writeStartElement(NS_CMDI, "Components");
		output.writeStartElement(NS_CMDI, "VirtualCollection");
		output.writeStartElement(NS_CMDI, "Name");
		output.writeCharacters(vc.getName());
		output.writeEndElement(); // "Name" element
		if (vc.getDescription() != null) {
			output.writeStartElement(NS_CMDI, "Description");
			output.writeCharacters(vc.getDescription());
			output.writeEndElement(); // "Description" element
		}
		output.writeStartElement(NS_CMDI, "CreationDate");
		output.writeCharacters(df.format(vc.getCreationDate()));
		output.writeEndElement(); // "CreationDate" element
		output.writeStartElement(NS_CMDI, "Visibility");
		switch (vc.getVisibility()) {
		case ADVERTISED:
			output.writeCharacters("advertised");
			break;
		case NON_ADVERTISED:
			output.writeCharacters("non-advertised");
			break;
		} // switch
		output.writeEndElement(); // "Visibility" element
		if (vc.getOrigin() != null) {
			output.writeStartElement(NS_CMDI, "Origin");
			output.writeCharacters(vc.getOrigin());
			output.writeEndElement(); // "Visibility" element
		}
		if (vc.getCreator() != null) {
			Creator creator = vc.getCreator();
			// make sure there is anything set in creator
			if ((creator.getName() != null) ||
				(creator.getEMail() != null) ||
                (creator.getOrganisation() != null)) {
				output.writeStartElement(NS_CMDI, "Creator");
				if (creator.getName() != null) {
					output.writeStartElement(NS_CMDI, "Name");
					output.writeCharacters(creator.getName());
					output.writeEndElement(); // "Name" element
				}
				if (creator.getEMail() != null) {
					output.writeStartElement(NS_CMDI, "Email");
					output.writeCharacters(creator.getEMail());
					output.writeEndElement(); // "Email" element
				}
				if (creator.getOrganisation() != null) {
					output.writeStartElement(NS_CMDI, "Organisation");
					output.writeCharacters(creator.getOrganisation());
					output.writeEndElement(); // "Organisation" element
				}
				output.writeEndElement(); // "Creator" element
			}
		}
		output.writeEndElement(); // "VirtualCollection" element
		output.writeEndElement(); // "Components" element

		output.writeEndElement(); // "CMD" element (root)
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
