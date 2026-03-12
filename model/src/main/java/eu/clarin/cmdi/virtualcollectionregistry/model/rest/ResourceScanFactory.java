/*
 * Copyright (C) 2026 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.model.rest;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScanLog;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScanLogKV;
import java.util.LinkedList;
import java.util.List;

/**
 * Factory to convert from ResourceScan into ResourceScanResponse and vice versa.
 * 
 * @author wilelb
 */
public class ResourceScanFactory {
    
    public static List<ResourceScanResponse> asResourceScanResponseList(List<ResourceScan> scans) {
        List<ResourceScanResponse> response = new LinkedList<>();
        for(ResourceScan scan : scans) {
            response.add(asResourceScanResponse(scan));
        }
        return response;        
    }
    
    public static List<ResourceScan> asResourceScanList(List<ResourceScanResponse> response) {
        List<ResourceScan> scans = new LinkedList<>();
        for(ResourceScanResponse scanResponse : response) {
            scans.add(asResourceScan(scanResponse));
        }
        return scans;
    }
    
    public static ResourceScanResponse asResourceScanResponse(ResourceScan scan) {
        ResourceScanResponse r = new ResourceScanResponse();
        r.setId(scan.getId());
        r.setRef(scan.getRef());
        r.setResolvedRef(scan.getResolvedRef());
        r.setSessionId(scan.getSessionId());
        r.setCreated(scan.getCreated());
        r.setLastScanStart(scan.getLastScan());
        r.setLastScanEnd(scan.getLastScanEnd());
        r.setHttpResponseCode(scan.getHttpResponseCode());
        r.setHttpResponseMessage(scan.getHttpResponseMessage());
        r.setException(scan.getException());
        r.setMimeType(scan.getMimeType());
        List<ResourceScanLogResponse> logs = new LinkedList<>();
        for(ResourceScanLog l : scan.getLogs()) {
            ResourceScanLogResponse rScanLog = new ResourceScanLogResponse();
            rScanLog.setId(l.getId());
            rScanLog.setProcessorId(l.getProcessorId());
            rScanLog.setStart(l.getStart());
            rScanLog.setEnd(l.getEnd());
            List<ResourceScanLogKVResponse> kvs = new LinkedList<>();
            for(ResourceScanLogKV kv : l.getKvs()) {
                kvs.add(new ResourceScanLogKVResponse(kv.getId(), kv.getKey(), kv.getValue()));
            }
            rScanLog.setKvs(kvs);
            logs.add(rScanLog);
        }
        r.setLogs(logs);
        return r;
    }
    
    public static ResourceScan asResourceScan(ResourceScanResponse response) {
        ResourceScan scan = new ResourceScan();
        scan.setId(response.getId());
        scan.setRef(response.getRef());
        scan.setResolvedRef(response.getResolvedRef());
        scan.setSessionId(response.getSessionId());
        scan.setCreated(response.getCreated());
        scan.setLastScan(response.getLastScanStart());
        scan.setLastScanEnd(response.getLastScanEnd());
        scan.setHttpResponseCode(response.getHttpResponseCode());
        scan.setHttpResponseMessage(response.getHttpResponseMessage());
        scan.setException(response.getException());
        scan.setMimeType(response.getMimeType());
        List<ResourceScanLog> logs = new LinkedList<>();
        for(ResourceScanLogResponse l : response.getLogs()) {
            ResourceScanLog log = new ResourceScanLog();
            log.setScan(scan);
            log.setStart(l.getStart());
            log.setEnd(l.getEnd());
            log.setProcessorId(l.getProcessorId());           
            List<ResourceScanLogKV> kvs = new LinkedList<>();
            for(ResourceScanLogKVResponse kv : l.getKvs()) {
                kvs.add(new ResourceScanLogKV(log, kv.getId(), kv.getKey(), kv.getValue()));
            }
            log.setKvs(kvs);
            logs.add(log);
        }
        scan.setLogs(logs);
        return scan;
    }
}
