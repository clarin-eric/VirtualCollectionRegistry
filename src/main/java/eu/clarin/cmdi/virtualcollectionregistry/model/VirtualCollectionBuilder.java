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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission.SubmissionUtils;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class VirtualCollectionBuilder {
    private static Logger logger = LoggerFactory.getLogger(VirtualCollectionBuilder.class);
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    private final VirtualCollection vc;

    public VirtualCollectionBuilder() {
        this.vc = new VirtualCollection();
        this.vc.setCreationDate(new Date());
    }

    public VirtualCollectionBuilder(VirtualCollection vc) {
        this.vc = vc;
    }
    
    public VirtualCollection build() {
        return this.vc;
    }

    public VirtualCollectionBuilder setOwner(Principal p) throws VirtualCollectionRegistryUsageException {
        if (p != null && p.getName() != null) {
            this.vc.setOwner(new User(p.getName()));
        }
        return this;
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
        if(p != null && p.getName() != null) {
            Creator c = new Creator(p.getName());
            this.vc.getCreators().add(c);
        }
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
    
     public class ResourceInput {
         private String uri;
         private String description;
         private String label;

        /**
         * @return the uri
         */
        public String getUri() {
            return uri;
        }

        /**
         * @param uri the uri to set
         */
        public void setUri(String uri) {
            this.uri = uri;
        }

        /**
         * @return the Description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param Description the Description to set
         */
        public void setDescription(String description) {
            this.description = description;
        }

        /**
         * @return the label
         */
        public String getLabel() {
            return label;
        }

        /**
         * @param label the label to set
         */
        public void setLabel(String label) {
            this.label = label;
        }
     }
     
     private void addResource(Resource.Type type, String uri) {
         try {
                ResourceInput input = mapper.readValue(uri, ResourceInput.class);
                Resource r = new Resource(type, input.getUri());
                r.setDescription(input.getDescription());
                r.setLabel(input.label);
                this.vc.getResources().add(r);
            } catch(IOException ex) {
                logger.debug("Failed to unmarshal resource json: "+ex.getMessage()+", falling back to add as plain url with value="+uri);
                this.vc.getResources().add(new Resource(type, uri));
            }
     }
     
    public VirtualCollectionBuilder addMetadataResource(String uri) {
        this.addResource(Resource.Type.METADATA, uri);
        return this;
    }
    
    public VirtualCollectionBuilder addMetadataResources(List<String> metadataUris) {
        for(String uri : metadataUris) {
            addMetadataResource(uri);
        }
        return this;
    }
    
    public VirtualCollectionBuilder addResourceResource(String uri) {
        this.addResource(Resource.Type.RESOURCE, uri);
        return this;
    }

    public VirtualCollectionBuilder addResourceResources(List<String> resourceUris) {
        for(String uri : resourceUris) {
            this.addResourceResource(uri);
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
