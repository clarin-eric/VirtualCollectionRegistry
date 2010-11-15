package eu.clarin.cmdi.virtualcollectionregistry.gui.auth;

import java.security.Principal;
import java.util.Iterator;
import java.util.Map;

public final class AuthPrincipal implements Principal {
    private final String name;
    private final Map<String, String> attributes;
    
    AuthPrincipal(String name, Map<String, String> attributes) {
        if ((name == null) || name.isEmpty()) {
            throw new IllegalArgumentException("name is invalid");
        }
        this.name = name;
        if ((attributes != null) && !attributes.isEmpty()) {
            this.attributes = attributes;
        } else {
            this.attributes = null;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public Iterator<String> getAttributeNames() {
        if (attributes != null) {
            return attributes.keySet().iterator();
        }
        return null;
    }
    
    public String getAttibute(String name) {
        if ((attributes != null) && (name != null)) {
            return attributes.get(name.toLowerCase());
        }
        return null;
    }

} // class AuthPrincipal
