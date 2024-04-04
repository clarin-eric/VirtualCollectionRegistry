package eu.clarin.cmdi.virtualcollectionregistry.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.GeneratedByQuery;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class VirtualCollectionFactory implements Serializable  {

    public final static Long FORKED_ID = -1L;
    public final static Long SUBMITTED_ID = -2L;

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionFactory.class);

    private final ObjectMapper mapper = new ObjectMapper();

    public static VirtualCollectionFactory createNew(User owner) {
        return new VirtualCollectionFactory(owner);
    }

    /**
     * Initialize a virtual collection factory for a new collection. Some basic fields will be set. All other fields can
     * be modified via the factory methods.
     *
     * @param owner The owner for this collection
     * @param name The name for this collection
     * @param description A description for this collection
     * @return
     */
    public static VirtualCollectionFactory createNew(User owner, String name, String description) {
        return new VirtualCollectionFactory(owner, name, description);
    }

    /**
     * Initialize a virtual collection factory with a fork based on the supplied virtual collection. The supplied owner
     * will be set as the new forked collection owner.
     *
     * @param vc
     * @param newOwner
     * @return
     */
    public static VirtualCollectionFactory createFork(VirtualCollection vc, User newOwner) {
        VirtualCollection forked_vc = vc.fork(newOwner);
        forked_vc.setId(FORKED_ID);
        return new VirtualCollectionFactory(forked_vc);
    }

    public static VirtualCollectionFactory createSubmission(User owner) {
        VirtualCollection vc = createNew(owner).getCollection();
        vc.setId(SUBMITTED_ID);
        return new VirtualCollectionFactory(vc);
    }

    /**
     * Initialize a virtual collection factory with a new collection version with the supplied collection as its parent
     *
     * @param vc The new virtual collection parent
     * @return
     */
    public static VirtualCollectionFactory createNewVersion(VirtualCollection vc) {
        if(vc.getState() != VirtualCollection.State.PUBLIC_PENDING && vc.getState() != VirtualCollection.State.PUBLIC &&
                vc.getState() != VirtualCollection.State.PUBLIC_FROZEN_PENDING && vc.getState() != VirtualCollection.State.PUBLIC_FROZEN ) {
            throw new IllegalStateException("Cannot create a new version from a non public collection (state="+vc.getState().toString()+")");
        }
        VirtualCollection new_version = vc.clone(false);
        new_version.setState(VirtualCollection.State.PRIVATE);
        new_version.setParent(vc);
        new_version.setRoot(vc.getRoot());

        vc.setChild(new_version); //TODO: how to ensure vc is properly persisted?
        return new VirtualCollectionFactory(new_version);
    }

    public static VirtualCollectionFactory cloneWithId(VirtualCollection vc) {
        VirtualCollection new_vc = vc.clone(true);
        return new VirtualCollectionFactory(new_vc);
    }

    public static VirtualCollectionFactory cloneWithoutId(VirtualCollection vc) {
        VirtualCollection new_vc = vc.clone(false);
        return new VirtualCollectionFactory(new_vc);
    }

    /**
     * Initialize a virtual collection factory to modify the supplied existing collection (by reference).
     * This method can be used to edit existing collections.
     *
     * @param vc The collection to edit
     * @return
     */
    public static VirtualCollectionFactory fromExisting(VirtualCollection vc) {
        return new VirtualCollectionFactory(vc);
    }

    private VirtualCollection c;
    private boolean dirty = false;
    private boolean forked = false;

    private VirtualCollectionFactory() {
        Date now = new Date();
        c = new VirtualCollection();

        c.setProblem(null);
        c.setProblemDetails(null);

        c.setOrigin(null);
        c.setState(VirtualCollection.State.PRIVATE);
        c.setPublicLeaf(false);
        c.setType(VirtualCollection.Type.EXTENSIONAL);
        c.getCreators();
        c.setPurpose(VirtualCollection.Purpose.REFERENCE);
        c.setReproducibility(VirtualCollection.Reproducibility.INTENDED);
        c.setReproducibilityNotice(null);
        c.getKeywords();
        c.getResources();
        c.setGeneratedBy(null);

        c.setCreationDate(now);
        c.setDateModified(now);
        c.setDatePublished(null);

        c.setForkedFrom(null);
        c.setParent(null);
        c.setChild(null);
        c.setRoot(null);
        dirty = true;
    }

    private VirtualCollectionFactory(User owner) {
       this();
        c.setOwner(owner);
    }

    private VirtualCollectionFactory(User owner, String name, String description) {
        this(owner);
        c.setName(name);
        c.setDescription(description);
        dirty = true;
    }

    private VirtualCollectionFactory(VirtualCollection vc) {
        this.c = vc;
        if(c.getId() == null || c.getId() == FORKED_ID) {
            dirty = true;
        }
    }

    public VirtualCollectionFactory persist(VirtualCollectionDao dao) throws VirtualCollectionRegistryException {
        //if(c.getId() == FORKED_ID) {
        //    c.setId(null);
        //}
        dao.persist(c);
        dirty = false;
        return this;
    }

    public VirtualCollection getPersistedCollection(VirtualCollectionDao dao) throws VirtualCollectionRegistryException  {
        if(dirty) {
            persist(dao);
        }
        return getCollection();
    }

    public VirtualCollection getCollection() {
        /*
        if(c.getId() == FORKED_ID) {
            c.setId(null);
        }*/
        return this.c;
    }

    public boolean isDirty() {
        return dirty;
    }

    public VirtualCollectionFactory startPublish(VirtualCollectionDao dao) throws VirtualCollectionRegistryException {
        c.setState(VirtualCollection.State.PUBLIC_PENDING);
        persist(dao);
        return this;
    }

    public VirtualCollectionFactory finishPublish(VirtualCollectionDao dao) throws VirtualCollectionRegistryException {
        c.setState(VirtualCollection.State.PUBLIC);
        c.setPublicLeaf(true);
        if(c.getParent() != null) {
            c.getParent().setPublicLeaf(false);
        }
        persist(dao);
        return this;
    }

    public boolean isNewVersion() {
        return c.getId() == null && c.getParent() != null;
    }

    public boolean isEditing() {
        return c.getId() != null ||  isNewVersion();
    }

    public VirtualCollectionFactory setName(String name) {
        c.setName(name);
        return this;
    }

    public VirtualCollectionFactory setDescription(String description) {
        c.setDescription(description);
        return this;
    }

    public VirtualCollectionFactory setReproducibility(VirtualCollection.Reproducibility reproducibility, String reproducibilityNotice) {
        c.setReproducibility(reproducibility);
        c.setReproducibilityNotice(reproducibilityNotice);
        dirty = true;
        return this;
    }

    public VirtualCollectionFactory setOrigin(String origin) {
        c.setOrigin(origin);
        dirty = true;
        return this;
    }

    public VirtualCollectionFactory setType(VirtualCollection.Type t) {
        c.setType(t);
        dirty = true;
        return this;
    }

    public VirtualCollectionFactory setPurpose(VirtualCollection.Purpose p) {
        c.setPurpose(p);
        dirty = true;
        return this;
    }

    public VirtualCollectionFactory addCreator(Creator e) {
        if(e == null) {
            throw new NullPointerException("Creator cannot be null");
        }
        c.addCreator(e);
        dirty = true;
        return this;
    }

   public VirtualCollectionFactory addAllCreators(List<Creator> creators) {
        for(Creator creator: creators) {
            addCreator(creator);
        }
        dirty = true;
        return this;
    }

    public VirtualCollectionFactory addKeyword(String k) {
        if(k == null) {
            throw new NullPointerException("Keyword cannot be null");
        }
        c.addKeyword(k);
        dirty = true;
        return this;
    }

    public VirtualCollectionFactory addAllKeywords(List<String> keywords) {
        for(String keyword: keywords) {
            addKeyword(keyword);
        }
        dirty = true;
        return this;
    }

    public VirtualCollectionFactory addResource(Resource r) {
        if(r == null) {
            throw new NullPointerException("Resource cannot be null");
        }
        c.addResource(r);
        dirty = true;
        return this;
    }

    public VirtualCollectionFactory addResource(Resource.Type type, String uri, String originUrl, String originalQuery) {
        try {
            ResourceInput input = mapper.readValue(uri, ResourceInput.class);
            logger.debug("Resource input: "+uri+", adding as JSON");
            Resource r = new Resource(type, input.getUri());
            r.setDescription(input.getDescription());
            r.setLabel(input.getLabel());
            r.setOrigin(originUrl);
            r.setOriginalQuery(originalQuery);
            addResource(r);
        } catch(IOException ex) {
            logger.debug("Resource input: "+uri+", adding as url");
            Resource r = new Resource(type, uri);
            r.setOrigin(originUrl);
            r.setOriginalQuery(originalQuery);
            addResource(r);
        }
        return this;
    }

    public VirtualCollectionFactory addAllResources(List<Resource> resources) {
        for(Resource r: resources) {
            addResource(r);
        }
        dirty = true;
        return this;
    }

    public VirtualCollectionFactory setGeneratedBy(GeneratedBy g) {
        c.setGeneratedBy(g);
        dirty = true;
        return this;
    }

    public VirtualCollectionFactory setGeneratedBy(String description, String uri, String profile, String value) throws VirtualCollectionRegistryUsageException {
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
        return setGeneratedBy(gen);
    }

    private boolean isValid(String value) {
        return value != null && !value.isEmpty();
    }

    public VirtualCollectionFactory setState(VirtualCollection.State state) {
        if(state == null) {
            throw new NullPointerException("State cannot be null");
        }
        c.setState(state);
        dirty = true;
        return this;
    }










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
}
