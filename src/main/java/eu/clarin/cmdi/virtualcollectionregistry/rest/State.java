package eu.clarin.cmdi.virtualcollectionregistry.rest;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="State")
@XmlEnum(String.class)
public enum State {
    @XmlEnumValue("private")
    PRIVATE,
    @XmlEnumValue("public")
    PUBLIC,
    @XmlEnumValue("public_frozen")
    PUBLIC_FROZEN;
    
    @Override
    public String toString() {
        switch (this) {
        case PRIVATE:
            return "private";
        case PUBLIC:
            return "public";
        case PUBLIC_FROZEN:
            return "public_frozen";
        default:
            throw new InternalError();

        }
    }

} // enum State
