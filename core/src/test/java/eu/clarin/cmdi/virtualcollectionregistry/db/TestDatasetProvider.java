package eu.clarin.cmdi.virtualcollectionregistry.db;

import eu.clarin.cmdi.virtualcollectionregistry.core.DataStore;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionDao;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionDaoImpl;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionFactory;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;

public class TestDatasetProvider {

    public final static String owner1Name = "test1";
    public final static String owner2Name = "test2";

    public final static int publicCollectionCount = 2;
    public final static int owner1PrivateCollectionCount = 2;
    public final static int owner2PrivateCollectionCount = 1;

    public void createDataset(DataStore ds) throws VirtualCollectionRegistryException {
        VirtualCollectionDao dao = new VirtualCollectionDaoImpl(ds);

        User owner1 = new User(owner1Name, "Display as tést owner 1");
        User owner2 = new User(owner2Name, "Display as tést owner 2");

        //private, owner1
        VirtualCollection vc1 =
            VirtualCollectionFactory
                .createNew(owner1, "Private Tést Collection 1", "This is the tést collection description")
                .getPersistedCollection(dao);

        //public, owner1
        VirtualCollection vc2 =
            VirtualCollectionFactory
                .createNew(owner1, "Public Tést Collection 2", "This is the tést collection description")
                .startPublish(dao)
                .finishPublish(dao)
                .getPersistedCollection(dao);

        //public, owner2
        VirtualCollection vc3 =
            VirtualCollectionFactory
                .createNew(owner2, "Public Tést Collection 3", "This is the tést collection description")
                .startPublish(dao)
                .finishPublish(dao)
                .getPersistedCollection(dao);

        //public, owner2
        VirtualCollection vc3_2 =
            VirtualCollectionFactory
                .createNewVersion(vc3)
                .startPublish(dao)
                .finishPublish(dao)
                .getPersistedCollection(dao);

        //private, owner2
        VirtualCollection vc3_3 =
            VirtualCollectionFactory
                .createNewVersion(vc3_2)
                .getPersistedCollection(dao);

    }
}
