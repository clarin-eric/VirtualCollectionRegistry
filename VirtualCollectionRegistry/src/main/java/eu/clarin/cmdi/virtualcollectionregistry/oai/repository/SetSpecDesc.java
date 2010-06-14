/**
 * 
 */
package eu.clarin.cmdi.virtualcollectionregistry.oai.repository;

public final class SetSpecDesc {
    private final String id;
    private final String name;
    private final String description;

    public SetSpecDesc(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public SetSpecDesc(String id, String name) {
        this(id, name, null);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

} // class SetSpecDesc
