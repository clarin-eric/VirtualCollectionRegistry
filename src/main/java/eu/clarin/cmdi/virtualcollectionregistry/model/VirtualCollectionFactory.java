package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.util.Date;

public class VirtualCollectionFactory {

    public static VirtualCollectionFactory createNew(User owner, String name, String description) {
        return new VirtualCollectionFactory(owner, name, description);
    }

    public static VirtualCollectionFactory fromExisting(VirtualCollection vc) {
        return new VirtualCollectionFactory(vc);
    }


    private VirtualCollection c;

    private VirtualCollectionFactory(User owner, String name, String description) {
        Date now = new Date();
        c = new VirtualCollection();
        c.setRoot(c);
        c.setOwner(owner);
        c.setProblem(null);
        c.setProblemDetails(null);
        c.setOrigin(null);
        c.setState(VirtualCollection.State.PRIVATE);
        c.setType(VirtualCollection.Type.EXTENSIONAL);
        c.setName(name);
        c.setDescription(description);
        c.getCreators();//.add();
        c.setPurpose(VirtualCollection.Purpose.REFERENCE);
        c.setReproducibility(VirtualCollection.Reproducibility.INTENDED);
        c.setReproducibilityNotice(null);
        c.getKeywords();//.add();
        c.getResources();//.add();
        c.setGeneratedBy(null);

        c.setCreationDate(now);
        c.setDateModified(now);
        c.setDatePublished(null);

        c.setForkedFrom(null);
        c.setParent(null);
        c.setChild(null);
        c.setRoot(null);
    }

    private VirtualCollectionFactory(VirtualCollection vc) {
        this.c = vc;
    }

    public VirtualCollection getCollection() {
        return this.c;
    }

    public VirtualCollectionFactory getNewCollectionFork(User newOwner) {
        return fromExisting(c.fork(newOwner));
    }

    public VirtualCollectionFactory getNewCollectionVersion() {
        if(c.getState() != VirtualCollection.State.PUBLIC && c.getState() != VirtualCollection.State.PUBLIC_FROZEN ) {
            throw new IllegalStateException("Cannot create a new version from a non public collection");
        }
        VirtualCollection newVersion = c.clone();
        newVersion.setState(VirtualCollection.State.PRIVATE);
        newVersion.setParent(c);
        newVersion.setRoot(c.getRoot());
        return fromExisting(newVersion);
    }

    public VirtualCollectionFactory setReproducibility(VirtualCollection.Reproducibility reproducibility, String reproducibilityNotice) {
        c.setReproducibility(reproducibility);
        c.setReproducibilityNotice(reproducibilityNotice);
        return this;
    }

    public VirtualCollectionFactory publish() {
        c.getIdentifiers();//.add();
        c.setState(VirtualCollection.State.PUBLIC);
        return this;
    }

}
