package eu.clarin.cmdi.oai.provider.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.time.FastDateFormat;

import eu.clarin.cmdi.oai.provider.DublinCoreConverter;
import eu.clarin.cmdi.oai.provider.MetadataFormat;
import eu.clarin.cmdi.oai.provider.OAIException;

final class DublinCoreMetadataFormat implements MetadataFormat {
    private static final String SCHEMA_LOCATION =
        MetadataConstants.NS_OAI_DC + " " +
        MetadataConstants.NS_OAI_DC_SCHEMA_LOCATION;
    private final static FastDateFormat fmt =
        FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss'Z'",
                                   TimeZone.getTimeZone("UTC"));
    private final Set<DublinCoreConverter> converters;

    DublinCoreMetadataFormat(Set<DublinCoreConverter> converters) {
        this.converters = converters;
    }

    @Override
    public final String getPrefix() {
        return "oai_dc";
    }

    @Override
    public final String getNamespaceURI() {
        return "http://www.openarchives.org/OAI/2.0/oai_dc/";
    }


    @Override
    public final String getSchemaLocation() {
        return "http://www.openarchives.org/OAI/2.0/oai_dc.xsd";
    }

    @Override
    public boolean canWriteClass(Class<?> clazz) {
        return getConverter(clazz) != null;
    }
    
    @Override
    public final void writeObject(XMLStreamWriter stream, Object item)
            throws XMLStreamException, OAIException {
        final DublinCoreConverter converter = getConverter(item.getClass());
        
        stream.setPrefix("oai_dc", MetadataConstants.NS_OAI_DC);
        stream.setPrefix("dc", MetadataConstants.NS_DC);
        stream.writeStartElement(MetadataConstants.NS_OAI_DC, "dc");
        stream.writeNamespace("oai_dc", MetadataConstants.NS_OAI_DC);
        stream.writeNamespace("dc", MetadataConstants.NS_DC);
        stream.writeNamespace("xsi",
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        stream.writeAttribute(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                "schemaLocation", SCHEMA_LOCATION);

        // dc:title
        writeElement(stream, "title", converter.getTitles(item));

        // dc:creator
        writeElement(stream, "creator", converter.getCreators(item));

        // dc:subject
        writeElement(stream, "subject", converter.getSubjects(item));

        // dc:description
        writeElement(stream, "description", converter.getDescriptions(item));

        // dc:publisher
        writeElement(stream, "publisher", converter.getPublishers(item));

        // dc:contributor
        writeElement(stream, "contributor", converter.getContributors(item));

        // dc:date
        List<Date> dates = converter.getDates(item);
        if ((dates != null) && !dates.isEmpty()) {
            for (Date date : dates) {
                stream.writeStartElement(MetadataConstants.NS_DC, "date");
                stream.writeCharacters(fmt.format(date));
                stream.writeEndElement(); // close element
            }
        }

        // dc:type
        writeElement(stream, "type", converter.getTypes(item));

        // dc:format
        writeElement(stream, "format", converter.getFormats(item));

        // dc:identifier
        writeElement(stream, "identifier", converter.getIdentifiers(item));

        // dc:source
        writeElement(stream, "source", converter.getSources(item));

        // dc:language
        writeElement(stream, "language", converter.getLanguages(item));

        // dc:relation
        writeElement(stream, "relation", converter.getRelations(item));

        // dc:coverage
        writeElement(stream, "coverage", converter.getCoverages(item));

        // dc:rights
        writeElement(stream, "rights", converter.getRights(item));

        stream.writeEndElement(); // "oai_dc:dc" element
    }

    private void writeElement(XMLStreamWriter stream, String element,
            List<String> values) throws XMLStreamException {
        if ((values != null) && !values.isEmpty()) {
            for (String title : values) {
                stream.writeStartElement(MetadataConstants.NS_DC, element);
                stream.writeCharacters(title);
                stream.writeEndElement(); // close element
            }
        }
    }

    private DublinCoreConverter getConverter(Class<?> clazz) {
        for (DublinCoreConverter conveter : converters) {
            if (conveter.canProcessResource(clazz)) {
                return conveter;
            }
        }
        return null;
    }

} // class DublinCoreMetadataFormat
