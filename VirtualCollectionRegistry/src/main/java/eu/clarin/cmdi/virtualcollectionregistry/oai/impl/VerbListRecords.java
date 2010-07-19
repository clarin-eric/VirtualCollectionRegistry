package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

import java.util.Date;

import eu.clarin.cmdi.virtualcollectionregistry.oai.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.RecordList;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.RepositoryAdapter;

final class VerbListRecords extends VerbEnumerateRecord {

    @Override
    public String getName() {
        return "ListRecords";
    }

    @Override
    protected RecordList doGetRecords(RepositoryAdapter repository,
            String prefix, Date from, Date until, String set, int offset)
            throws OAIException {
        return repository.getRecords(prefix, from, until, set, offset, false);
    }

    @Override
    protected void doWriteRecord(RepositoryAdapter repository,
            OAIOutputStream out, MetadataFormat format, Record record)
            throws OAIException {
        out.writeRecord(record, format);
    }

} // class VerbListRecords
