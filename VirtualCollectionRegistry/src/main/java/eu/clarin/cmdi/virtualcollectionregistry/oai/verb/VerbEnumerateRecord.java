package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Date;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ResumptionToken;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.RecordList;

abstract class VerbEnumerateRecord extends Verb {
    private static final String PROP_OFFSET = "_offset";
    private static final Argument[] ARGUMENTS = {
        new Argument(Argument.ARG_FROM, false),
        new Argument(Argument.ARG_UNTIL, false),
        new Argument(Argument.ARG_SET, false),
        new Argument(Argument.ARG_METADATAPREFIX, true),
        new Argument(Argument.ARG_RESUMPTIONTOKEN, false)
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

        OAIRepositoryAdapter repository = ctx.getRepository();
        String prefix = null;
        String set = null;
        Date from = null;
        Date until = null;
        int offset = 0;

        if (ctx.hasArgument(Argument.ARG_RESUMPTIONTOKEN)) {
            String id = (String) ctx.getArgument(Argument.ARG_RESUMPTIONTOKEN);
            ResumptionToken token = repository.getResumptionToken(id);
            if (token == null) {
                ctx.addError(OAIErrorCode.BAD_RESUMPTION_TOKEN,
                        "Invalid resumption token (id='" + id + "')");
                return; // bail early
            }
            synchronized (token) {
                prefix =
                    (String) token.getProperty(Argument.ARG_METADATAPREFIX);
                set    = (String)  token.getProperty(Argument.ARG_SET);
                from   = (Date)    token.getProperty(Argument.ARG_FROM);
                until  = (Date)    token.getProperty(Argument.ARG_UNTIL);
                offset = (Integer) token.getProperty(PROP_OFFSET);
            } // synchronized (token)
        } else {
            prefix = (String) ctx.getArgument(Argument.ARG_METADATAPREFIX);
            set    = (String) ctx.getArgument(Argument.ARG_SET);
            from   = (Date)   ctx.getArgument(Argument.ARG_FROM);
            until  = (Date)   ctx.getArgument(Argument.ARG_UNTIL);
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
                            token.setProperty(Argument.ARG_METADATAPREFIX,
                                              prefix);
                            token.setProperty(Argument.ARG_SET, set);
                            token.setProperty(Argument.ARG_FROM, from);
                            token.setProperty(Argument.ARG_UNTIL, until);
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

    protected abstract RecordList doGetRecords(OAIRepositoryAdapter repository,
            String prefix, Date from, Date until, String set, int offset)
            throws OAIException;

    protected abstract void doWriteRecord(OAIRepositoryAdapter repository,
            OAIOutputStream out, MetadataFormat format, Record item)
            throws OAIException;

} // abstract class VerbEnumerateRecord