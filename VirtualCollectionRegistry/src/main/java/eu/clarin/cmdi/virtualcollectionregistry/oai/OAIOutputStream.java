package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.Date;
import java.util.List;

public interface OAIOutputStream {
	public static class NamespaceDecl {
		private final String namespaceURI;
		private final String prefix;
		private final String schemaLocation;
		
		public NamespaceDecl(String namespaceURI, String prefix, String schemaLocation) {
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
			return prefix;
		}
		
		public boolean hasSchemaLocation() {
			return schemaLocation != null;
		}

		public String getSchemaLocation() {
			return schemaLocation;
		}
	} // class NamespaceDecl

	public abstract void close() throws OAIException;

	public abstract void flush() throws OAIException;
	
	public abstract void writeStartElement(String localName)
			throws OAIException;

	public abstract void writeStartElement(String namespaceURI,
			String localName) throws OAIException;

	public abstract void writeStartElement(String namespaceURI,
			String localName, List<NamespaceDecl> decls) throws OAIException;

	public abstract void writeEndElement() throws OAIException;

	public abstract void writeAttribute(String localName, String value)
		throws OAIException;

	public abstract void writeAttribute(String namespaceURI, String localName,
			String value) throws OAIException;

	public abstract void writeCharacters(String text) throws OAIException;

	public abstract void writeDateAsCharacters(Date date) throws OAIException;

} // interface OAIOutputStream
