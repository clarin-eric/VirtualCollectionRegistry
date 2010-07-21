package eu.clarin.cmdi.oai.provider.impl;

import java.util.Date;

import eu.clarin.cmdi.oai.provider.MetadataFormat;
import eu.clarin.cmdi.oai.provider.OAIException;
import eu.clarin.cmdi.oai.provider.Record;
import eu.clarin.cmdi.oai.provider.RecordList;
import eu.clarin.cmdi.oai.provider.ext.OAIOutputStream;
import eu.clarin.cmdi.oai.provider.ext.RepositoryAdapter;

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
