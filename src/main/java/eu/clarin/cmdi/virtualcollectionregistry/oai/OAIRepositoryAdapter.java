package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.DublinCoreConverter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.OAIRepository;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.RecordList;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.SetSpecDesc;

public final class OAIRepositoryAdapter {
    private final OAIProvider provider;
    private final OAIRepository repository;
    private final Set<String> adminEmailAddresses;
    private final Set<MetadataFormat> metadataFormats =
        new HashSet<MetadataFormat>();
    private final Set<SetSpecDesc> setSpecs;
    private final Map<Class<?>, Set<MetadataFormat>> metadataFormatsByClass =
        new HashMap<Class<?>, Set<MetadataFormat>>();

    OAIRepositoryAdapter(OAIProvider provider, OAIRepository repository)
            throws OAIException {
        this.provider = provider;
        this.repository = repository;

        this.adminEmailAddresses = repository.getAdminAddreses();
        if (this.adminEmailAddresses == null) {
            throw new NullPointerException("getAdminAddreses() == null");
        }
        if (this.adminEmailAddresses.isEmpty()) {
            throw new OAIException("admin email addresses are empty");
        }

        // handle Dublin Core and do some sanity checks
        Set<DublinCoreConverter> converters =
            repository.getDublinCoreConverters();
        if (converters == null) {
            throw new NullPointerException("getDublinCoreConverters() == null");
        }
        if (converters.isEmpty()) {
            throw new OAIException("set of Dublin Core converters is empty");
        }
        this.metadataFormats.add(new DublinCoreMetadataFormat(converters));

        // handle metadata custom formats and do some sanity checks
        Set<MetadataFormat> formats = repository.getCustomMetadataFormats();
        if (formats != null) {
            Set<String> prefixes = new HashSet<String>();
            for (MetadataFormat format : formats) {
                String prefix = format.getPrefix();
                if (prefix == null) {
                    throw new NullPointerException("metadata format needs " +
                            "prefix non-null prefix");
                }
                if (prefixes.contains(prefix)) {
                    throw new OAIException("metadata prefix must be unique " +
                            "for a repository: " + prefix);
                }
                if ("oai_dc".equals(prefix)) {
                    throw new OAIException("dublin core metadata format must " +
                            "not be in the set of supported custom metadata " +
                            "formats");
                }
                // add format
                this.metadataFormats.add(format);
            }
        }

        // cache set specs
        Set<SetSpecDesc> tmp = repository.getSetDescs();
        if ((tmp != null) && tmp.isEmpty()) {
            tmp = null;
        }
        this.setSpecs = tmp;
    }

    public OAIProvider getProvider() {
        return provider;
    }

    public String getId() {
        return repository.getId();
    }

    public String getName() {
        return repository.getName();
    }

    public Set<String> getAdminEmailAddresses() {
        return adminEmailAddresses;
    }

    public Date getEarliestTimestamp() {
        Date date = repository.getEarliestTimestamp();
        if (date == null) {
            date = new Date();
        }
        return date;
    }

    public OAIRepository.DeletedNotion getDeletedNotion() {
        return repository.getDeletedNotion();
    }

    public OAIRepository.Granularity getGranularity() {
        return repository.getGranularity();
    }

    public boolean isSupportingCompressionMethod(int method) {
        int methods = repository.getCompressionMethods();
        return (methods & method) > 0;
    }

    public String getDescription() {
        String description = repository.getDescription();
        if ((description != null) && !description.isEmpty()) {
            return description;
        }
        return null;
    }

    public String getSampleRecordId() {
        return createRecordId(repository.getSampleRecordLocalId());
    }

    public Set<MetadataFormat> getMetadataFormats() {
        return metadataFormats;
    }

    public Set<MetadataFormat> getMetadataFormats(Record record) {
        Class<?> clazz = record.getItemClass();
        synchronized (metadataFormatsByClass) {
            Set<MetadataFormat> result = metadataFormatsByClass.get(clazz);
            if (result == null) {
                result = new HashSet<MetadataFormat>();
                for (MetadataFormat format : metadataFormats) {
                    if (format.canWriteClass(clazz)) {
                        result.add(format);
                    }
                }
                if (result.isEmpty()) {
                    result = Collections.emptySet();
                }
                metadataFormatsByClass.put(clazz, result);
            }
            return result;
        } // synchronized
    }

    public MetadataFormat getMetadataFormatByPrefix(String prefix) {
        for (MetadataFormat format : metadataFormats) {
            if (prefix.equals(format.getPrefix())) {
                return format;
            }
        }
        return null;
    }

    public Set<SetSpecDesc> getSetSpecs() {
        return setSpecs;
    }

    public boolean isUsingSets() {
        return setSpecs != null;
    }

    public String createRecordId(Object localId) {
        StringBuilder sb = new StringBuilder("oai:");
        sb.append(repository.getId());
        sb.append(":");
        sb.append(repository.unparseLocalId(localId));
        return sb.toString();
    }

    public Record getRecord(Object localId, boolean headerOnly)
            throws OAIException {
        return repository.getRecord(localId, headerOnly);
    }

    public Object parseLocalId(String unparsedLocalId) {
        return repository.parseLocalId(unparsedLocalId);
    }

    public RecordList getRecords(String prefix, Date from, Date until,
            String set, int offset, boolean headerOnly) throws OAIException {
        return repository.getRecords(prefix, from, until, set, offset,
                headerOnly);
    }

    public ResumptionToken createResumptionToken() {
        return provider.createResumptionToken(-1);
    }

    public ResumptionToken getResumptionToken(String id) {
        return provider.getResumptionToken(id, -1);
    }

} // class OAIRepositoryAdapter
