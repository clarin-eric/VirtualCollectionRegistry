package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

import java.util.Date;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.RecordList;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.Argument;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.RepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.ResumptionToken;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.Verb;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.VerbContext;

abstract class VerbEnumerateRecord extends Verb {
    private static final String PROP_OFFSET = "_offset";
    private static final Argument[] ARGUMENTS = {
        DefaultArguments.FROM,
        DefaultArguments.UNTIL,
        DefaultArguments.SET,
        DefaultArguments.METADATAPREFIX,
        DefaultArguments.RESUMPTIONTOKEN
    };

    protected VerbEnumerateRecord() {
    }

    @Override
    public final Argument[] getArguments() {
        return ARGUMENTS;
    }

    @Override
    public final void process(VerbContext ctx) throws OAIException {
        logger.debug("process ENUMERATE-RECORD ({})", getName());

        RepositoryAdapter repository = ctx.getRepository();
        String prefix = null;
        String set = null;
        Date from = null;
        Date until = null;
        int offset = 0;

        if (ctx.hasArgument(DefaultArguments.ARG_RESUMPTIONTOKEN)) {
            String id = (String) ctx.getArgument(DefaultArguments.ARG_RESUMPTIONTOKEN);
            ResumptionToken token = repository.getResumptionToken(id);
            if (token == null) {
                ctx.addError(OAIErrorCode.BAD_RESUMPTION_TOKEN,
                        "Invalid resumption token (id='" + id + "')");
                return; // bail early
            }
            synchronized (token) {
                prefix =
                    (String) token.getProperty(DefaultArguments.ARG_METADATAPREFIX);
                set    = (String)  token.getProperty(DefaultArguments.ARG_SET);
                from   = (Date)    token.getProperty(DefaultArguments.ARG_FROM);
                until  = (Date)    token.getProperty(DefaultArguments.ARG_UNTIL);
                offset = (Integer) token.getProperty(PROP_OFFSET);
            } // synchronized (token)
        } else {
            prefix = (String) ctx.getArgument(DefaultArguments.ARG_METADATAPREFIX);
            set    = (String) ctx.getArgument(DefaultArguments.ARG_SET);
            from   = (Date)   ctx.getArgument(DefaultArguments.ARG_FROM);
            until  = (Date)   ctx.getArgument(DefaultArguments.ARG_UNTIL);
        }

        MetadataFormat format = repository.getMetadataFormatByPrefix(prefix);
        if (format != null) {
            if ((set != null) && !repository.isUsingSets()) {
                ctx.addError(OAIErrorCode.NO_SET_HIERARCHY,
                        "Repository does not support sets");
            } else {
                // fetch records
                RecordList result =
                    doGetRecords(repository, prefix, from, until, set, offset);

                // process results
                if (result != null) {
                    OAIOutputStream out = ctx.getOutputStream();
                    out.writeStartElement(getName());
                    List<Record> records = result.getRecords();
                    for (Record record : records) {
                        doWriteRecord(repository, out, format, record);
                    }

                    // add resumption token, if more results are pending
                    if (result.hasMore()) {
                        ResumptionToken token =
                            repository.createResumptionToken();
                        synchronized (token) {
                            token.setProperty(DefaultArguments.ARG_METADATAPREFIX,
                                              prefix);
                            token.setProperty(DefaultArguments.ARG_SET, set);
                            token.setProperty(DefaultArguments.ARG_FROM, from);
                            token.setProperty(DefaultArguments.ARG_UNTIL, until);
                            int nextOffset =
                                records.size() + result.getOffset();
                            token.setProperty(PROP_OFFSET, nextOffset);
                            token.setCursor(offset);
                            token.setCompleteListSize(result.getTotalCount());
                            out.writeResumptionToken(token);
                        } // synchronized (token)
                    }
                    out.writeEndElement(); // ListRecords element
                    out.close();
                } else {
                    ctx.addError(OAIErrorCode.NO_RECORDS_MATCH,
                            "No records match");
                }
            }
        } else {
            ctx.addError(OAIErrorCode.CANNOT_DISSERMINATE_FORMAT,
                    "Repository does not support metadataPrefix '" +
                    prefix + "'");
        }
    }

    protected abstract RecordList doGetRecords(RepositoryAdapter repository,
            String prefix, Date from, Date until, String set, int offset)
            throws OAIException;

    protected abstract void doWriteRecord(RepositoryAdapter repository,
            OAIOutputStream out, MetadataFormat format, Record item)
            throws OAIException;

} // abstract class VerbEnumerateRecord
