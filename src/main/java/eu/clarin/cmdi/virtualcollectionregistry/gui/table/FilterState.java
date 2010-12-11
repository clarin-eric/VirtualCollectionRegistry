package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.io.Serializable;
import java.util.Date;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

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
    private VirtualCollection.State state;
    private QueryOptions.Relation createdRelation;
    private Date created;

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

    public VirtualCollection.State getState() {
        return state;
    }

    public void setType(VirtualCollection.State state) {
        this.state = state;
    }

    public boolean hasState() {
        return state != null;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public String getDescriptionWithWildcard() {
        return addWildcards(description, descriptionMode);
    }

    public QueryOptions.Relation getCreatedRelation() {
        return createdRelation;
    }
    
    public void setCreatedRelation(QueryOptions.Relation createdRelation) {
        if (createdRelation == null) {
            throw new IllegalArgumentException("createdRelation == null");
        }
        this.createdRelation = createdRelation;
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

    public void clear() {
        nameMode = SearchMode.CONTAINS;
        name = null;
        descriptionMode = SearchMode.CONTAINS;
        description = null;
        type = null;
        state = null;
        createdRelation = QueryOptions.Relation.EQ;
        created = null;
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
