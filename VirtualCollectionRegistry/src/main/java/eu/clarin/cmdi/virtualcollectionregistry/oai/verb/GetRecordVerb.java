package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Arrays;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.Record;

public class GetRecordVerb extends Verb {
	private static final List<Argument> s_arguments =
		Arrays.asList(new Argument(Argument.ARG_IDENTIFIER, true),
					  new Argument(Argument.ARG_METADATAPREFIX, true));

	@Override
	public String getName() {
		return "GetRecord";
	}

	@Override
	public List<Argument> getArguments() {
		return s_arguments;
	}

	@Override
	public void process(VerbContext ctx) throws OAIException {
		logger.debug("process GET-RECORD");

		OAIRepositoryAdapter repository = ctx.getRepository();
		Object localId = ctx.getArgument(Argument.ARG_IDENTIFIER);
		Record record = repository.getRecord(localId);
		if (record != null) {
			String prefix = (String) ctx.getArgument(Argument.ARG_METADATAPREFIX);
			MetadataFormat format = null;
			for (MetadataFormat f : record.getSupportedMetadataFormats()) {
				if (prefix.equals(f.getPrefix())) {
					format = f;
					break;
				}
			}
			if (format != null) {
				OAIOutputStream out = ctx.getOutputStream();
				out.writeStartElement("GetRecord");
				repository.writeRecord(out, record, format);
				out.writeEndElement(); // GetRecord element
				out.close();
			} else {
				ctx.addError(OAIErrorCode.CANNOT_DISSERMINATE_FORMAT,
						"Repository does not support metadataPrefix '" +
						prefix + "'");
			}
		} else {
			ctx.addError(OAIErrorCode.ID_DOES_NOT_EXIST,
			         "Record does not exist");
		}
	}

} // class GetRecordVerb
