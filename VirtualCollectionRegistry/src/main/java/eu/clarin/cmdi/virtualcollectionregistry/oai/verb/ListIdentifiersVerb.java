package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Date;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepository.RecordList;

public class ListIdentifiersVerb extends EnumerateRecordVerb {

	@Override
	public String getName() {
		return "ListIdentifiers";
	}

	@Override
	protected RecordList doGetRecords(OAIRepositoryAdapter repository,
			String prefix, Date from, Date until, String set, int offset)
			throws OAIException {
		return repository.getRecords(prefix, from, until, set, offset, true);
	}

	@Override
	protected void doWriteRecord(OAIRepositoryAdapter repository,
			OAIOutputStream out, MetadataFormat format, Object item)
			throws OAIException {
		Record record = repository.createRecord(item, true);
		repository.writeRecordHeader(out, record);
	}

} // list IdentifiersVerb
