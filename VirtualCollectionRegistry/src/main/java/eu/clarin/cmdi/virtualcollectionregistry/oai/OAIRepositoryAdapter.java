package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.List;


public class OAIRepositoryAdapter {
	private final OAIProvider provider;
	private final OAIRepository repository;
	
	OAIRepositoryAdapter(OAIProvider provider, OAIRepository repository) {
		this.provider   = provider;
		if (repository == null) {
			throw new NullPointerException("repository == null");
		}
		this.repository = repository;
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

	public List<String> getSupportedMetadataPrefixes() {
		return repository.getSupportedMetadataPrefixes();
	}

	public String getDescription() {
		String description = repository.getDescription();
		if ((description != null) && !description.isEmpty()) {
			return description;
		}
		return null;
	}

	public String getSampleRecordId() {
		return repository.getSampleRecordId();
	}

	public String makeRecordId(String recordId) {
		return "oai:" + repository.getId() + ":" +recordId;
	}

} // class OAIRepositoryAdapter
