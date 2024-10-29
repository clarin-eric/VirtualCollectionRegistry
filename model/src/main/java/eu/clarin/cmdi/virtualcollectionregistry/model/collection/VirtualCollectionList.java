package eu.clarin.cmdi.virtualcollectionregistry.model.collection;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

public class VirtualCollectionList implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<VirtualCollection> virtualCollections;
    private int offset;
    private int totalCount;
    private String result;
    
    public VirtualCollectionList() {}
    
    public VirtualCollectionList(List<VirtualCollection> list, int offset,
            int totalCount) {
        if (list != null) {
            this.virtualCollections = list;
        } else {
            this.virtualCollections = Collections.emptyList();
        }
        this.offset = offset;
        this.totalCount = totalCount;
    }

    @XmlAttribute(name = "offset")
    @JsonProperty(value = "@offset")
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public int getOffset() {
        return offset;
    }

    @XmlAttribute(name = "totalCount")
    @JsonProperty(value = "@totalCount")
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public int getTotalCount() {
        return totalCount;
    }

    public boolean isPartialList() {
        return virtualCollections.size() < totalCount;
    }

    @XmlElement(name = "VirtualCollection")
    @JsonProperty(value = "VirtualCollection")
    public void setVirtualCollections(List<VirtualCollection> virtualCollections) {
        this.virtualCollections = virtualCollections;
    }
    
    public List<VirtualCollection> getVirtualCollections() {
        return virtualCollections;
    }

    public void addVirtualCollections(VirtualCollection vc) {
        if(vc != null) {
            //Todo: prevent duplicates?
            if(this.virtualCollections == null) {
                this.virtualCollections = new LinkedList<>();
            }
            virtualCollections.add(vc);
        }
    }
    
    @XmlAttribute(name = "result")
    @JsonProperty(value = "@result")
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }

} // class VirtualCollectionList
