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
        logger.debug("Build virtual collection");
        return this.vc;
    }

    public VirtualCollectionBuilder setOwner(Principal p) throws VirtualCollectionRegistryUsageException {
        if (p != null && p.getName() != null) {
            this.vc.setOwner(new User(p.getName()));
        }
        return this;
    }

    public VirtualCollectionBuilder setOrigin(String origin) throws VirtualCollectionRegistryUsageException {
        logger.info("Setting origin to: {}", origin);
        if(origin.isEmpty()) {
            this.vc.setOrigin(null);
        } else {
            this.vc.setOrigin(origin);
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
            Creator c = new Creator(p.getName(), "");
            this.vc.getCreators().add(c);
        }
        return this;
    }
    
    public VirtualCollectionBuilder addCreator(String familyName, String givenName) throws VirtualCollectionRegistryUsageException {
        if(familyName == null) {
            throw new VirtualCollectionRegistryUsageException("Cannot set creator from empty familyName");
        }
        if(givenName == null) {
            throw new VirtualCollectionRegistryUsageException("Cannot set creator from empty givenName");
        }
        Creator c = new Creator(familyName, givenName);
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
    
     public  static class ResourceInput {
         private String uri;
         private String description;
         private String label;;

        public String getUri() {
            return uri;
        }
        public void setUri(String uri) {
            this.uri = uri;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public String getLabel() {
            return label;
        }
        public void setLabel(String label) {
            this.label = label;
        }
     }
     
     private void addResource(Resource.Type type, String uri, String originalQuery) {
         logger.debug("Resource input: "+uri);
         try {
                ResourceInput input = mapper.readValue(uri, ResourceInput.class);
                logger.debug("Parsed JSON input: uri="+input.getUri()+
                        ", label="+input.getLabel()+
                        ", description="+input.getDescription());
                Resource r = new Resource(type, input.getUri());
                r.setOriginalQuery(originalQuery);
                r.setDescription(input.getDescription());
                r.setLabel(input.label);
                r.setOrigin(this.vc.getOrigin());
                this.vc.getResources().add(r);
            } catch(IOException ex) {
                logger.debug("Failed to unmarshal resource json: "+ex.getMessage()+", falling back to add as plain url with value="+uri);
                Resource r = new Resource(type, uri);
                r.setOrigin(this.vc.getOrigin());
                this.vc.getResources().add(r);
            }
     }
     
    public VirtualCollectionBuilder addMetadataResource(String uri, String originalQuery) {
        this.addResource(Resource.Type.METADATA, uri, originalQuery);
        return this;
    }
    
    public VirtualCollectionBuilder addMetadataResources(List<String> metadataUris, String originalQuery) {
        for(String uri : metadataUris) {
            addMetadataResource(uri, originalQuery);
        }
        return this;
    }
    
    public VirtualCollectionBuilder addResourceResource(String uri, String originalQuery) {
        this.addResource(Resource.Type.RESOURCE, uri, originalQuery);
        return this;
    }

    public VirtualCollectionBuilder addResourceResources(List<String> resourceUris, String originalQuery) {
        for(String uri : resourceUris) {
            this.addResourceResource(uri, originalQuery);
        }
        return this;
    }
    
    /* Intensional values */
    
    public VirtualCollectionBuilder setIntenstionalQuery(String description, String uri, String profile, String value) throws VirtualCollectionRegistryUsageException {
        if(!isValid(description)) {
            throw new VirtualCollectionRegistryUsageException("Intensional description is required");
        }
        if(!isValid(uri)) {
            throw new VirtualCollectionRegistryUsageException("Intensional uri is required");
        }
        if(!isValid(profile)) {
            throw new VirtualCollectionRegistryUsageException("Intensional query profile is required");
        }
        if(!isValid(value)) {
            throw new VirtualCollectionRegistryUsageException("Intensional query value is required");
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
