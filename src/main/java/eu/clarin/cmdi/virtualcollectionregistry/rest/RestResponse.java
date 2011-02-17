package eu.clarin.cmdi.virtualcollectionregistry.rest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "RegistryResult")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(propOrder = { "id", "info", "errors" })
public class RestResponse {
    private boolean isSuccess;
    private Long id;
    private String info;
    private List<String> errors;

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    @XmlAttribute(name = "success")
    public boolean isSuccess() {
        return isSuccess;
    }

    public void setId(long id) {
        this.id = Long.valueOf(id);
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
