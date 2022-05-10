package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.config.VcrConfig;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references.ReferencesEditor;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.ResourceScan;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class VirtualCollectionRegistryReferenceValidatorImpl extends TxManager implements VirtualCollectionRegistryReferenceValidator, InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionRegistryReferenceValidatorImpl.class);

    private final transient List<VirtualCollectionRegistryReferenceValidatorWorker> workers = new CopyOnWriteArrayList<>();

    @Autowired
    private VcrConfig vcrConfig;

    private boolean running = false;

    public VirtualCollectionRegistryReferenceValidatorImpl() { }

    // called by Spring directly after Bean construction
    @Override
    public void afterPropertiesSet() { }

    @Override
    public void shutdown() { }

    @Override
    public synchronized void perform(long now, DataStore datastore) throws VirtualCollectionRegistryException {
        try {
            if(!running) {
                running = true;

                //Fetch and process list of work items
                List<ResourceScan> scans = fetchListOfWork(datastore.getEntityManager());
                if(scans.size() > 0) {
                    logger.debug("Found {} resource scan(s) requiring analysing.", scans.size());
                }
                try {
                    beginTransaction(datastore.getEntityManager());

                    for (ResourceScan scan : scans) {
                        logger.trace("Marking start for scan with ref = {}", scan.getRef());
                        markScanStarted(datastore.getEntityManager(), scan);
                        logger.trace("Starting work for scan with ref = {}", scan.getRef());
                        VirtualCollectionRegistryReferenceValidatorWorker.WorkerResult result =
                                new VirtualCollectionRegistryReferenceValidatorWorker().doWork(scan.getRef());

                        //Update and persist scan
                        scan.setMimeType(result.getMimeType());
                        scan.setHttpResponseCode(result.getHttpResponseCode());
                        scan.setHttpResponseMessage(result.getHttpResponseMsg());
                        scan.setLastScanEnd(new Date());
                        scan.setException(result.getException());
                        scan.setNameSuggestion(result.getNameSuggestion());
                        scan.setDescriptionSuggestion(result.getDescriptionSuggestion());
                        datastore.getEntityManager().merge(scan);

                        logger.info("Finished resource scan for ref = {} in {}s, http response = {}", scan.getRef(), scan.getDurationInSeconds(), scan.getHttpResponseCode());
                    }
                } catch(Exception ex) {
                    rollbackActiveTransaction(datastore.getEntityManager(), "Failed to update resource scan", ex);
                } finally {
                    commitActiveTransaction(datastore.getEntityManager());
                }
            }
        } catch (Exception ex) {
            logger.info("Perform failed", ex);
        } finally {
            running = false;
        }
    }

    /**
     * Fetch list of resources that require scanning from the database.
     *
     * @param em
     * @return
     * @throws VirtualCollectionRegistryException
     */
    private synchronized List<ResourceScan>  fetchListOfWork(final EntityManager em) throws VirtualCollectionRegistryException {
        List<ResourceScan> scans = new ArrayList<>();
        try {
            beginTransaction(em);
            TypedQuery<ResourceScan> q = em.createNamedQuery("ResourceScan.findScanRequired", ResourceScan.class);
            scans = q.getResultList();
        } catch (Exception ex) {
            rollbackActiveTransaction(em, "Failed to fetch scan resources", ex);
        } finally {
            commitActiveTransaction(em);
        }
        return scans;
    }

    /**
     * Update the properties of the ResourceScan to mark it as being analysed to prevent other workers of processing this
     * item.
     *
     * @param em
     * @param scan
     */
    private synchronized void markScanStarted(final EntityManager em, ResourceScan scan) {//throws VirtualCollectionRegistryException {
        beginTransaction(em);
        scan.setLastScan(new Date());
        scan.setLastScanEnd(null);
        em.merge(scan);
        commitActiveTransaction(em);
    }
}