package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.DeletedNotion;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.Granularity;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.RecordList;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument;


public class OAIRepositoryAdapter {
	private final OAIProvider provider;
	private final OAIRepository repository;
	private final ThreadLocal<SimpleDateFormat> sdf =
		new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			SimpleDateFormat sdf = new SimpleDateFormat();
			sdf.applyPattern(getDatePattern(repository.getGranularity()));
			sdf.setLenient(false);
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			return sdf;
		}
	};
	private final Date earliestTimestamp;

	OAIRepositoryAdapter(OAIProvider provider, OAIRepository repository)
			throws OAIException {
		this.provider   = provider;
		this.repository = repository;

		// check of repository supports oai_dc format
		if (getMetadataFormat("oai_dc") == null) {
			throw new OAIException("repository does not supported " +
					"mandatory \"oai_dc\" format");
		}

		// cache earliest timestamp
		this.earliestTimestamp = repository.getEarliestTimestamp();
		if (this.earliestTimestamp == null) {
			throw new OAIException("invalid earliest timestamp");
		}
		
		// XXX: for now raise error, if repository supports sets
		if (repository.getSetDescs() != null) {
			throw new OAIException("Repository supportes set, but set "+
					"support is not available, yet!");
		}
	}

	public OAIProvider getProvider() {
		return provider;
	}

	public String getId() {
		return repository.getId();
	}
	
	public String getName() {
		return repository.getName();
	}
	
	public List<String> getAdminEmailAddresses() {
		return repository.getAdminAddreses();
	}

	public String getEarliestTimestamp() {
		return formatDate(earliestTimestamp);
	}

	public DeletedNotion getDeletedNotion() {
		return repository.getDeletedNotion();
	}
	
	public Granularity getGranularity() {
		return repository.getGranularity();
	}

	public List<MetadataFormat> getSupportedMetadataFormats() {
		return repository.getSupportedMetadataFormats();
	}

	public String getDescription() {
		String description = repository.getDescription();
		if ((description != null) && !description.isEmpty()) {
			return description;
		}
		return null;
	}

	public String getSampleRecordId() {
		return createRecordId(repository.getSampleRecordLocalId());
	}

	public Object parseArgument(String name, String value) {
		Object result = null;
		if (name.equals(Argument.ARG_IDENTIFIER)) {
			String localId = extractLocalId(value);
			if (localId != null) {
				result = repository.parseLocalId(localId);
			}
		} else if (name.equals(Argument.ARG_FROM) ||
				   name.equals(Argument.ARG_UNTIL)) {
			/* First try to parse date format in repository default format.
			 * If this fails and repository supports SECONDS granularity try
		     * also DAYS granularity 
			 */
			SimpleDateFormat parser = sdf.get();
			ParsePosition pos       = new ParsePosition(0);
			Date date               = parser.parse(value, pos);
			if ((date == null) &&
			    (repository.getGranularity() == Granularity.SECONDS)) {
				// re-try with DAYS granularity
				pos.setIndex(0);
				parser.applyPattern(getDatePattern(Granularity.DAYS));
				date = parser.parse(value, pos);
				// reset pattern, success check if done below
				parser.applyPattern(getDatePattern(Granularity.SECONDS));
			}

			// make sure input has not been parsed partly
			if ((date != null) && (pos.getIndex() == value.length())) {
				result = date;
			}
		} else {
			result = value;
		}
		return result;
	}

	public String createRecordId(Object localId) {
		StringBuilder sb = new StringBuilder("oai:");
		sb.append(repository.getId());
		sb.append(":");
		sb.append(repository.unparseLocalId(localId));
		return sb.toString();
	}

	public MetadataFormat getMetadataFormat(String prefix) {
		for (MetadataFormat format : repository.getSupportedMetadataFormats()) {
			if (prefix.equals(format.getPrefix())) {
				return format;
			}
		}
		return null;
	}

	public boolean isUsingSets() {
		return repository.getSetDescs() != null;
	}

	public String formatDate(Date date) {
		return sdf.get().format(date);
	}

	public Record getRecord(Object localId) throws OAIException {
		return repository.getRecord(localId);
	}

	public RecordList getRecords(String prefix, Date from, Date until,
			String set, int offset) throws OAIException {
		return repository.getRecords(prefix, from, until, set, offset);
	}

	public ResumptionToken createResumptionToken() {
		return provider.createResumptionToken(-1);
	}

	public ResumptionToken getResumptionToken(String id) {
		return provider.getResumptionToken(id, -1);
	}

	public void writeRecordHeader(OAIOutputStream out, Record record)
		throws OAIException {
		out.writeStartElement("header");
		if (record.isDeleted()) {
			out.writeAttribute("status", "deleted");
		}
		out.writeStartElement("identifier");
		out.writeCharacters(createRecordId(record.getLocalId()));
		out.writeEndElement(); // identifier element
		out.writeStartElement("datestamp");
		out.writeDate(record.getDatestamp());
		out.writeEndElement(); // datestamp element
		for (String setSpec : record.getSetSpec()) {
			out.writeStartElement("setSpec");
			out.writeCharacters(setSpec);
			out.writeEndElement(); // setSpec element
		}
		out.writeEndElement(); // header element
	}

	public void writeRecord(OAIOutputStream out, Record record,
			MetadataFormat format) throws OAIException {
		out.writeStartElement("record");
		writeRecordHeader(out, record);
		if (!record.isDeleted()) {
			out.writeStartElement("metadata");
			format.writeObject(out, record.getItem());
			out.writeEndElement(); // metadata element
		}
		out.writeEndElement(); // record element
	}

	public void writeResumptionToken(OAIOutputStream out, ResumptionToken token)
		throws OAIException {
		out.writeStartElement("resumptionToken");
		out.writeAttribute("expirationDate",
				formatDate(token.getExpirationDate()));
		if (token.getCursor() >= 0) {
			out.writeAttribute("cursor", Integer.toString(token.getCursor()));
		}
		if (token.getCompleteListSize() > 0) {
			out.writeAttribute("completeListSize",
					Integer.toString(token.getCompleteListSize()));
		}
		out.writeCharacters(token.getId());
		out.writeEndElement(); // resumptionToken element
	}

	private String extractLocalId(String identifier) {
		int pos1 = identifier.indexOf(':'); 
		if (pos1 != -1) {
			int pos2 = identifier.indexOf(':', pos1 + 1);
			if (pos2 != -1) {
				// check of repository id matches
				String id = repository.getId();
				if (identifier.regionMatches(pos1 + 1, id, 0, id.length())) {
					return identifier.substring(pos2 + 1);
				}
			}
		}
		return null;
	}

	private static String getDatePattern(Granularity granularity) {
		switch (granularity) {
		case DAYS:
			return "yyyy-MM-dd";
		default:
			return "yyyy-MM-dd'T'HH:mm:ss'Z'";
		}
	}

} // class OAIRepositoryAdapter
