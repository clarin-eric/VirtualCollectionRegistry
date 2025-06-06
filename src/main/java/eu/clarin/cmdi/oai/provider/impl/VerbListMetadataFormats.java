package eu.clarin.cmdi.oai.provider.impl;

import java.util.Set;

import eu.clarin.cmdi.oai.provider.MetadataFormat;
import eu.clarin.cmdi.oai.provider.OAIException;
import eu.clarin.cmdi.oai.provider.Record;
import eu.clarin.cmdi.oai.provider.ext.Argument;
import eu.clarin.cmdi.oai.provider.ext.OAIErrorCode;
import eu.clarin.cmdi.oai.provider.ext.OAIOutputStream;
import eu.clarin.cmdi.oai.provider.ext.RepositoryAdapter;
import eu.clarin.cmdi.oai.provider.ext.Verb;
import eu.clarin.cmdi.oai.provider.ext.VerbContext;

final class VerbListMetadataFormats extends Verb {
    private static final Argument[] ARGUMENTS = {
        new ArgumentIdentifier(false)
    };

    @Override
    public String getName() {
        return "ListMetadataFormats";
    }

    @Override
    public Argument[] getArguments() {
        return ARGUMENTS;
    }

    @Override
    public void process(VerbContext ctx) throws OAIException {
        logger.debug("process LIST-METADATA-FORMATS");

        RepositoryAdapter repository = ctx.getRepository();

        Set<MetadataFormat> formats = null;
        if (ctx.hasArgument(DefaultArguments.ARG_IDENTIFIER)) {
            Object localId = ctx.getArgument(DefaultArguments.ARG_IDENTIFIER);
            Record record = repository.getRecord(localId, true);
            if (record != null) {
                formats = repository.getMetadataFormats(record);
            } else {
                ctx.addError(OAIErrorCode.ID_DOES_NOT_EXIST,
                        "Record does not exist");
            }
        } else {
            formats = repository.getMetadataFormats();
        }

        if (!ctx.hasErrors()) {
            // render response
            OAIOutputStream out = ctx.getOutputStream();
            out.writeStartElement("ListMetadataFormats");
            for (MetadataFormat format : formats) {
                out.writeStartElement("metadataFormat");

                out.writeStartElement("metadataPrefix");
                out.writeCharacters(format.getPrefix());
                out.writeEndElement(); // metadataPrefix element

                out.writeStartElement("schema");
                out.writeCharacters(format.getSchemaLocation());
                out.writeEndElement(); // schema element

                out.writeStartElement("metadataNamespace");
                out.writeCharacters(format.getNamespaceURI());
                out.writeEndElement(); // metadataNamespace element

                out.writeEndElement(); // metadataFormat element
            }
            out.writeEndElement(); // ListMetadataFormats element
            out.close();
        }
    }

} // class VerbListMetadataFormats
