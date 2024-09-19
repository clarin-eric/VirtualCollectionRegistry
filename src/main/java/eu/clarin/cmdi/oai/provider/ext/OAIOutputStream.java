package eu.clarin.cmdi.oai.provider.ext;

import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamWriter;

import eu.clarin.cmdi.oai.provider.MetadataFormat;
import eu.clarin.cmdi.oai.provider.OAIException;
import eu.clarin.cmdi.oai.provider.Record;

public interface OAIOutputStream {
    public static class NamespaceDecl {
        private final String namespaceURI;
        private final String prefix;
        private final String schemaLocation;

        public NamespaceDecl(String namespaceURI, String prefix,
                String schemaLocation) {
            this.namespaceURI = namespaceURI;
            this.prefix = prefix;
            this.schemaLocation = schemaLocation;
        }

        public NamespaceDecl(String namespaceURI, String prefix) {
            this(namespaceURI, prefix, null);
        }

        public String getNamespaceURI() {
            return namespaceURI;
        }

        public String getPrefix() {
            return prefix != null ? prefix : XMLConstants.DEFAULT_NS_PREFIX;
        }

        public boolean hasSchemaLocation() {
            return schemaLocation != null;
        }

        public String getSchemaLocation() {
            return schemaLocation;
        }
    } // class NamespaceDecl

    public void close() throws OAIException;

    public void flush() throws OAIException;

    public XMLStreamWriter getXMLStreamWriter() throws OAIException;

    public void writeStartElement(String localName) throws OAIException;

    public void writeStartElement(String namespaceURI, String localName)
            throws OAIException;

    public void writeStartElement(String namespaceURI, String localName,
            List<NamespaceDecl> decls) throws OAIException;

    public void writeEndElement() throws OAIException;

    public void writeAttribute(String localName, String value)
            throws OAIException;

    public void writeAttribute(String namespaceURI, String localName,
            String value) throws OAIException;

    public void writeCharacters(String text) throws OAIException;

    public void writeDate(Date date) throws OAIException;

    public void writeResumptionToken(ResumptionToken token) throws OAIException;

    public void writeRecordHeader(Record record) throws OAIException;
    
    public void writeRecord(Record record, MetadataFormat format)
            throws OAIException;

} // interface OAIOutputStream
