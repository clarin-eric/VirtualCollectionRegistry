package eu.clarin.cmdi.virtualcollectionregistry.model.collection;

import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * 
 * scan (1) <---> (n) log (1) <---> (n) kv
 *
 * Results in:
 *  scan
 *      processor 1 log
 *          kv 1
 *      processor 2 log 
 *          kv 1
 *          kv 2
 * 
 * Example SQL statements to trigger a scan:
 *  insert into resource_scan (ref, session_id, created) VALUES ('https://www.clarin.eu', 'test_session_id', '2024-02-29 10:00:00');
 *  insert into resource_scan (ref, session_id, created) VALUES ('https://www.example.com', 'test_session_id', '2024-02-29 10:00:00');
 * 
 * @author wilelb
 */
@Entity
@Table(name = "resource_scan")
@NamedQueries({
        @NamedQuery(name = "ResourceScan.findAll",
                query = "SELECT r FROM ResourceScan r"),
        @NamedQuery(name = "ResourceScan.findByRef",
                query = "SELECT r FROM ResourceScan r WHERE r.ref = :ref"),
        @NamedQuery(name = "ResourceScan.findByRefs",
                query = "SELECT r FROM ResourceScan r WHERE r.ref IN :refs"),
        @NamedQuery(name = "ResourceScan.findScanRequired",
                query = "SELECT r FROM ResourceScan r WHERE r.lastScanStart IS NULL")
})
public class ResourceScan implements Serializable, Comparable<ResourceScan>  {

    /**
     * A resource scan added to the UI has INITIALIZED state (database ID == null).
     * After inserting it into the database (effectivly queing it to be processed), puts it it QUEUED state (database ID != null and lastScanStart == null).
     * When the row is actually picked for processing it is put into ANALYZING state (database ID != null and lastScanStart !== null and lastScanEnd == null).
     * When processing is finished it is moved into DONE or FAILED state.
     * 
     *                                         .--> DONE 
     * INITIALIZED --> QUEUED --> ANALYZING --|
     *                                         '--> FAILED
     */
    public static enum State {
        INITIALIZED, QUEUED, ANALYZING, DONE, FAILED
    }
    
    public static boolean isStateAnalyzing(State s) {
        return s == State.INITIALIZED || s == State.QUEUED || s == State.ANALYZING;
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

    @Column(name = "exception", nullable = true, length = 255)
    private String exception;

    @Column(name = "mimetype", nullable = true, length = 255)
    private String mimeType;
        
    @OneToMany(cascade = CascadeType.ALL,
               fetch = FetchType.EAGER,
                mappedBy = "scan")
    private Set<ResourceScanLog> logs;
    
/*
    @Column(name = "name_suggestion", nullable = true, length = 255)
    private String nameSuggestion;

    @Column(name = "description_suggestion", nullable = true)
    private String descriptionSuggestion;

    @Column(name = "processor", nullable = true, length = 255)
    private String processor;
    
    @Column(name = "pid_suggestion", nullable = true)
    private String suggestedPid;
*/
    
    public ResourceScan() {}

    public ResourceScan(String ref, String sessionId) {
        this.ref = ref;
        this.sessionId = sessionId;
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

    public boolean hasHttpResponseMessage() {
        return httpResponseMessage != null && !httpResponseMessage.isEmpty();
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
        if(id == null) {
            return State.INITIALIZED;             
        }
        
        if(lastScanStart == null) {
            return State.QUEUED;
        }
        
        //if(lastScanStart != null && lastScanEnd == null) {
        if(lastScanEnd == null) {
            return State.ANALYZING;
        }

        int httpCode = getHttpResponseCode().intValue();
        if(httpCode < 200 || httpCode >= 300) {
            return State.FAILED;
        }

        if(exception != null) {
            return State.FAILED;
        }
        
        return State.DONE;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }
/*
    public String getNameSuggestion() {
        return nameSuggestion;
    }

    public void setNameSuggestion(String nameSuggestion) {
        this.nameSuggestion = nameSuggestion;
    }

    public String getDescriptionSuggestion() {
        return descriptionSuggestion;
    }

    public void setDescriptionSuggestion(String descriptionSuggestion) {
        this.descriptionSuggestion = descriptionSuggestion;
    }
*/
    @Override
    public int compareTo(@NotNull ResourceScan o) {
        return getRef().compareTo(o.getRef());
    }
/*
    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getSuggestedPid() {
        return suggestedPid;
    }

    public void setSuggestedPid(String suggestedPid) {
        this.suggestedPid = suggestedPid;
    }
*/
    
    /**
     * @return the logs
     */
    public Set<ResourceScanLog> getLogs() {
        return logs;
    }

    /**
     * @param logs the logs to set
     */
    public void setLogs(Set<ResourceScanLog> logs) {
        this.logs = logs;
    }
    
    /**
     * Add a parser log to this scan.
     * 
     * @param parserId 
     */
    public void addResourceScanLog(String parserId) {
        if(logs == null) {
            logs = new HashSet();
        }
        logs.add(new ResourceScanLog(this, parserId));
    }
    
    /**
     * Add a key,value pair for this scans parser log.
     * 
     * @param parserId
     * @param key
     * @param value 
     */
    public void addResourceScanLogKV(String parserId, String key, String value) {
        boolean found = false;
        for(ResourceScanLog log : logs) {
            if(log.getProcessorId().equalsIgnoreCase(parserId)) {
                found = true;
                log.addKV(key, value);
            }
        }
        
        if(!found) {
            //TODO: how to handle this case where the specified parser id was not
            //found in the list of parsers (by parser id).
        }
    }
}
