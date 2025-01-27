package eu.clarin.cmdi.virtualcollectionregistry.core.reference.processor;

import eu.clarin.cmdi.virtualcollectionregistry.core.DataStore;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.CmdiReferenceParserImpl;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.DogReferenceParser;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.MscrReferenceParser;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.ReferenceParser;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.ParserConfig;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Reference processing does 2 things:
 * 
 * 1. validate the HTTP response
 * 
 * And if the response is valid:
 * 
 * 2. improve the manual input where possible
 *  2.1 suggest a PID if we know one exists
 *  2.2 try to process the response body in order to make suggestions on what
 *      values to use for the metadata we request (currently name and description)
 * 
 * @author Willem Elbers
 */
public class VirtualCollectionRegistryReferenceProcessor {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionRegistryReferenceProcessor.class);
    
    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private final RequestConfig requestConfig;
    private final Map<String, Boolean> skipList = new HashMap<>(); //In-memory list of reference with issues, to avoid looping over them continously
    
    private final List<ReferenceParser> parsers = new LinkedList<>();

    public VirtualCollectionRegistryReferenceProcessor(ParserConfig config) {
        this.requestConfig =
            RequestConfig
                .custom()
                .setConnectionRequestTimeout(1000)
                .setMaxRedirects(5)
                .build();
        
        //Parsers will be processing the reference in the order they are added
        //When getting a parser value, order of adding determines which value is used, first match wins.
        logger.info("isCmdiParserEnabled={}", config.isCmdiParserEnabled());
        if(config.isCmdiParserEnabled()) {
            parsers.add(new CmdiReferenceParserImpl());
        }
        logger.info("isDogParserEnabled={}", config.isDogParserEnabled());
        if(config.isDogParserEnabled()) {
            this.parsers.add(new DogReferenceParser());
        }
        logger.info("isMscrParserEnabled={}", config.isMscrParserEnabled());
        if(config.isMscrParserEnabled()) {
            parsers.add(new MscrReferenceParser(config));
        }
    }

    /**
     * Try to fetch the reference via HTTP and set it's state based on the response.
     *
     * TODO: currently this runs in one thread, so resource validation might impact creation of collections (collection
     * can only be saved  after validating all resources). Consider alternative approach with multiple threads doing the
     * work in the background.
     *
     * @param datastore 
     * @param scan 
     */
    public void doWork(final DataStore datastore, ResourceScan scan) {
        ReferenceHttpResponseHandler responseHandler = new ReferenceHttpResponseHandler(parsers, datastore, scan);
        String exceptionMsg = null;
        try {
            if(!skipList.containsKey(scan.getResolvedRef())) {
                logger.debug("Validating reference = "+scan.getRef()+", resolved to = "+scan.getResolvedRef());
                HttpGet httpget = new HttpGet(scan.getResolvedRef());
                httpget.setConfig(requestConfig);
                httpclient.execute(httpget, responseHandler);            
            } else {
                logger.warn("Skipped problematic (failed before) reference: "+scan.getResolvedRef()); 
            }
        } catch(Exception ex) {
            try {
                scan.setException(ex.getMessage());
                datastore.getEntityManager().merge(scan);
            } catch(Exception ex2) {
                logger.warn("Added "+scan.getResolvedRef()+" to the skiplist.", ex);
                skipList.put(scan.getResolvedRef(), Boolean.TRUE);
            }
        }
    }    
}