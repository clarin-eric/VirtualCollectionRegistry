package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.ResumptionToken;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.RecordList;

public class ListIdentifiersVerb extends Verb {
	private static final String PROP_OFFSET = "_offset";
	private static final List<Argument> s_arguments =
		Arrays.asList(new Argument(Argument.ARG_FROM, false),
				      new Argument(Argument.ARG_UNTIL, false),
				      new Argument(Argument.ARG_SET, false),
					  new Argument(Argument.ARG_RESUMPTIONTOKEN, false),
					  new Argument(Argument.ARG_METADATAPREFIX, true));

	@Override
	public String getName() {
		return "ListIdentifiers";
	}

	@Override
	public List<Argument> getArguments() {
		return s_arguments;
	}

	@Override
	public void process(VerbContext ctx) throws OAIException {
		logger.debug("process LIST-IDENTIFIERS");
		
		OAIRepositoryAdapter repository = ctx.getRepository();
		String prefix = null;
		String set    = null;
		Date from     = null;
		Date until    = null;
		int offset    = 0;

		if (ctx.hasArgument(Argument.ARG_RESUMPTIONTOKEN)) {
			String id = (String) ctx.getArgument(Argument.ARG_RESUMPTIONTOKEN);
			ResumptionToken token = repository.getResumptionToken(id);
			if (token == null) {
				ctx.addError(OAIErrorCode.BAD_RESUMPTION_TOKEN,
							 "Invalid resumption token (id='" + id + "')");
				return; // bail early
			}
			synchronized (token) {
				prefix = (String)  token.getProperty(Argument.ARG_METADATAPREFIX);
				set    = (String)  token.getProperty(Argument.ARG_SET);
				from   = (Date)    token.getProperty(Argument.ARG_FROM);
				until  = (Date)    token.getProperty(Argument.ARG_UNTIL);
				offset = (Integer) token.getProperty(PROP_OFFSET);
			} // synchronized (token)
		} else {
			prefix = (String) ctx.getArgument(Argument.ARG_METADATAPREFIX);
			set    = (String) ctx.getArgument(Argument.ARG_SET);
			from   = (Date) ctx.getArgument(Argument.ARG_FROM);
			until  = (Date) ctx.getArgument(Argument.ARG_UNTIL);
		}

		MetadataFormat format = repository.getMetadataFormat(prefix);
		if (format != null) {
			if ((set != null) && !repository.isUsingSets()) {
				ctx.addError(OAIErrorCode.NO_SET_HIERARCHY,
                             "Repository does not support sets");
			} else {
				// fetch records
				// FIXME: should also pass metadataPrefix
				RecordList result =
					repository.getRecords(prefix, from, until, set, offset);
				
				// process results
				if (result != null) {
					OAIOutputStream out = ctx.getOutputStream();
					out.writeStartElement("ListIdentifiers");
					for (Record record : result.getRecords()) {
						repository.writeRecordHeader(out, record);
					}

					// add resumption token, if more results are pending
					if (result.hasMore()) {
						ResumptionToken token =
							repository.createResumptionToken();
						synchronized (token) {
							token.setProperty(Argument.ARG_METADATAPREFIX,
									          prefix);
							token.setProperty(Argument.ARG_SET,   set);
							token.setProperty(Argument.ARG_FROM,  from);
							token.setProperty(Argument.ARG_UNTIL, until);
							token.setProperty(PROP_OFFSET,
									          result.getNextOffset());
							token.setCursor(offset);
							token.setCompleteListSize(result.getTotalCount());
							repository.writeResumptionToken(out, token);
						} // synchronized (token)
					}
					out.writeEndElement(); // ListIdentifiers element
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

} // list IdentifiersVerb