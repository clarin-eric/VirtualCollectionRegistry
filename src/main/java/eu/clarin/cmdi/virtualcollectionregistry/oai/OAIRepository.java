package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.util.List;

public interface OAIRepository {

	public String getId();
	
	public String getName();
	
	/* FIXME: add
	 * - protocol version
	 * - earliest datastamp
	 * - deleteced record
	 * - granularity
	 */
	public List<String> getAdminAddreses();
	
	/* XXX: whats missing?
	 * - compression? (should be handled directly by OAIProvider!)
	 * - default oai-identifier?
	 */

	public List<String> getSupportedMetadataPrefixes();

	public String getDescription();

	public String getSampleRecordId();

} // interface OAIRepository
