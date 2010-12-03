package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.io.Serializable;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
final class FilterState implements Serializable {
    private String name;
    private VirtualCollection.Type type;
    private VirtualCollection.State state;
    private String description;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VirtualCollection.Type getType() {
        return type;
    }

    public void setType(VirtualCollection.Type type) {
        this.type = type;
    }

    public VirtualCollection.State getState() {
        return state;
    }

    public void setType(VirtualCollection.State state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEmpty() {
        return (name == null) && (type == null) && (state == null) &&
                (description == null);
    }

    public QueryOptions getQueryOptions(boolean privateMode) {
        QueryOptions options = new QueryOptions();
        QueryOptions.Filter and = options.and();
        if (privateMode) {
            ApplicationSession session = ApplicationSession.get();
            and.add(QueryOptions.Property.VC_OWNER,
                    QueryOptions.Relation.EQ, session.getUser());
        } else {
            and.add(QueryOptions.Property.VC_STATE,
                    QueryOptions.Relation.EQ,
                    VirtualCollection.State.PUBLIC);
        }

        if (name != null) {
            and.add(QueryOptions.Property.VC_NAME,
                    QueryOptions.Relation.EQ, name);
        }
        if (type != null) {
            and.add(QueryOptions.Property.VC_TYPE,
                    QueryOptions.Relation.EQ, type);
        }
        if (state != null) {
            and.add(QueryOptions.Property.VC_STATE,
                    QueryOptions.Relation.EQ, state);
        }
        if (description != null) {
            and.add(QueryOptions.Property.VC_DESCRIPTION,
                    QueryOptions.Relation.EQ, description);
        }
        options.setFilter(and);
        return options;
    }

    public String toString() {
        boolean first = true;
        StringBuilder sb = new StringBuilder("FilterState[");
        if (name != null) {
            sb.append("name=\"");
            sb.append(name);
            sb.append("\"");
            first = false;
        }
        if (type != null) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append("type=\"");
            sb.append(type);
            sb.append("\"");
        }
        if (state != null) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append("state=\"");
            sb.append(state);
            sb.append("\"");
        }
        if (description != null) {
            if (!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append("description=\"");
            sb.append(description);
            sb.append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

} // class FilterState
