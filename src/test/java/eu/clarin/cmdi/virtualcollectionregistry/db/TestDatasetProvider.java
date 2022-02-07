package eu.clarin.cmdi.virtualcollectionregistry.db;

import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionFactory;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class TestDatasetProvider {

    public final static String owner1Name = "test1";
    public final static String owner2Name = "test2";

    public final static int publicCollectionCount = 2;
    public final static int owner1PrivateCollectionCount = 2;
    public final static int owner2PrivateCollectionCount = 1;

    private List<VirtualCollection> collections = new ArrayList<>();

    public void createDataset(EntityManager em) {
        try {
            em.getTransaction().begin();

            User owner1 = new User(owner1Name, "Display as tést owner 1");
            User owner2 = new User(owner2Name, "Display as tést owner 2");

            //private, owner1
            VirtualCollection vc1 =
                    VirtualCollectionFactory
                            .createNew(owner1, "Private Tést Collection 1", "This is the tést collection description")
                            .getCollection();
            collections.add(vc1);

            //public, owner1
            VirtualCollection vc2 =
                    VirtualCollectionFactory
                            .createNew(owner1, "Public Tést Collection 2", "This is the tést collection description")
                            .publish()
                            .getCollection();
            collections.add(vc2);

            //public, owner2
            VirtualCollection vc3 =
                    VirtualCollectionFactory
                            .createNew(owner2, "Public Tést Collection 3", "This is the tést collection description")
                            .publish()
                            .getCollection();
            collections.add(vc3);

            //public, owner2
            VirtualCollection vc3_2 = VirtualCollectionFactory.fromExisting(vc3).getNewCollectionVersion().publish().getCollection();
            collections.add(vc3_2);

            //private, owner2
            VirtualCollection vc3_3 = VirtualCollectionFactory.fromExisting(vc3_2).getNewCollectionVersion().getCollection();
            collections.add(vc3_3);

            //persist all collections
            for(VirtualCollection vc : collections) {
                em.persist(vc);
            }
        } finally {
            em.getTransaction().commit();
        }
    }
}
