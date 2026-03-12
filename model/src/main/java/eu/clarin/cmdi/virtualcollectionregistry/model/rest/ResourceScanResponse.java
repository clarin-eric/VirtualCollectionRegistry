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

import java.util.Date;
import java.util.List;

/**
 *
 * @author wilelb
 */
public class ResourceScanResponse {
    private Long id;

    private String ref;

    private String resolvedRef;
    
    private String sessionId;

    private Date created;

    private Date lastScanStart;

    private Date lastScanEnd;

    private Integer httpResponseCode;

    private String httpResponseMessage;

    private String exception;

    private String mimeType;
        
    private List<ResourceScanLogResponse> logs;

    public ResourceScanResponse() {}
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * @param ref the ref to set
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * @return the resolvedRef
     */
    public String getResolvedRef() {
        return resolvedRef;
    }

    /**
     * @param resolvedRef the resolvedRef to set
     */
    public void setResolvedRef(String resolvedRef) {
        this.resolvedRef = resolvedRef;
    }

    /**
     * @return the sessionId
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId the sessionId to set
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param created the created to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * @return the lastScanStart
     */
    public Date getLastScanStart() {
        return lastScanStart;
    }

    /**
     * @param lastScanStart the lastScanStart to set
     */
    public void setLastScanStart(Date lastScanStart) {
        this.lastScanStart = lastScanStart;
    }

    /**
     * @return the lastScanEnd
     */
    public Date getLastScanEnd() {
        return lastScanEnd;
    }

    /**
     * @param lastScanEnd the lastScanEnd to set
     */
    public void setLastScanEnd(Date lastScanEnd) {
        this.lastScanEnd = lastScanEnd;
    }

    /**
     * @return the httpResponseCode
     */
    public Integer getHttpResponseCode() {
        return httpResponseCode;
    }

    /**
     * @param httpResponseCode the httpResponseCode to set
     */
    public void setHttpResponseCode(Integer httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    /**
     * @return the httpResponseMessage
     */
    public String getHttpResponseMessage() {
        return httpResponseMessage;
    }

    /**
     * @param httpResponseMessage the httpResponseMessage to set
     */
    public void setHttpResponseMessage(String httpResponseMessage) {
        this.httpResponseMessage = httpResponseMessage;
    }

    /**
     * @return the exception
     */
    public String getException() {
        return exception;
    }

    /**
     * @param exception the exception to set
     */
    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * @return the mimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return the logs
     */
    public List<ResourceScanLogResponse> getLogs() {
        return logs;
    }

    /**
     * @param logs the logs to set
     */
    public void setLogs(List<ResourceScanLogResponse> logs) {
        this.logs = logs;
    }
}
