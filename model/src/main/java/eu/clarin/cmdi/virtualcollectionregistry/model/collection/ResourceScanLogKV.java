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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Scan log entry key value pair.
 *  
 * @author wilelb
 */
@Entity
@Table(name = "resource_scan_log_kv")
public class ResourceScanLogKV implements Serializable, Comparable<ResourceScanLogKV> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "k", nullable = false, length = 255)
    private String key;

    @Column(name = "v", nullable = false)
    private String value;
    
    @ManyToOne(cascade = CascadeType.ALL,
          fetch = FetchType.LAZY,
          optional = true)
    @JoinColumn(name = "scan_log_id",
            nullable = false,
            unique = false)
    private ResourceScanLog scanLog;
     
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the scan
     */
    public ResourceScanLog getScanLog() {
        return scanLog;
    }

    /**
     * @param scan the scan to set
     */
    public void setScanLog(ResourceScanLog scan) {
        this.scanLog = scan;
    }

    @Override
    public int compareTo(ResourceScanLogKV o) {
        return getId().compareTo(o.getId());
    }
    
    
}
