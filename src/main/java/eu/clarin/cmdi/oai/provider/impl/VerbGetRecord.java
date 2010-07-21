package eu.clarin.cmdi.oai.provider.impl;

import eu.clarin.cmdi.oai.provider.MetadataFormat;
import eu.clarin.cmdi.oai.provider.OAIException;
import eu.clarin.cmdi.oai.provider.Record;
import eu.clarin.cmdi.oai.provider.ext.Argument;
import eu.clarin.cmdi.oai.provider.ext.OAIErrorCode;
import eu.clarin.cmdi.oai.provider.ext.OAIOutputStream;
import eu.clarin.cmdi.oai.provider.ext.RepositoryAdapter;
import eu.clarin.cmdi.oai.provider.ext.Verb;
import eu.clarin.cmdi.oai.provider.ext.VerbContext;

final class VerbGetRecord extends Verb {
    private static final Argument[] ARGUMENTS = {
        new ArgumentIdentifier(true),
        DefaultArguments.METADATAPREFIX
    };

    @Override
    public String getName() {
        return "GetRecord";
    }

    @Override
    public Argument[] getArguments() {
        return ARGUMENTS;
    }

    @Override
    public void process(VerbContext ctx) throws OAIException {
        logger.debug("process GET-RECORD");

        RepositoryAdapter repository = ctx.getRepository();
        Object localId = ctx.getArgument(DefaultArguments.ARG_IDENTIFIER);
        Record record = repository.getRecord(localId, false);
        if (record != null) {
            String prefix =
                (String) ctx.getArgument(DefaultArguments.ARG_METADATAPREFIX);
            MetadataFormat format = null;
            for (MetadataFormat f : repository.getMetadataFormats(record)) {
                if (prefix.equals(f.getPrefix())) {
                    format = f;
                    break;
                }
            }
            if (format != null) {
                OAIOutputStream out = ctx.getOutputStream();
                out.writeStartElement("GetRecord");
                out.writeRecord(record, format);
                out.writeEndElement(); // GetRecord element
                out.close();
            } else {
                ctx.addError(OAIErrorCode.CANNOT_DISSERMINATE_FORMAT,
                        "The metadataPrefix '" + prefix +
                        "' is not supported for the item identified by '" +
                        repository.createRecordId(localId) + "'");
            }
        } else {
            ctx.addError(OAIErrorCode.ID_DOES_NOT_EXIST,
                    "Record does not exist");
        }
    }

} // class VerbGetRecord
