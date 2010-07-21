package eu.clarin.cmdi.oai.provider;

import java.util.List;

/**
 * A list of items from the repository as the result of a query. Used by the
 * <em>ListRecords</em> and <em>ListIdentifiers</em> verb. This can also be a
 * partial list if the repository wants do flow-control.
 */
public final class RecordList {
    private final List<Record> items;
    private final int offset;
    private final boolean hasMore;
    private final int totalCount;

    /**
     * Constructor.
     * 
     * @param records
     *            the list of records from the repository
     * @param offset
     *            the offset of this result list
     * @param hasMore
     *            <code>true</code> if these are the last records of the result
     *            set, <code>false</code> otherwise
     * @param totalCount
     *            the total count of records
     */
    public RecordList(List<Record> records, int offset, boolean hasMore,
            int totalCount) {
        if (records == null) {
            throw new NullPointerException("records == null");
        }
        if (records.isEmpty()) {
            throw new IllegalArgumentException("results may not be empty");
        }
        this.items = records;
        this.offset = offset;
        this.hasMore = hasMore;
        this.totalCount = totalCount;
    }

    /**
     * Get the list of records.
     * 
     * @return the list of records
     */
    public List<Record> getRecords() {
        return items;
    }

    /**
     * Get the offset of the first element in this (partial) result list.
     * 
     * @return the offset of the first record in this result set
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Check of the result set is incomplete and there are more records
     * available. This is needed for flow-control.
     * 
     * @return <code>true</code> if the list contains more results,
     *         <code>false</code> otherwise
     */
    public boolean hasMore() {
        return hasMore;
    }

    /**
     * Get the total number of items.
     * 
     * @return the total number of items
     */
    public int getTotalCount() {
        return totalCount > -1 ? totalCount : -1;
    }

} // class RecordList
