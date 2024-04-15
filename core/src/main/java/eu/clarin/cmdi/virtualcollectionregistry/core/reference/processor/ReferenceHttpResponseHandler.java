/*
 * Copyright (C) 2024 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.core.reference.processor;

import eu.clarin.cmdi.virtualcollectionregistry.core.DataStore;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.ReferenceParser;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.ReferenceParserResult;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class ReferenceHttpResponseHandler implements ResponseHandler<String> {
    private final static Logger logger = LoggerFactory.getLogger(ReferenceHttpResponseHandler.class);
    
    public final static String PARSER_ID = "PARSER_HTTP";
     
    private final DataStore datastore;
    private final ResourceScan scan;
          
    private final List<ReferenceParser> parsers;

    private final String referenceUrl;
    private String body = null;
    private String mediaType = null;
    private Integer httpResponseCode;
    private String statusMessage = "";

    /*
    private String suggestedPid = null;
    private String suggestedName = null;
    private String suggestedDescription = null;
    */
            
    public ReferenceHttpResponseHandler(List<ReferenceParser> parsers, final DataStore datastore, ResourceScan scan) {
        this.parsers = parsers;
        this.datastore = datastore;
        this.scan = scan;
        this.referenceUrl = scan.getRef();
    }

    private void processHeaders(final HttpResponse response) {
        for (Header h : response.getHeaders("Content-Type")) {
            logger.trace(h.getName() + " - " + h.getValue());

            String[] parts = h.getValue().split(";");
            String mediaType = parts[0];

            logger.trace("Media-Type=" + mediaType);
            if (parts.length > 1) {
                String p = parts[1].trim();
                if (p.startsWith("charset=")) {
                    logger.trace("Charset=" + p.replaceAll("charset=", ""));
                } else if (p.startsWith("boundary=")) {
                    logger.trace("Boundary=" + p.replaceAll("boundary=", ""));
                }
            }

            this.mediaType = mediaType;
        }
    }
        
    private void processResponse(final HttpResponse response) throws IOException {
        int httpCode = response.getStatusLine().getStatusCode();
        String httpMessage = response.getStatusLine().getReasonPhrase();

        this.httpResponseCode = httpCode;
        this.statusMessage = "HTTP "+httpCode+" "+httpMessage;

        logger.trace("Http response: " + httpCode + " " + httpMessage);
        for (Header h : response.getHeaders("Content-Length")) {
            logger.trace(h.getName() + " - " + h.getValue());
        }

        if (httpCode >= 200 && httpCode < 300) {
            HttpEntity entity = response.getEntity();
            this.body = entity != null ? EntityUtils.toString(entity) : null;
        }
    }
    
    private void reversePidLookup() {
        if(this.scan.getRef().startsWith("http")) {
            //this.suggestedPid =
        }
    }
        
    protected static String getFilenameFromRef(String ref) {
        String[] parts = new String[]{""};
        try {
            URI uri = URI.create(ref);
            if(uri.getPath().isEmpty() || uri.getPath().equalsIgnoreCase("/")) {
                parts = new String[] {uri.getHost()};
            } else {
                parts = uri.getPath().split("/");            
            }
        } catch(Exception ex) {
            parts = ref.split("/");            
        }
        
        String filename = parts[0];
        if(parts.length > 1) {
            filename = parts[parts.length-1];
        }
        return filename;
    }
    
    @Override
    public String handleResponse(final HttpResponse response) throws IOException {        
        logger.debug("Running parser: {}", PARSER_ID);
        long t1 = System.nanoTime();
        scan.addResourceScanLog(PARSER_ID);
        //Process basic HTTP properties
        processHeaders(response);
        processResponse(response);
        reversePidLookup();
        //Add default name and http response values
        scan.addResourceScanLogKV(PARSER_ID, ReferenceParserResult.KEY_HTTP_RESPONSE_MEDIA_TYPE, mediaType);
        scan.addResourceScanLogKV(PARSER_ID, ReferenceParserResult.KEY_HTTP_RESPONSE_CODE, httpResponseCode.toString());
        scan.addResourceScanLogKV(PARSER_ID, ReferenceParserResult.KEY_NAME, getFilenameFromRef(scan.getRef()));
        scan.finishResourceScanLog(PARSER_ID);
        datastore.getEntityManager().merge(scan);
        long t2 = System.nanoTime();
        logger.debug("Finished parser: {} in {}ms", PARSER_ID, (t2-t1)/1000000);
                
        try {
            if (this.body != null) {               
                //Let parsers do their work
                for (ReferenceParser parser : parsers) {
                    t1 = System.nanoTime();
                    logger.debug("Running parser: {}", parser.getId());
                    scan.addResourceScanLog(parser.getId());
                    
                    try {
                        //TODO: we are always running all parsers, maybe stop after a successfull result?
                        //this would require synchronous processing.
                        parser.parse(this.body, this.mediaType);
                            ReferenceParserResult parserResult = parser.getResult();
                            scan.addResourceScanLogKV(parser.getId(), ReferenceParserResult.KEY_STATE, ReferenceParserResult.VALUE_STATE_OK);
                            scan.addResourceScanLogKV(parser.getId(), ReferenceParserResult.KEY_NAME, parserResult.get(ReferenceParserResult.KEY_NAME));
                            scan.addResourceScanLogKV(parser.getId(), ReferenceParserResult.KEY_DESCRIPTION, parserResult.get(ReferenceParserResult.KEY_DESCRIPTION));
                    } catch(Exception ex) {
                        scan.addResourceScanLogKV(parser.getId(), ReferenceParserResult.KEY_STATE, ReferenceParserResult.VALUE_STATE_ERROR);
                        scan.addResourceScanLogKV(parser.getId(), ReferenceParserResult.KEY_STATE_MSG, ex.getMessage());
                    }
        
                    scan.finishResourceScanLog(parser.getId());
                    datastore.getEntityManager().merge(scan);
                    t2 = System.nanoTime();
                    logger.debug("Finished parser: {} in {}ms", parser.getId(),(t2-t1)/1000000);
                }
            }

            //TODO: Set suggested PID if a suggestion is available

            //Update and persist scan with scan results
            scan.setMimeType(mediaType);
            scan.setHttpResponseCode(httpResponseCode);
            scan.setHttpResponseMessage(statusMessage);
            scan.setLastScanEnd(new Date());        
            datastore.getEntityManager().merge(scan);
        } catch(Exception ex) {
            //Handle any exception that was not properly handled.
            logger.error("Reference processing failed (stacktrace logged at debug level). Error: ", ex.getMessage());
            logger.debug("Exception: ", ex);
            scan.setLastScanEnd(new Date());        
            scan.setException(ex.getMessage());
            datastore.getEntityManager().merge(scan);            
            
        }
        return this.body;
    }    
}
