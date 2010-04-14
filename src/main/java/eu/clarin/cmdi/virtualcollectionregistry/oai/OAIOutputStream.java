package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.Date;
import java.util.List;

import javax.xml.XMLConstants;

public interface OAIOutputStream {
	public static class NamespaceDecl {
		private final String namespaceURI;
		private final String prefix;
		private final String schemaLocation;

		public NamespaceDecl(String namespaceURI, String prefix,
				String schemaLocation) {
			this.namespaceURI   = namespaceURI;
			this.prefix         = prefix;
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
	
	public void writeStartElement(String localName)
			throws OAIException;

	public void writeStartElement(String namespaceURI,
			String localName) throws OAIException;

	public void writeStartElement(String namespaceURI,
			String localName, List<NamespaceDecl> decls) throws OAIException;

	public void writeEndElement() throws OAIException;

	public void writeAttribute(String localName, String value)
		throws OAIException;

	public void writeAttribute(String namespaceURI, String localName,
			String value) throws OAIException;

	public void writeCharacters(String text) throws OAIException;

	public void writeDateAsCharacters(Date date) throws OAIException;

} // interface OAIOutputStream
