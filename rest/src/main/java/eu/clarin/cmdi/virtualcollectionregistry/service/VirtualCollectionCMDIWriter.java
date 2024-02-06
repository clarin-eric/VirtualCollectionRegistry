package eu.clarin.cmdi.virtualcollectionregistry.service;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * @author twagoo
 */
public interface VirtualCollectionCMDIWriter {

    void writeCMDI(XMLStreamWriter out, VirtualCollection vc) throws XMLStreamException;

}
