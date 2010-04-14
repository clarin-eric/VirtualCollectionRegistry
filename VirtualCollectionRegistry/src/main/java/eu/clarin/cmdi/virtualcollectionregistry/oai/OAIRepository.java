package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.Date;
import java.util.List;

public interface OAIRepository {
	public static enum Deleted {
		NO,
		PERSISTENT,
		TRANSIENT;
	}
	public static enum Granularity {
		DAYS,
		SECONDS;
	}
	public String getId();
	
	public String getName();
	
	public List<String> getAdminAddreses();
	
	public Date getEarliestTimestamp();
	
	public Deleted getDeletedNotion();
	
	public Granularity getGranularity();

	public List<String> getSupportedMetadataPrefixes();

	public String getDescription();

	public String getSampleRecordId();

} // interface OAIRepository
