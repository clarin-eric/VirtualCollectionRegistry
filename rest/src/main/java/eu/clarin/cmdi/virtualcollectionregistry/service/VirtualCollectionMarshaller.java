package eu.clarin.cmdi.virtualcollectionregistry.service;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollectionList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author twagoo
 */
public interface VirtualCollectionMarshaller {

    public static enum Format {
        XML, JSON, UNSUPPORTED
    } // public enum Format

    void marshal(OutputStream out, Format format, VirtualCollection vc) throws IOException;

    void marshal(OutputStream out, Format format, VirtualCollectionList vcs) throws IOException;

    void marshalAsCMDI(OutputStream out, Format format, VirtualCollection vc) throws IOException;

    VirtualCollection unmarshal(InputStream in, Format format, String encoding) throws IOException;

}
