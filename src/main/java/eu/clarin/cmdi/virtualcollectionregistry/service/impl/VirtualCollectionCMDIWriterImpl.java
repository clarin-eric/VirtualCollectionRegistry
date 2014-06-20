package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionCMDIWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.springframework.stereotype.Service;

/**
 *
 * @author twagoo
 */
@Service
public class VirtualCollectionCMDIWriterImpl implements VirtualCollectionCMDIWriter {

    private static final String NS_CMDI = "http://www.clarin.eu/cmd/";
    private static final String NS_CMDI_PREFIX = "cmdi";
    // FIXME: use correct schema for CMDI virtual collections
    private static final String NS_CMDI_SCHEMA_LOCATION
            = "http://www.clarin.eu/cmd/xsd/minimal-cmdi.xsd";

    @Override
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
        out.writeAttribute("CMDVersion", "1.1");

        /*
         * header
         */
        out.writeStartElement(NS_CMDI, "Header");
        out.writeStartElement(NS_CMDI, "MdCreator");
        out.writeCharacters(vc.getOwner().getName());
        out.writeEndElement(); // "MdCreator" element
        out.writeStartElement(NS_CMDI, "MdCreationDate");
        out.writeCharacters(df.format(vc.getDateCreated()));
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
                if ((creator.getPerson() != null)
                        || (creator.getEMail() != null)
                        || (creator.getOrganisation() != null)) {
                    out.writeStartElement(NS_CMDI, "Creator");
                    if (creator.getPerson() != null) {
                        out.writeStartElement(NS_CMDI, "Person");
                        out.writeCharacters(creator.getPerson());
                        out.writeEndElement(); // "Person" element
                    }
                    if (creator.getAddress() != null) {
                        out.writeStartElement(NS_CMDI, "Address");
                        out.writeCharacters(creator.getAddress());
                        out.writeEndElement(); // "Address" element
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
                    if (creator.getTelephone() != null) {
                        out.writeStartElement(NS_CMDI, "Telephone");
                        out.writeCharacters(creator.getTelephone());
                        out.writeEndElement(); // "Telephone" element
                    }
                    if (creator.getWebsite() != null) {
                        out.writeStartElement(NS_CMDI, "Website");
                        out.writeCharacters(creator.getWebsite());
                        out.writeEndElement(); // "Website" element
                    }
                    if (creator.getRole() != null) {
                        out.writeStartElement(NS_CMDI, "Role");
                        out.writeCharacters(creator.getRole());
                        out.writeEndElement(); // "Role" element
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

}
