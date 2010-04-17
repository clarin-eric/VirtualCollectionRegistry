package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.DeletedNotion;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.Granularity;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument;


public class OAIRepositoryAdapter {
	private final OAIProvider provider;
	private final OAIRepository repository;
	private final ThreadLocal<SimpleDateFormat> sdf =
		new ThreadLocal<SimpleDateFormat>() {
		protected SimpleDateFormat initialValue() {
			return createDateFormat(repository);
		}
	};
	private final Date earliestTimestamp;

	OAIRepositoryAdapter(OAIProvider provider, OAIRepository repository)
			throws OAIException {
		this.provider   = provider;
		this.repository = repository;

		// check of repository supports oai_dc format
		if (!supportsMetadataFormat("oai_dc")) {
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
		return sdf.get().format(earliestTimestamp);
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

	public Object parseArgument(Argument.Name name, String value) {
		switch (name) {
		case IDENTIFIER:
			String localId = extractLocalId(value);
			if (localId != null) {
				return repository.parseLocalId(localId);
			}
			break;
		default:
			return value;
		}
		return false;
	}

	public String createRecordId(String recordId) {
		StringBuilder sb = new StringBuilder("oai:");
		sb.append(repository.getId());
		sb.append(":");
		sb.append(recordId);
		return sb.toString();
	}

	public boolean supportsMetadataFormat(String prefix) {
		// XXX: maybe store prefixes in hash map for faster access?
		for (MetadataFormat format : repository.getSupportedMetadataFormats()) {
			if (prefix.equals(format.getPrefix())) {
				return true;
			}
		}
		return false;
	}

	public boolean isUsingSets() {
		return repository.getSetDescs() != null;
	}

	public Record getRecord(String localId) throws OAIException {
		return repository.getRecord(localId);
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

	private static SimpleDateFormat createDateFormat(OAIRepository repository) {
		SimpleDateFormat sdf = null;
		switch (repository.getGranularity()) {
		case DAYS:
			sdf = new SimpleDateFormat("yyyy-MM-dd");
			break;
		case SECONDS:
			sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			break;
		}
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf;
	}

} // class OAIRepositoryAdapter
