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
        
    @Override
    public String handleResponse(final HttpResponse response) throws IOException {
        processHeaders(response);
        processResponse(response);
        reversePidLookup();
        try {
            if (this.body != null) {
                //Let parsers do their work
                for (ReferenceParser parser : parsers) {
                    //Update and persist scan with current processor
                    scan.addResourceScanLog(parser.getId());
                    datastore.getEntityManager().merge(scan);

                    //Run parser
                    try {
                        if (parser.parse(this.body, this.mediaType)) {
                            ReferenceParserResult parserResult = parser.getResult();
                            scan.addResourceScanLogKV(parser.getId(), ReferenceParserResult.KEY_NAME, parserResult.get(ReferenceParserResult.KEY_NAME));
                            scan.addResourceScanLogKV(parser.getId(), ReferenceParserResult.KEY_DESCRIPTION, parserResult.get(ReferenceParserResult.KEY_DESCRIPTION));
                            break; //exit loop if the parser processed the reference
                        }    
                    } catch(Exception ex) {
                        scan.addResourceScanLogKV(parser.getId(), ReferenceParserResult.KEY_ERROR, ex.getMessage());
                    }
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
