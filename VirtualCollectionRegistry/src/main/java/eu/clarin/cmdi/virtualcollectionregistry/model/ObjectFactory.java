package eu.clarin.cmdi.virtualcollectionregistry.model;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    private static ObjectFactory s_instance = new ObjectFactory();

    private ObjectFactory() {
        super();
    }

    public static ObjectFactory instance() {
        return s_instance;
    }

    public VirtualCollection createVirtualCollection() {
        return new VirtualCollection();
    }

} // class ObjectFactory
