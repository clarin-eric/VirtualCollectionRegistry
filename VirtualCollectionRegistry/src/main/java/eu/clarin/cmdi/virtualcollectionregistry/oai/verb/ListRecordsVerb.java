package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.RecordList;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument.Name;

public class ListRecordsVerb extends Verb {
	private static final List<Argument> s_arguments =
		Arrays.asList(new Argument(Name.FROM, false),
				      new Argument(Name.UNTIL, false),
				      new Argument(Name.SET, false),
					  new Argument(Name.RESUMPTIONTOKEN, false),
					  new Argument(Name.METADATAPREFIX, true));


	@Override
	public String getName() {
		return "ListRecords";
	}

	@Override
	public List<Argument> getArguments() {
		return s_arguments;
	}

	@Override
	public void process(VerbContext ctx) throws OAIException {
		logger.debug("process LIST-RECORDS");
		
		OAIRepositoryAdapter repository = ctx.getRepository();
		if (ctx.hasArgument(Name.RESUMPTIONTOKEN)) {
			// handle resumption
			throw new OAIException("resumption not supported, yet!");
		} 

		String prefix = (String) ctx.getArgument(Name.METADATAPREFIX);
		MetadataFormat format = repository.getMetadataFormat(prefix);
		if (format != null) {
			String set = (String) ctx.getArgument(Name.SET);
			if ((set != null) && !repository.isUsingSets()) {
				ctx.addError(OAIErrorCode.NO_SET_HIERARCHY,
                             "Repository does not support sets");
			} else {
				Date from  = (Date) ctx.getArgument(Name.FROM);
				Date until = (Date) ctx.getArgument(Name.UNTIL);
				int offset = 0;
		
				RecordList result =
					repository.getRecords(from, until, set, offset);
				if (result != null) {
					OAIOutputStream out = ctx.getOutputStream();
					out.writeStartElement("ListRecords");
					for (Record record : result.getRecords()) {
						repository.writeRecord(out, record, format);
					}
					out.writeEndElement(); // ListRecords element
					out.close();
				}
			}
		} else {
			ctx.addError(OAIErrorCode.CANNOT_DISSERMINATE_FORMAT,
					"Repository does not support metadataPrefix '" +
					prefix + "'");
		}
	}

} // class ListRecordsVerb
