package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.util.Collections;
import java.util.List;

public class VirtualCollectionList {
    private List<VirtualCollection> collections;
    private int offset;
    private int totalCount;

    public VirtualCollectionList(List<VirtualCollection> list, int offset,
            int totalCount) {
        if (list != null) {
            this.collections = list;
        } else {
            this.collections = Collections.emptyList();
        }
        this.offset = offset;
        this.totalCount = totalCount;
    }

    public int getOffset() {
        return offset;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public boolean isPartialList() {
        return collections.size() < totalCount;
    }

    public List<VirtualCollection> getItems() {
        return collections;
    }

} // class VirtualCollectionList
