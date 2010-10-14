package eu.clarin.cmdi.virtualcollectionregistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.XMLStreamWriter2;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.codehaus.stax2.validation.XMLValidationSchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;

public class VirtualCollectionMarshaller {
    private static final String NS_CMDI = "http://www.clarin.eu/cmd";
    private static final String NS_CMDI_PREFIX = "cmdi";
    // FIXME: use correct schema for CMDI virtual collections
    private static final String NS_CMDI_SCHEMA_LOCATION =
        "http://www.clarin.eu/cmd/xsd/minimal-cmdi.xsd";

    public static enum Format {
        XML, JSON, UNSUPPORTED
    } // public enum Format

    private static final String ENCODING = "UTF-8";
    private static final String VERSION = "1.0";
    private static final Logger logger =
        LoggerFactory.getLogger(VirtualCollectionMarshaller.class);
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private final XMLValidationSchema schema;
    private final XMLInputFactory2 xmlReaderFactory;
    private final XMLOutputFactory2 xmlWriterFactory;
    private final MappedXMLInputFactory jsonReaderFactory;
    private final MappedXMLOutputFactory jsonWriterFactory;

    VirtualCollectionMarshaller() throws VirtualCollectionRegistryException {
        logger.debug("initializing schemas for marshaller ...");
        try {
            XMLValidationSchemaFactory sf = XMLValidationSchemaFactory
                    .newInstance(XMLValidationSchema.SCHEMA_ID_W3C_SCHEMA);
            URL url = VirtualCollectionMarshaller.class
                    .getClassLoader()
                    .getResource("META-INF/VirtualCollection.xsd");
            if (url == null) {
                throw new NullPointerException("schema not found!");
            }
            schema = sf.createSchema(url);

            // XML factories
            xmlReaderFactory =
                (XMLInputFactory2) XMLInputFactory2.newInstance();
            xmlReaderFactory.configureForSpeed();
            xmlReaderFactory
                .setProperty(XMLInputFactory2.IS_NAMESPACE_AWARE, Boolean.TRUE);
            xmlReaderFactory
                .setProperty(XMLInputFactory2.P_INTERN_NAMES, Boolean.TRUE);
            xmlReaderFactory
                .setProperty(XMLInputFactory2.P_INTERN_NS_URIS, Boolean.TRUE);
            xmlWriterFactory =
                (XMLOutputFactory2) XMLOutputFactory2.newInstance();
            xmlWriterFactory.configureForSpeed();

            // JSON factories
            final Map<String, String> mapping = Collections.emptyMap();
            jsonReaderFactory = new MappedXMLInputFactory(mapping);
            jsonWriterFactory = new MappedXMLOutputFactory(mapping);
        } catch (Exception e) {
            logger.error("error initializing marshaller", e);
            throw new VirtualCollectionRegistryException(
                    "error initializing marshaller", e);
        }
    }

    public void marshal(OutputStream out, Format format, VirtualCollection vc)
            throws IOException {
        if (vc == null) {
            throw new NullPointerException("vc == null");
        }
        XMLStreamWriter writer = null;
        try {
            writer = createWriter(out, format, true);
            writer.writeStartDocument(ENCODING, VERSION);
            writeVirtualCollection(writer, vc);
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            logger.error("error marshalling virtual collection", e);
            throw new IOException("error marshalling virtual collection", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (XMLStreamException e) {
                /* IGNORE */
            }
        }
    }

    public void marshal(OutputStream out, Format format,
            VirtualCollectionList vcs) throws IOException {
        if (vcs == null) {
            throw new NullPointerException("vcs == null");
        }
        XMLStreamWriter writer = null;
        try {
            writer = createWriter(out, format, true);
            writer.writeStartDocument(ENCODING, VERSION);
            writer.writeStartElement("VirtualCollections");
            writer.writeAttribute("totalCount",
                    Integer.toString(vcs.getTotalCount()));
            writer.writeAttribute("offset",
                    Integer.toString(vcs.getOffset()));
            writer.writeAttribute("result",
                    (vcs.isPartialList() ? "partial" : "full"));
            for (VirtualCollection vc : vcs.getItems()) {
                writeVirtualCollection(writer, vc);
            }
            writer.writeEndElement(); // "VirtualCollections" element
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            logger.error("error marshalling virtual collections", e);
            throw new IOException("error marshalling virtual collections", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (XMLStreamException e) {
                /* IGNORE */
            }
        }
    }

    public VirtualCollection unmarshal(InputStream in, Format format,
            String encoding) throws IOException {
        XMLStreamReader reader = null;
        try {
            reader = createReader(in, format, encoding);
            return readVirtualCollection(reader);
        } catch (XMLStreamException e) {
            logger.error("error unmarshalling virtual collection", e);
            throw new IOException("error unmarshalling virtual collection", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                /* IGNORE */
            }
        }
    }

    public void marshalAsCMDI(OutputStream out, Format format,
            VirtualCollection vc) throws IOException {
        if (vc == null) {
            throw new NullPointerException("vc == null");
        }

        XMLStreamWriter writer = null;
        try {
            writer = createWriter(out, format, false);
            writer.writeStartDocument(ENCODING, VERSION);
            writeCMDI(writer, vc);
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            logger.error("error marshalling clarin virtual collections", e);
            throw new IOException(
                    "error marshalling clarin virtual collections", e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (XMLStreamException e) {
                /* IGNORE */
            }
        }
    }

    private void writeVirtualCollection(XMLStreamWriter writer,
            VirtualCollection vc) throws XMLStreamException {
        writer.writeStartElement("VirtualCollection");
        if (vc.getId() != null) {
            writer.writeAttribute("id", Long.toString(vc.getId()));
        }
        if (vc.getPersistentIdentifier() != null) {
            writer.writeAttribute("persistentId",
                    vc.getPersistentIdentifier().getIdentifier());
        }
        String s = null;
        switch (vc.getState()) {
        case PRIVATE:
            s = "private";
            break;
        case PUBLIC_PENDING:
            s = "public-pending";
            break;
        case PUBLIC:
            s = "public";
        case DELETED:
            s = "deleted";
        case DEAD:
            s = "dead";
            break;
        }
        writer.writeAttribute("state", s);
        writer.writeStartElement("Type");
        switch (vc.getType()) {
        case EXTENSIONAL:
            writer.writeCharacters("extensional");
            break;
        case INTENSIONAL:
            writer.writeCharacters("intensional");
            break;
        } // switch
        writer.writeEndElement(); // "Type" element

        writer.writeStartElement("Name");
        writer.writeCharacters(vc.getName());
        writer.writeEndElement(); // "Name" element

        if (vc.getDescription() != null) {
            writer.writeStartElement("Description");
            writer.writeCharacters(vc.getDescription());
            writer.writeEndElement(); // "Description" element
        }

        if (vc.getCreationDate() != null) {
            writer.writeStartElement("CreationDate");
            // XXX: threading vs. performance
            synchronized (df) {
                writer.writeCharacters(df.format(vc.getCreatedDate()));
            } // synchronized (df)
            writer.writeEndElement(); // "CreationDate" element
        }

        if ((vc.getCreators() != null) && !vc.getCreators().isEmpty()) {
            writer.writeStartElement("Creators");
            for (Creator creator : vc.getCreators()) {
                writer.writeStartElement("Creator");

                writer.writeStartElement("Name");
                writer.writeCharacters(creator.getName());
                writer.writeEndElement(); // "Name" element

                if (creator.getEMail() != null) {
                    writer.writeStartElement("Email");
                    writer.writeCharacters(creator.getEMail());
                    writer.writeEndElement(); // "Email" element
                }

                if (creator.getOrganisation() != null) {
                    writer.writeStartElement("Organisation");
                    writer.writeCharacters(creator.getOrganisation());
                    writer.writeEndElement(); // "Organisation" element
                }

                writer.writeEndElement(); // "Creator" element
            }
            writer.writeEndElement(); // "Creators" element
        } // Creators

        if (vc.getPurpose() != null) {
            writer.writeStartElement("Purpose");
            switch (vc.getPurpose()) {
            case RESEARCH:
                writer.writeCharacters("research");
                break;
            case REFERENCE:
                writer.writeCharacters("reference");
                break;
            case SAMPLE:
                writer.writeCharacters("sample");
                break;
            case FUTURE_USE:
                writer.writeCharacters("future-use");
                break;
            } // switch
            writer.writeEndElement(); // "Purpose" element
        }

        if (vc.getReproducibility() != null) {
            writer.writeStartElement("Reproducibility");
            switch (vc.getReproducibility()) {
            case INTENDED:
                writer.writeCharacters("intended");
                break;
            case FLUCTUATING:
                writer.writeCharacters("fluctuating");
                break;
            case UNTENDED:
                writer.writeCharacters("untended");
                break;
            } // switch
            writer.writeEndElement(); // "Reproducibility" element
        }

        if (vc.getReproducibilityNotice() != null) {
            writer.writeStartElement("ReproducibilityNotice");
            writer.writeCharacters(vc.getReproducibilityNotice());
            writer.writeEndElement(); // "ReproducibilityNotice" element
        }

        if ((vc.getKeywords() != null) && !vc.getKeywords().isEmpty()) {
            writer.writeStartElement("Keywords");

            for (final String keyword : vc.getKeywords()) {
                writer.writeStartElement("Keyword");
                writer.writeCharacters(keyword);
                writer.writeEndElement(); // "Keyword" element
            }

            writer.writeEndElement(); // "Keywords" element
        } // Keywords

        if ((vc.getResources() != null) && !vc.getResources().isEmpty()) {
            writer.writeStartElement("Resources");

            for (final Resource resource : vc.getResources()) {
                writer.writeStartElement("Resource");

                writer.writeStartElement("ResourceType");
                switch (resource.getType()) {
                case METADATA:
                    writer.writeCharacters("Metadata");
                    break;
                case RESOURCE:
                    writer.writeCharacters("Resource");
                    break;
                } // switch
                writer.writeEndElement(); // "ResourceType" element

                writer.writeStartElement("ResourceRef");
                writer.writeCharacters(resource.getRef());
                writer.writeEndElement(); // "ResourceRef" element

                writer.writeEndElement(); // "Resource" element
            }

            writer.writeEndElement(); // "Resources" element
        } // Resources

        if (vc.getGeneratedBy() != null) {
            final GeneratedBy generatedBy = vc.getGeneratedBy();
            writer.writeStartElement("GeneratedBy");

            writer.writeStartElement("Description");
            writer.writeCharacters(generatedBy.getDescription());
            writer.writeEndElement(); // "Description" element

            if (generatedBy.getURI() != null) {
                writer.writeStartElement("URI");
                writer.writeCharacters(generatedBy.getURI());
                writer.writeEndElement(); // "URI" element
            }

            if (generatedBy.getQuery() != null) {
                GeneratedBy.Query query = generatedBy.getQuery();
                writer.writeStartElement("Query");
                writer.writeAttribute("profile", query.getProfile());
                writer.writeCData(query.getValue());
                writer.writeEndElement(); // "Query" element
            }

            writer.writeEndElement(); // "GeneratedBy" element
        }

        writer.writeEndElement(); // "VirtualCollection" element
    }

    private VirtualCollection readVirtualCollection(
            XMLStreamReader reader) throws XMLStreamException {
        readStart(reader, "VirtualCollection", true, false);
        VirtualCollection.State vc_state = null;
        String s = reader.getAttributeValue(null, "state");
        if ((s != null) && !s.isEmpty()) {
            if ("private".equals(s)) {
                vc_state = VirtualCollection.State.PRIVATE;
            } else if ("public-pending".equals(s)) {
                vc_state = VirtualCollection.State.PUBLIC_PENDING;
            } else if ("public".equals(s)) {
                vc_state = VirtualCollection.State.PUBLIC;
            } else if ("deleted".equals(s)) {
                vc_state = VirtualCollection.State.DELETED;
            } else if ("dead".equals(s)) {
                vc_state = VirtualCollection.State.DEAD;
            } else {
                throw new XMLStreamException("invalid value for attribute " +
                        "'state' on element 'VirtualCollecion', expected one " +
                        "of 'private', 'public-pending', 'public', " +
                        "'deleted' or 'dead'");
            }
        }
        reader.next();
        readStart(reader, "Type", true, true);
        VirtualCollection.Type vc_type;
        s = readString(reader, true);
        if ("extensional".equals(s)) {
            vc_type = VirtualCollection.Type.EXTENSIONAL;
        } else if ("intensional".equals(s)) {
            vc_type = VirtualCollection.Type.INTENSIONAL;
        } else {
            throw new XMLStreamException("invalid value for element 'Type', " +
                    "expected either 'extensional' or 'intensional'");
        }
        readStart(reader, "Name", true, true);
        VirtualCollection vc =
            new VirtualCollection(vc_type, readString(reader, false));
        if (vc_state != null) {
            vc.setState(vc_state);
        }
        if (readStart(reader, "Description", false, true)) {
            vc.setDescription(readString(reader, false));
        }
        if (readStart(reader, "CreationDate", false, true)) {
            try {
                // XXX: threading vs. performance
                synchronized (df) {
                    vc.setCreationDate(df.parse(readString(reader, false)));
                } // synchronized df
            } catch (ParseException e) {
                throw new XMLStreamException("invalid date format", e);
            }
        }
        if (readStart(reader, "Creators", false, true)) {
            readStart(reader, "Creator", true, true);
            do {
                readStart(reader, "Name", true, true);
                Creator creator = new Creator(readString(reader, false));
                if (readStart(reader, "Email", false, true)) {
                    creator.setEMail(readString(reader, false));
                }
                if (readStart(reader, "Organisation", false, true)) {
                    creator.setOrganisation(readString(reader, false));
                }
                vc.getCreators().add(creator);
            } while (readStart(reader, "Creator", false, true));
        }
        if (readStart(reader, "Purpose", false, true)) {
            s = readString(reader, true);
            if ("research".equals(s)) {
                vc.setPurpose(VirtualCollection.Purpose.RESEARCH);
            } else if ("reference".equals(s)) {
                vc.setPurpose(VirtualCollection.Purpose.REFERENCE);
            } else if ("sample".equals(s)) {
                vc.setPurpose(VirtualCollection.Purpose.SAMPLE);
            } else if ("future-use".equals(s)) {
                vc.setPurpose(VirtualCollection.Purpose.FUTURE_USE);
            } else {
                throw new XMLStreamException("invalid value for element " +
                        "'Purpose', expected one of 'research', 'reference'," +
                        " 'sample' or 'future-use'");
            }
        }
        if (readStart(reader, "Reproducibility", false, true)) {
            s = readString(reader, true);
            if ("intended".equals(s)) {
                vc.setReproducibility(VirtualCollection
                        .Reproducibility.INTENDED);
            } else if ("fluctuating".equals(s)) {
                vc.setReproducibility(VirtualCollection
                        .Reproducibility.FLUCTUATING);
            } else if ("untended".endsWith(s)) {
                vc.setReproducibility(VirtualCollection
                        .Reproducibility.UNTENDED);
            } else {
                throw new XMLStreamException("invalid value for element " +
                        "'Reproducibility', expected one of 'intended', "+
                        "'fluctuating' or 'fluctuating'");
            }
        }
        if (readStart(reader, "ReproducibilityNotice", false, true)) {
            vc.setReproducibilityNotice(readString(reader, false));
        }
        if (readStart(reader, "Keywords", false, true)) {
            readStart(reader, "Keyword", true, true);
            do {
                vc.getKeywords().add(readString(reader, false));
            } while (readStart(reader, "Keyword", false, true));
        }
        if (readStart(reader, "Resources", false, true)) {
            readStart(reader, "Resource", true, true);
            do {
                readStart(reader, "ResourceType", true, true);
                Resource.Type r_type;
                s = readString(reader, true);
                if ("Metadata".equals(s)) {
                    r_type = Resource.Type.METADATA;
                } else if ("Resource".equals(s)) {
                    r_type = Resource.Type.RESOURCE;
                } else {
                    throw new XMLStreamException("invalid value for element " +
                            "'ResourceType', expected either 'Metadata' " +
                            "or 'Resource'");
                }
                readStart(reader, "ResourceRef", true, true);
                vc.getResources().add(
                        new Resource(r_type, readString(reader, false)));
            } while (readStart(reader, "Resource", false, true));
        }
        if (readStart(reader, "GeneratedBy", false, true)) {
            readStart(reader, "Description", true, true);
            GeneratedBy generatedBy =
                new GeneratedBy(readString(reader, false));
            if (readStart(reader, "URI", false, true)) {
                generatedBy.setURI(readString(reader, false));
            }
            if (readStart(reader, "Query", false, false)) {
                s = reader.getAttributeValue(null, "profile");
                if ((s == null) || s.isEmpty()) {
                    throw new XMLStreamException("missing or empty " +
                            "attribute 'profile' on element 'Query'");
                }
                reader.next();
                final GeneratedBy.Query query =
                    new GeneratedBy.Query(s, readString(reader, false));
                generatedBy.setQuery(query);
            }
            vc.setGeneratedBy(generatedBy);
        }
        // skip to end of virtual collection
        for (int t = reader.getEventType();
             reader.hasNext();
             t = reader.next()) {
            if ((t == XMLStreamConstants.END_ELEMENT) &&
                    "VirtualCollection".equals(reader.getLocalName())) {
                break;
            }
        }
        return vc;
    }

    public void writeCMDI(XMLStreamWriter out, VirtualCollection vc)
            throws XMLStreamException {
        if (out == null) {
            throw new NullPointerException("out == null");
        }
        if (vc == null) {
            throw new NullPointerException("vc == null");
        }

        // FIXME: use FastDateFormat?
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        out.setPrefix(NS_CMDI_PREFIX, NS_CMDI);
        out.writeStartElement(NS_CMDI, "CMD");
        out.writeNamespace(NS_CMDI_PREFIX, NS_CMDI);
        /*
         * FIXME: Use the correct schema for virtual collections here. For now,
         * just use minimal-cmdi.xsd. However, XML validation will probably fail
         * because minimal-cmdi.xsd does neither declare the "targetNamespace"
         * nor the "elementFormDefault" attributes.
         */
        out.writeNamespace("xsi",
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        out.writeAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                "schemaLocation", NS_CMDI + " " + NS_CMDI_SCHEMA_LOCATION);

        /*
         * header
         */
        out.writeStartElement(NS_CMDI, "Header");
        out.writeStartElement(NS_CMDI, "MdCreator");
        out.writeCharacters(vc.getOwner().getName());
        out.writeEndElement(); // "MdCreator" element
        out.writeStartElement(NS_CMDI, "MdCreationDate");
        out.writeCharacters(df.format(vc.getCreatedDate()));
        out.writeEndElement(); // "MdCreationDate" element
        out.writeStartElement(NS_CMDI, "MdSelfLink");

        out.writeCharacters(vc.getPersistentIdentifier().getActionableURI());
        out.writeEndElement(); // "MdSelfLink" element
        out.writeStartElement(NS_CMDI, "MdProfile");
        out.writeCharacters(NS_CMDI_SCHEMA_LOCATION);
        out.writeEndElement(); // "MdProfile" element
        out.writeEndElement(); // "Header" element

        /*
         * resources
         */
        out.writeStartElement(NS_CMDI, "Resources");
        out.writeStartElement(NS_CMDI, "ResourceProxyList");
        for (Resource resource : vc.getResources()) {
            out.writeStartElement(NS_CMDI, "ResourceProxy");
            out.writeAttribute("id", "r" + resource.getId());
            out.writeStartElement(NS_CMDI, "ResourceType");
            switch (resource.getType()) {
            case METADATA:
                out.writeCharacters("Metadata");
                break;
            case RESOURCE:
                out.writeCharacters("Resource");
                break;
            } // switch
            out.writeEndElement(); // "ResourceType" element
            out.writeStartElement(NS_CMDI, "ResourceRef");
            out.writeCharacters(resource.getRef());
            out.writeEndElement(); // "ResourceRef" element
            out.writeEndElement(); // "ResourceProxy" element
        } // for (resource)
        out.writeEndElement(); // "ResourceProxyList" element
        out.writeEmptyElement(NS_CMDI, "JournalFileProxyList");
        out.writeEmptyElement(NS_CMDI, "ResourceRelationList");
        out.writeEndElement(); // "Resources"

        /*
         * components
         */
        out.writeStartElement(NS_CMDI, "Components");
        out.writeStartElement(NS_CMDI, "VirtualCollection");
        out.writeStartElement(NS_CMDI, "Name");
        out.writeCharacters(vc.getName());
        out.writeEndElement(); // "Name" element
        if (vc.getDescription() != null) {
            out.writeStartElement(NS_CMDI, "Description");
            out.writeCharacters(vc.getDescription());
            out.writeEndElement(); // "Description" element
        }
        out.writeStartElement(NS_CMDI, "CreationDate");
        out.writeCharacters(df.format(vc.getCreationDate()));
        out.writeEndElement(); // "CreationDate" element
        if ((vc.getCreators() != null) || !vc.getCreators().isEmpty()) {
            out.writeStartElement(NS_CMDI, "Creators");
            for (Creator creator : vc.getCreators()) {
                // make sure there is anything set in creator
                if ((creator.getName() != null) ||
                        (creator.getEMail() != null) ||
                        (creator.getOrganisation() != null)) {
                    out.writeStartElement(NS_CMDI, "Creator");
                    if (creator.getName() != null) {
                        out.writeStartElement(NS_CMDI, "Name");
                        out.writeCharacters(creator.getName());
                        out.writeEndElement(); // "Name" element
                    }
                    if (creator.getEMail() != null) {
                        out.writeStartElement(NS_CMDI, "Email");
                        out.writeCharacters(creator.getEMail());
                        out.writeEndElement(); // "Email" element
                    }
                    if (creator.getOrganisation() != null) {
                        out.writeStartElement(NS_CMDI, "Organisation");
                        out.writeCharacters(creator.getOrganisation());
                        out.writeEndElement(); // "Organisation" element
                    }
                    out.writeEndElement(); // "Creator" element
                }
            } // for (Creator ...)
            out.writeEndElement(); // "Creators" element
        }
        if (vc.getPurpose() != null) {
            out.writeStartElement(NS_CMDI, "Purpose");
            switch (vc.getPurpose()) {
            case RESEARCH:
                out.writeCharacters("research");
                break;
            case REFERENCE:
                out.writeCharacters("reference");
                break;
            case SAMPLE:
                out.writeCharacters("sample");
                break;
            case FUTURE_USE:
                out.writeCharacters("future-use");
                break;
            } // switch (purpose)
            out.writeEndElement(); // "Purpose" element
        }
        if (vc.getReproducibility() != null) {
            out.writeStartElement(NS_CMDI, "Reproducibility");
            switch (vc.getReproducibility()) {
            case INTENDED:
                out.writeCharacters("intended");
                break;
            case FLUCTUATING:
                out.writeCharacters("fluctuating");
                break;
            case UNTENDED:
                out.writeCharacters("untended");
                break;
            } // switch (purpose)
            out.writeEndElement(); // "Reproducibility" element
        }
        if (vc.getReproducibilityNotice() != null) {
            out.writeStartElement(NS_CMDI, "ReproducibilityNotice");
            out.writeCharacters(vc.getReproducibilityNotice());
            out.writeEndElement(); // "ReproducibilityNotice" element
        }
        out.writeEndElement(); // "VirtualCollection" element
        out.writeEndElement(); // "Components" element

        out.writeEndElement(); // "CMD" element (root)
    }

    private XMLStreamWriter createWriter(OutputStream out, Format format,
            boolean validate) throws XMLStreamException {
        if (out == null) {
            throw new NullPointerException("out == null");
        }
        if (format == null) {
            throw new NullPointerException("format == null");
        }

        switch (format) {
        case XML:
            XMLStreamWriter writer =
                xmlWriterFactory.createXMLStreamWriter(out, ENCODING);
            if (validate) {
                ((XMLStreamWriter2) writer).validateAgainst(schema);
            }
            return writer;
        case JSON:
            return jsonWriterFactory.createXMLStreamWriter(out, ENCODING);
        default:
            // should never happen
            throw new IllegalArgumentException("unsupported input format");
        } // switch
    }

    private XMLStreamReader createReader(InputStream in, Format format,
            String encoding) throws XMLStreamException {
        if (in == null) {
            throw new NullPointerException("in == null");
        }
        if (format == null) {
            throw new NullPointerException("format == null");
        }
        if (encoding == null) {
            throw new NullPointerException("encoding == null");
        }

        switch (format) {
        case XML:
            XMLStreamReader reader =
                xmlReaderFactory.createXMLStreamReader(in, encoding);
            ((XMLStreamReader2) reader).validateAgainst(schema);
            return reader;
        case JSON:
            // FIXME: json + schema validation?
            return jsonReaderFactory.createXMLStreamReader(in, encoding);
        default:
            // should never happen
            throw new IllegalArgumentException("unsupported input format");
        } // switch
    }

    private static boolean readStart(XMLStreamReader reader, String element,
            boolean required, boolean advance) throws XMLStreamException {
        for (int type = reader.getEventType();
             reader.hasNext();
             type = reader.next()) {
            if (type == XMLStreamConstants.START_ELEMENT) {
                if (element.equals(reader.getLocalName())) {
                    if (advance) {
                        reader.next(); // advance to next tag
                    }
                    return true;
                }
                break;
            }
        } // for
        if (required) {
            throw new XMLStreamException("expected element '" +  element +
                    "' at this position");
        }
        return false;
    }

    private static String readString(XMLStreamReader reader, boolean intern)
            throws XMLStreamException {
        int type = reader.getEventType();
        while (type == XMLStreamConstants.SPACE) {
            type = reader.next();
        }
        switch (type) {
        case XMLStreamConstants.CHARACTERS:
            /* FALL-TROUGH */
        case XMLStreamConstants.CDATA:
            if (!reader.hasNext()) {
                throw new XMLStreamException("unexpected end of stream");
            }
            String s = reader.getText();
            if (s != null) {
                s = s.trim();
                if (intern) {
                    s = s.intern();
                }
            }
            if (s == null || s.isEmpty()) {
                throw new XMLStreamException("expected character content " +
                        "at this position", reader.getLocation());
            }
            reader.next();
            return s;
        default:
            throw new XMLStreamException("expected character content " +
                    "at this position", reader.getLocation());
        } // switch
    }

} // class VirtualCollectionRegistryMarshaller
