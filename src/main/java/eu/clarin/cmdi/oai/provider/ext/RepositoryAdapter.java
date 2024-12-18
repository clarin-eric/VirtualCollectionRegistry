package eu.clarin.cmdi.oai.provider.ext;

import java.util.Date;
import java.util.Set;

import eu.clarin.cmdi.oai.provider.MetadataFormat;
import eu.clarin.cmdi.oai.provider.OAIException;
import eu.clarin.cmdi.oai.provider.Record;
import eu.clarin.cmdi.oai.provider.RecordList;
import eu.clarin.cmdi.oai.provider.Repository;
import eu.clarin.cmdi.oai.provider.SetSpecDesc;
import eu.clarin.cmdi.oai.provider.impl.OAIProvider;

public interface RepositoryAdapter {

    public OAIProvider getProvider();

    public String getId();

    public String getName();

    public Set<String> getAdminEmailAddresses();

    public Date getEarliestTimestamp();

    public Repository.DeletedNotion getDeletedNotion();

    public Repository.Granularity getGranularity();

    public boolean supportsCompressionMethod(int method);

    public String getDescription();

    public String getSampleRecordId();

    public Set<MetadataFormat> getMetadataFormats();

    public Set<MetadataFormat> getMetadataFormats(Record record);

    public MetadataFormat getMetadataFormatByPrefix(String prefix);

    public Set<SetSpecDesc> getSetSpecs();

    public boolean isUsingSets();

    public String createRecordId(Object localId);

    public Object parseLocalId(String unparsedLocalId);

    public Record getRecord(Object localId, boolean headerOnly)
            throws OAIException;

    public RecordList getRecords(String prefix, Date from, Date until,
            String set, int offset, boolean headerOnly) throws OAIException;

    public ResumptionToken createResumptionToken();

    public ResumptionToken getResumptionToken(String id);

} // interface RepositoryAdapter
