package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.util.Iterator;
import java.util.List;

public class VirtualCollectionList implements Iterable<VirtualCollection> {
	private List<VirtualCollection> collections;
	private int offset;
	private int totalCount;
	
	public VirtualCollectionList(List<VirtualCollection> list, int offset,
			int totalCount) {
		this.collections = list;
		this.offset      = offset;
		this.totalCount  = totalCount;
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

	public Iterator<VirtualCollection> iterator() {
		return collections.iterator();
	}
	
} // class VirtualCollectionList
