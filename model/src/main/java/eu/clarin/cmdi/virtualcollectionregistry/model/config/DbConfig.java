package eu.clarin.cmdi.virtualcollectionregistry.model.config;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "config")
@NamedQueries({
        @NamedQuery(name = "DbConfig.findByKey",
                query = "SELECT c FROM DbConfig c WHERE c.key = :keyName")
})
public class DbConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    public DbConfig() { }

    @Id
    @Column(name = "key", nullable = false, unique = true, length = 255)
    private String key;

    @Column(name = "value", nullable = true, unique = false, length = 255)
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
