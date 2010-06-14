package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.Record;

public class ListMetadataFormatsVerb extends Verb {
    private static final List<Argument> s_arguments = Arrays.asList(
            new Argument(Argument.ARG_IDENTIFIER, false));

    @Override
    public String getName() {
        return "ListMetadataFormats";
    }

    @Override
    public List<Argument> getArguments() {
        return s_arguments;
    }

    @Override
    public void process(VerbContext ctx) throws OAIException {
        logger.debug("process LIST-METADATA-FORMATS");

        OAIRepositoryAdapter repository = ctx.getRepository();

        Set<MetadataFormat> formats = null;
        if (ctx.hasArgument(Argument.ARG_IDENTIFIER)) {
            Object localId = ctx.getArgument(Argument.ARG_IDENTIFIER);
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

} // class ListMetadataFormatsVerb
