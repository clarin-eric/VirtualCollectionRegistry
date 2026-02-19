package eu.clarin.cmdi.virtualcollectionregistry.rest;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "RegistryResult")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "id", "success", "info", "errors" })
public class RestResponse {
    private boolean success;
    private Long id = -1L;
    private String info;
    private List<String> errors;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @XmlAttribute(name = "success")
    public boolean isSuccess() {
        return success;
    }

    public void setId(long id) {
        this.id = id;
    }

    @XmlElement(name = "VirtualCollectionId")
    public Long getId() {
        return id;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @XmlElement(name = "Info")
    public String getInfo() {
        return info;
    }

    public void setError(List<String> errors) {
        if ((errors != null) && !errors.isEmpty()) {
            this.errors = errors;
        }
    }

    @XmlElementWrapper(name = "Errors")
    @XmlElements( { @XmlElement(name = "Error") })
    public List<String> getErrors() {
        return errors;
    }

} // class RestResponse
