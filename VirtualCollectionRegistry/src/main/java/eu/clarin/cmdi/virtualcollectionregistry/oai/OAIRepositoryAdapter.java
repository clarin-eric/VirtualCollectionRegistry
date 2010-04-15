package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.DeletedNotion;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.Granularity;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.MetadataFormat;


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
		return makeRecordId(repository.getSampleRecordId());
	}

	public String makeRecordId(String recordId) {
		return "oai:" + repository.getId() + ":" +recordId;
	}

	public String getInternalId(String identifier) {
		if (identifier == null) {
			throw new NullPointerException("identifier == null");
		}
		int pos = identifier.indexOf(':'); 
		if (pos != -1) {
			pos = identifier.indexOf(':', pos + 1);
			if (pos != -1) {
				String id = identifier.substring(pos + 1);
				if (repository.isValidInternalId(id)) {
					return id;
				}
			}
		}
		return null;
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
