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

/**
 *
 * @author wilelb
 */
public class ResourceScanRequest {
    private String ref;
    private String resolvedRef;
    private String sessionId;
    private boolean useCache;

    public ResourceScanRequest() {}
    
    public ResourceScanRequest(String ref, String resolvedRef, String sessionId, boolean useCache) {
        this.ref = ref;
        this.resolvedRef = resolvedRef;
        this.sessionId = sessionId;
        this.useCache = useCache;
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
     * @return the useCache
     */
    public boolean isUseCache() {
        return useCache;
    }

    /**
     * @param useCache the useCache to set
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }
}
