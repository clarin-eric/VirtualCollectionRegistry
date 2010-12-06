package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.io.Serializable;
import java.util.Date;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class FilterState implements Serializable {
    private String name;
    private VirtualCollection.Type type;
    private VirtualCollection.State state;
    private String description;
    private QueryOptions.Relation createdRelation = QueryOptions.Relation.EQ;
    private Date created;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasName() {
        return name != null;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean hasDescription() {
        return description != null;
    }

    public QueryOptions.Relation getCreatedRelation() {
        return createdRelation;
    }
    
    public void setCreatedRelation(QueryOptions.Relation createdRelation) {
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
        name = null;
        type = null;
        state = null;
        description = null;
        createdRelation = QueryOptions.Relation.EQ;
        created = null;
    }

} // class FilterState
