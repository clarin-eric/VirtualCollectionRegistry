package eu.clarin.cmdi.virtualcollectionregistry.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "resource_scan")
@NamedQueries({
        @NamedQuery(name = "ResourceScan.findByRef",
                query = "SELECT r FROM ResourceScan r WHERE r.ref = :ref"),
        @NamedQuery(name = "ResourceScan.findByRefs",
                query = "SELECT r FROM ResourceScan r WHERE r.ref IN :refs"),
        @NamedQuery(name = "ResourceScan.findScanRequired",
                query = "SELECT r FROM ResourceScan r WHERE r.lastScanStart IS NULL")
})
public class ResourceScan implements Serializable  {

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public static enum State {
        INITIALIZED, ANALYZING, DONE, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "ref", nullable = false, length = 255)
    private String ref;

    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created", nullable = false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_scan_start", nullable = true)
    private Date lastScanStart;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_scan_end", nullable = true)
    private Date lastScanEnd;

    @Column(name = "http_code", nullable = true)
    private Integer httpResponseCode;

    @Column(name = "http_message", nullable = true, length = 255)
    private String httpResponseMessage;

    @Column(name = "mimetype", nullable = true, length = 255)
    private String mimeType;

    public ResourceScan() {}

    public ResourceScan(String ref, String sessionId) {
        this.ref = ref;
        this.setSessionId(sessionId);
        this.created = new Date();
    }

    public Long getId() {
        return id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Integer getHttpResponseCode() {
        return httpResponseCode;
    }

    public void setHttpResponseCode(Integer httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    public String getHttpResponseMessage() {
        return httpResponseMessage;
    }

    public void setHttpResponseMessage(String httpResponseMessage) {
        this.httpResponseMessage = httpResponseMessage;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Date getLastScan() {
        return lastScanStart;
    }

    public void setLastScan(Date lastScanStart) {
        this.lastScanStart = lastScanStart;
    }

    public Date getLastScanEnd() {
        return lastScanEnd;
    }

    public void setLastScanEnd(Date lastScanEnd) {
        this.lastScanEnd = lastScanEnd;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public float getDurationInSeconds() {
        if(lastScanStart == null || lastScanEnd == null) {
            return -1;
        }
        return (lastScanEnd.getTime()-lastScanStart.getTime())/1000;
    }

    public State getState() {
        if(lastScanStart == null) {
            return State.INITIALIZED;
        }
        if(lastScanStart != null && lastScanEnd == null) {
            return State.ANALYZING;
        }

        int httpCode = getHttpResponseCode().intValue();
        if(httpCode < 200 && httpCode >= 300) {
            return State.FAILED;
        }
        
        return State.DONE;
    }
}
