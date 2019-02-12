/*
 * Copyright (C) 2019 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.model;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import java.security.Principal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author wilelb
 */
public class VirtualCollectionBuilder {

    private final VirtualCollection vc;

    public VirtualCollectionBuilder() {
        this.vc = new VirtualCollection();
        this.vc.setCreationDate(new Date());
    }

    public VirtualCollection build() {
        return this.vc;
    }

    public VirtualCollectionBuilder setName(String name) throws VirtualCollectionRegistryUsageException {
        if (name == null) {
            throw new VirtualCollectionRegistryUsageException("No name specified for collection");
        }
        this.vc.setName(name);
        return this;
    }

    public VirtualCollectionBuilder setType(VirtualCollection.Type type) throws VirtualCollectionRegistryUsageException {
        if (type == null) {
            throw new VirtualCollectionRegistryUsageException("No type specified for collection");
        }
        this.vc.setType(type);
        return this;
    }

    public VirtualCollectionBuilder setDescription(String description) {
        this.vc.setDescription(description);
        return this;
    }

    public VirtualCollectionBuilder setPurpose(VirtualCollection.Purpose purpose) {
        this.vc.setPurpose(purpose);
        return this;
    }

    public VirtualCollectionBuilder setReproducibility(VirtualCollection.Reproducibility reproducability) {
        this.vc.setReproducibility(reproducability);
        return this;
    }

    public VirtualCollectionBuilder setCreationDate(Date creationDate) {
        if (creationDate != null) {        
            this.vc.setCreationDate(creationDate);
        }        
        return this;
    }

    public VirtualCollectionBuilder setReproducibilityNotice(String notice) {
        this.vc.setReproducibilityNotice(notice);
        return this;
    }

    public VirtualCollectionBuilder addKeyword(String keyword) {
        final String trimmed = keyword.trim();
        if (!trimmed.isEmpty()) {
            this.vc.getKeywords().add(trimmed);
        }
        return this;
    }
    
    public VirtualCollectionBuilder addKeywords(List<String> keywords) {
        for(String keyword : keywords) {
            final String trimmed = keyword.trim();
            if (!trimmed.isEmpty()) {
                this.vc.getKeywords().add(trimmed);
            }
        }
        return this;
    }
    
    public VirtualCollectionBuilder addCreator(Principal p) throws VirtualCollectionRegistryUsageException {
        if(p == null || p.getName() == null) {
            throw new VirtualCollectionRegistryUsageException("Cannot set creator from empty principal");
        }
        Creator c = new Creator(p.getName());
        this.vc.getCreators().add(c);
        return this;
    }
    
    public VirtualCollectionBuilder addCreator(String name) throws VirtualCollectionRegistryUsageException {
        if(name == null) {
            throw new VirtualCollectionRegistryUsageException("Cannot set creator from empty name");
        }
        Creator c = new Creator(name);
        this.vc.getCreators().add(c);
        return this;
    }
    
     public VirtualCollectionBuilder addCreator(Creator c) throws VirtualCollectionRegistryUsageException {
        if(c == null) {
            throw new VirtualCollectionRegistryUsageException("Cannot set creator from empty value");
        }
        this.vc.getCreators().add(c);
        return this;
    }
     
    /* Extensional values */
    
    public VirtualCollectionBuilder addMetadataResource(String uri) {
        this.vc.getResources().add(new Resource(Resource.Type.METADATA, uri));
        return this;
    }
    
    public VirtualCollectionBuilder addMetadataResources(List<String> metadataUris) {
        for(String uri : metadataUris) {
            this.vc.getResources().add(new Resource(Resource.Type.METADATA, uri));
        }
        return this;
    }
    
    public VirtualCollectionBuilder addResourceResource(String uri) {
        this.vc.getResources().add(new Resource(Resource.Type.RESOURCE, uri));
        return this;
    }

    public VirtualCollectionBuilder addResourceResources(List<String> resourceUris) {
        for(String uri : resourceUris) {
            this.vc.getResources().add(new Resource(Resource.Type.RESOURCE, uri));
        }
        return this;
    }
    
    /* Intensional values */
    
    public VirtualCollectionBuilder setIntenstionalQuery(String description, String uri, String profile, String value) throws VirtualCollectionRegistryUsageException {
        if(!isValid(description)) {
            throw new VirtualCollectionRegistryUsageException("Intensional description is reqiured");
        }
        if(!isValid(uri)) {
            throw new VirtualCollectionRegistryUsageException("Intensional uri is reqiured");
        }
        if(!isValid(profile)) {
            throw new VirtualCollectionRegistryUsageException("Intensional query profile is reqiured");
        }
        if(!isValid(value)) {
            throw new VirtualCollectionRegistryUsageException("Intensional query value is reqiured");
        }        
        GeneratedBy gen = new GeneratedBy();
        gen.setDescription(description);
        gen.setQuery(new GeneratedByQuery(profile, value));
        this.vc.setGeneratedBy(gen);
        return this;
    }
    
    private boolean isValid(String value) {
        return value != null && !value.isEmpty();
    }
        
}
