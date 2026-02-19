package eu.clarin.cmdi.virtualcollectionregistry.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="State")
@XmlEnum(String.class)
public enum State {
    @XmlEnumValue("private")
    PRIVATE,
    @XmlEnumValue("public")
    PUBLIC,
    @XmlEnumValue("public_frozen")
    PUBLIC_FROZEN,
    @JsonProperty("public_frozen_pending")
    PUBLIC_FROZEN_PENDING,
    @JsonProperty("public_pending")
    PUBLIC_PENDING;
        
        
    @Override
    public String toString() {
        switch (this) {
        case PRIVATE:
            return "private";
        case PUBLIC:
            return "public";
        case PUBLIC_FROZEN:
            return "public_frozen";
        case PUBLIC_PENDING:
            return "public_pending";
        case PUBLIC_FROZEN_PENDING:
            return "public_frozen_pending";
        default:
            throw new InternalError();

        }
    }

} // enum State
