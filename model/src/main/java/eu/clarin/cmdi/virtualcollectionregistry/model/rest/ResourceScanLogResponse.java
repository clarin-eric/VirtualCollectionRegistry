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
public class ResourceScanLogResponse {
    private Long id;

    private String processorId;

    private Date start;

    private Date end;
    
    private List<ResourceScanLogKVResponse> kvs;

    public ResourceScanLogResponse() {}
    
    public ResourceScanLogResponse(Long id, String processorId, Date start, Date end, List<ResourceScanLogKVResponse> kvs) {
        this.id = id;
        this.processorId = processorId;
        this.start = start;
        this.end = end;
        this.kvs = kvs;
    }
    
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
     * @return the processorId
     */
    public String getProcessorId() {
        return processorId;
    }

    /**
     * @param processorId the processorId to set
     */
    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    /**
     * @return the start
     */
    public Date getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(Date start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public Date getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(Date end) {
        this.end = end;
    }

    /**
     * @return the kvs
     */
    public List<ResourceScanLogKVResponse> getKvs() {
        return kvs;
    }

    /**
     * @param kvs the kvs to set
     */
    public void setKvs(List<ResourceScanLogKVResponse> kvs) {
        this.kvs = kvs;
    }
}
