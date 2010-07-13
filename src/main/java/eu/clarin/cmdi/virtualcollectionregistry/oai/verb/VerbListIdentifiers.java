package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Date;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.RecordList;

public class VerbListIdentifiers extends VerbEnumerateRecord {

    @Override
    public String getName() {
        return "ListIdentifiers";
    }

    @Override
    protected RecordList doGetRecords(OAIRepositoryAdapter repository,
            String prefix, Date from, Date until, String set, int offset)
            throws OAIException {
        return repository.getRecords(prefix, from, until, set, offset, true);
    }

    @Override
    protected void doWriteRecord(OAIRepositoryAdapter repository,
            OAIOutputStream out, MetadataFormat format, Record record)
            throws OAIException {
        repository.writeRecordHeader(out, record);
    }

} // list IdentifiersVerb
