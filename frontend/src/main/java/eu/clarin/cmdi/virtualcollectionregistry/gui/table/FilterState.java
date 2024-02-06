package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.io.Serializable;
import java.util.Date;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
final class FilterState implements Serializable {
    public enum SearchMode {
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH
    } // enum FilterState.SearchMode
    private SearchMode nameMode;
    private String name;
    private SearchMode descriptionMode;
    private String description;
    private VirtualCollection.Type type;
    private List<VirtualCollection.State> state = new LinkedList<>();
    private QueryOptions.Relation createdRelation;
    private QueryOptions.Relation modifiedRelation;
    private Date created;
    private Date modified;
    private String origin;

    public FilterState() {
        clear();
    }

    public SearchMode getNameMode() {
        return nameMode;
    }

    public void setNameMode(SearchMode nameMode) {
        if (nameMode == null) {
            throw new IllegalArgumentException("nameMode == null");
        }
        this.nameMode = nameMode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasName() {
        return name != null;
    }

    public String getNameWithWildcard() {
        return addWildcards(name, nameMode);
    }

    public SearchMode getDescriptionMode() {
        return descriptionMode;
    }

    public void setDescriptionMode(SearchMode descriptionMode) {
        if (descriptionMode == null) {
            throw new IllegalArgumentException("descriptionMode == null");
        }
        this.descriptionMode = descriptionMode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public VirtualCollection.Type getType() {
        return type;
    }

    public void setType(VirtualCollection.Type type) {
        this.type = type;
    }

    public boolean hasType() {
        return type != null;
    }

    public List<VirtualCollection.State> getState() {
        return state;
    }

    public void setState(VirtualCollection.State state) {
        this.state.add(state);
    }

    public boolean hasState() {
        return state != null && !state.isEmpty();
    }

    public boolean hasDescription() {
        return description != null;
    }

    public String getDescriptionWithWildcard() {
        return addWildcards(description, descriptionMode);
    }

    public boolean hasOrigin() { return origin != null; }
    public String getOrigin() { return origin; }

    public void setOrigin(String origin) { this.origin = origin; }

    public QueryOptions.Relation getCreatedRelation() {
        return createdRelation;
    }

    public void setCreatedRelation(QueryOptions.Relation createdRelation) {
        if (createdRelation == null) {
            throw new IllegalArgumentException("createdRelation == null");
        }
        this.createdRelation = createdRelation;
    }

    public QueryOptions.Relation getModifiedRelation() {
        return modifiedRelation;
    }

    public void setModifiedRelation(QueryOptions.Relation modifiedRelation) {
        if (modifiedRelation == null) {
            throw new IllegalArgumentException("createdRelation == null");
        }
        this.modifiedRelation = modifiedRelation;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean hasCreated() {
        return created != null;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public boolean hasModified() {
        return modified != null;
    }

    public void clear() {
        nameMode = SearchMode.CONTAINS;
        name = null;
        descriptionMode = SearchMode.CONTAINS;
        description = null;
        type = null;
        state = new LinkedList<>();
        createdRelation = QueryOptions.Relation.EQ;
        created = null;
        modifiedRelation = QueryOptions.Relation.EQ;
        modified = null;
        origin = null;
    }

    public boolean isCleared() {
        return
            name == null &&
            description == null &&
            type == null &&
            state.isEmpty() &&
            created == null &&
            modified == null &&
            origin == null;
    }

    private static String addWildcards(String s, SearchMode mode) {
        s = s.replaceAll("\\*+", "*");
        StringBuilder sb = new StringBuilder();
        if ((mode != SearchMode.STARTS_WITH) && !s.startsWith("*")) {
            sb.append('*');
        }
        sb.append(s);
        if ((mode != SearchMode.ENDS_WITH) && !s.endsWith("*")) {
            sb.append('*');
        }
        return sb.toString();
    }

} // class FilterState
