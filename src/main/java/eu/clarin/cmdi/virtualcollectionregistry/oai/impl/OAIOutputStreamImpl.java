package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.time.FastDateFormat;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ResumptionToken;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.OAIRepository;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.Record;

public final class OAIOutputStreamImpl implements OAIOutputStream {
    private final class FlushSkipOutputStream extends FilterOutputStream {
        private byte[] buf;
        private int bufCount = 0;

        public FlushSkipOutputStream(OutputStream out, int bufsize) {
            super(out);
            this.buf = new byte[(bufsize > 1024 ? bufsize : 1024)];
        }

        @Override
        public synchronized void write(byte[] buffer, int offset, int length)
                throws IOException {
            if (buf == null) {
                throw new IOException("stream already closed");
            }
            if (buffer == null) {
                throw new NullPointerException("buffer == null");
            }
            if (offset < 0 || offset > buffer.length - length) {
                throw new ArrayIndexOutOfBoundsException(
                        "offset out of bounds: " + offset);
            }
            if (length < 0) {
                throw new ArrayIndexOutOfBoundsException(
                        "length out of bounds: " + length);
            }
            ensureCapacity(length);
            System.arraycopy(buffer, offset, buf, bufCount, length);
            bufCount += length;
        }

        @Override
        public synchronized void write(int b) throws IOException {
            if (buf == null) {
                throw new IOException("stream already closed");
            }
            ensureCapacity(1);
            buf[bufCount++] = (byte) (b & 0xFF);
        }

        @Override
        public synchronized void close() throws IOException {
            if (buf == null) {
                return;
            }
            try {
                ensureCapacity(buf.length); // explicitly force flush
                super.close();
            } finally {
                buf = null;
            }
        }

        @Override
        public synchronized void flush() throws IOException {
            // do nothing, defer flush() as long as possible
        }

        private void ensureCapacity(int needed) throws IOException {
            if (needed >= (buf.length - bufCount)) {
                out.write(buf, 0, bufCount);
                bufCount = 0;
            }
        }
    } // inner class FlushSkipOutputStream
    private static final String NS_OAI =
        "http://www.openarchives.org/OAI/2.0/";
    private static final String NS_OAI_SCHEMA_LOCATION =
        "http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
    private static final String SCHEMA_LOCATION =
        NS_OAI + " " + NS_OAI_SCHEMA_LOCATION;
    private static final XMLOutputFactory writerFactory =
        XMLOutputFactory.newInstance();
    private final OAIRepositoryAdapter repository;
    private final OutputStream stream;
    private final XMLStreamWriter writer;

    OAIOutputStreamImpl(VerbContext ctx, OutputStream out) throws OAIException {
        try {
            this.repository = ctx.getRepository();
            this.stream = new FlushSkipOutputStream(out, 8192);
            synchronized (writerFactory) {
                writer = writerFactory.createXMLStreamWriter(stream, "utf-8");
            } // synchronized (writerFactory)
            writer.writeStartDocument("utf-8", "1.0");

            StringBuilder data = new StringBuilder();
            data.append("type=\"text/xsl\" href=\"");
            data.append(ctx.getContextPath());
            data.append("/oai2.xsl\"");
            writer.writeProcessingInstruction("xml-stylesheet",
                    data.toString());

            writer.setDefaultNamespace(NS_OAI);
            writer.writeStartElement("OAI-PMH");
            writer.writeDefaultNamespace(NS_OAI);
            writer.writeNamespace("xsi",
                    XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
            writer.writeAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                    "schemaLocation", SCHEMA_LOCATION);

            FastDateFormat fmt =
                getDateFormat(OAIRepository.Granularity.SECONDS); 
            writer.writeStartElement("responseDate");
            writer.writeCharacters(fmt.format(System.currentTimeMillis()));
            writer.writeEndElement(); // responseDate element

            writer.writeStartElement("request");
            if (ctx.getVerb() != null) {
                writer.writeAttribute("verb", ctx.getVerb());
                Map<String, String> args = ctx.getUnparsedArguments();
                for (String key : args.keySet()) {
                    writer.writeAttribute(key, args.get(key));
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
            // explicitly close output stream, as XMLStreamWriter does not!
            stream.close();
        } catch (Exception e) {
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
    public XMLStreamWriter getXMLStreamWriter() throws OAIException {
        return writer;
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
            boolean schemaDeclWritten = false;
            for (NamespaceDecl decl : decls) {
                writer.writeNamespace(decl.getPrefix(), decl.getNamespaceURI());
                if (decl.hasSchemaLocation()) {
                    /*
                     * From an XML point of view, the XSI-namespace is still in
                     * scope and this is not needed, but all other providers
                     * show this behavior.
                     */
                    if (!schemaDeclWritten) {
                        writer.writeNamespace("xsi",
                                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
                        schemaDeclWritten = true;
                    }
                    writer.writeAttribute(
                            XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                            "schemaLocation", decl.getNamespaceURI() + " "
                                    + decl.getSchemaLocation());
                }
            }
        } catch (XMLStreamException e) {
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
    public void writeDate(Date date) throws OAIException {
        try {
            FastDateFormat fmt = getDateFormat(repository.getGranularity());
            writer.writeCharacters(fmt.format(date));
        } catch (XMLStreamException e) {
            throw new OAIException("error while serializing response", e);
        }
    }

    @Override
    public void writeRecordHeader(Record record) throws OAIException {
        try {
            writer.writeStartElement("header");
            if (record.isDeleted()) {
                writer.writeAttribute("status", "deleted");
            }
            writer.writeStartElement("identifier");
            writer.writeCharacters(
                    repository.createRecordId(record.getLocalId()));
            writer.writeEndElement(); // identifier element
            writer.writeStartElement("datestamp");
            writeDate(record.getDatestamp());
            writer.writeEndElement(); // datestamp element
            List<String> setSpecs = record.getSetSpecs();
            if ((setSpecs != null) && !setSpecs.isEmpty()) {
                for (String setSpec : setSpecs) {
                    writer.writeStartElement("setSpec");
                    writer.writeCharacters(setSpec);
                    writer.writeEndElement(); // setSpec element
                }
            }
            writer.writeEndElement(); // header element
        } catch (OAIException e) {
            throw e;
        } catch (XMLStreamException e) {
            throw new OAIException("error while serializing response", e);
        }
    }

    @Override
    public void writeRecord(Record record, MetadataFormat format)
            throws OAIException {
        try {
            writer.writeStartElement("record");
            writeRecordHeader(record);
            if (!record.isDeleted()) {
                writer.writeStartElement("metadata");
                format.writeObject(writer, record.getItem());
                writer.writeEndElement(); // metadata element
            }
            writer.writeEndElement(); // record element
        } catch (OAIException e) {
            throw e;
        } catch (XMLStreamException e) {
            throw new OAIException("error writing record", e);
        }
    }

    @Override
    public void writeResumptionToken(ResumptionToken token)
        throws OAIException {
        try {
            FastDateFormat fmt =
                getDateFormat(OAIRepository.Granularity.SECONDS); 
            writer.writeStartElement("resumptionToken");
            writer.writeAttribute("expirationDate",
                    fmt.format(token.getExpirationDate()));
            if (token.getCursor() >= 0) {
                writer.writeAttribute("cursor",
                        Integer.toString(token.getCursor()));
            }
            if (token.getCompleteListSize() > 0) {
                writer.writeAttribute("completeListSize",
                        Integer.toString(token.getCompleteListSize()));
            }
            writer.writeCharacters(token.getId());
            writer.writeEndElement(); // resumptionToken element
        } catch (XMLStreamException e) {
            throw new OAIException("error while serializing response", e);
        }
    }

    private FastDateFormat getDateFormat(OAIRepository.Granularity g) {
        switch (g) {
        case DAYS:
            return FastDateFormat.getInstance("yyyy-MM-dd",
                                              TimeZone.getTimeZone("UTC"));
        default:
            return FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'",
                                              TimeZone.getTimeZone("UTC"));
        } // switch
    }

} // class OAIOutputStreamImpl
