package eu.clarin.cmdi.virtualcollectionregistry.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "urn:x-vcr:virtualcollection:resource:type")
@XmlEnum(String.class)
public enum ResourceType {
    @XmlEnumValue("Metadata")
    METADATA,
    @XmlEnumValue("Resource")
    RESOURCE;
}