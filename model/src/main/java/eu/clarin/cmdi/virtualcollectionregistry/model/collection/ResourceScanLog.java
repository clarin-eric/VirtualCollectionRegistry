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
package eu.clarin.cmdi.virtualcollectionregistry.model.collection;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author wilelb
 */
@Entity
@Table(name = "resource_scan_log")
public class ResourceScanLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "processor", nullable = false, length = 255)
    private String processorId;

    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.EAGER,
               mappedBy = "scanLog")
    private Set<ResourceScanLogKV> kvs;
    
    @ManyToOne(cascade = CascadeType.ALL,
          fetch = FetchType.LAZY,
          optional = true)
    @JoinColumn(name = "scan_id",
            nullable = false,
            unique = false)
    private ResourceScan scan;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start", nullable = true)
    private Date start;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end", nullable = true)
    private Date end;
       
    public ResourceScanLog() {}
    
    public ResourceScanLog(ResourceScan scan, String processorId) {
        this.scan = scan;
        this.processorId = processorId;
        this.start = new Date();
    }
    
    public void finish() {
        this.setEnd(new Date());
    }
    
    /**
     * @return the id
     */
    public Long getId() {
        return id;
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
     * @return the kvs
     */
    public Set<ResourceScanLogKV> getKvs() {
        return kvs;
    }

    /**
     * @param kvs the kvs to set
     */
    public void setKvs(Set<ResourceScanLogKV> kvs) {
        this.kvs = kvs;
    }

    /**
     * @return the scan
     */
    public ResourceScan getScan() {
        return scan;
    }

    /**
     * @param scan the scan to set
     */
    public void setScan(ResourceScan scan) {
        this.scan = scan;
    }
    
    /**
     * Add a key value pair.
     * 
     * @param key
     * @param value 
     */
    public void addKV(String key, String value) {
        if(value != null) {
            ResourceScanLogKV kv = new ResourceScanLogKV();
            kv.setKey(key);
            kv.setValue(value);
            kv.setScanLog(this);
            if(kvs == null) {
                kvs = new HashSet();
            }
            kvs.add(kv);
        }
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
}
