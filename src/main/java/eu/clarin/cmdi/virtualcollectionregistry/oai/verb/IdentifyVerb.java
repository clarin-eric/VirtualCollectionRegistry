package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream.NamespaceDecl;

public class IdentifyVerb extends Verb {
	private static List<NamespaceDecl> descsNsDecls = Arrays.asList(
			new NamespaceDecl(MetadataConstants.NS_OAI_DC, null,
                              MetadataConstants.NS_OAI_DC_SCHEMA_LOCATION)
	);
	private static List<NamespaceDecl> identifierNsDecls = Arrays.asList(
			new NamespaceDecl(MetadataConstants.NS_OAI_IDENTIFIER, null,
                              MetadataConstants.NS_OAI_IDENTIFIER_SCHEMA_LOCATION)
	);

	@Override
	public String getName() {
		return "Identify";
	}

	@Override
	public List<Argument> getArguments() {
		return Collections.emptyList();
	}

	@Override
	public void process(VerbContext ctx) throws OAIException {
		logger.debug("process IDENTIFY");
		
		OAIRepositoryAdapter repository = ctx.getRepository();
		OAIOutputStream out = ctx.getOutputStream();
		out.writeStartElement("Identify");
		
		out.writeStartElement("repositoryName");
		out.writeCharacters(repository.getName());
		out.writeEndElement(); // repositoryName element
		
		out.writeStartElement("baseURL");
		out.writeCharacters(ctx.getRequestURI());
		out.writeEndElement(); // baseURL element
		
		out.writeStartElement("protocolVersion");
		out.writeCharacters("2.0");
		out.writeEndElement(); // protocolVersion element
		
		for (String adminEmail : repository.getAdminEmailAddresses()) {
			out.writeStartElement("adminEmail");
			out.writeCharacters(adminEmail);
			out.writeEndElement(); // adminEmail element
		}

		// XXX: earliestDatestamp
		// XXX: deletedRecord
		// XXX: granularity
		// XXX: compression?

		// description/oai-identifier
		out.writeStartElement("description");
		out.writeStartElement(MetadataConstants.NS_OAI_IDENTIFIER,
				              "oai-identifier",
                              identifierNsDecls);
		out.writeStartElement(MetadataConstants.NS_OAI_IDENTIFIER,
				              "scheme");
		out.writeCharacters("oai");
		out.writeEndElement(); // scheme element
		out.writeStartElement(MetadataConstants.NS_OAI_IDENTIFIER,
						      "repository");
		out.writeCharacters(repository.getId());
		out.writeEndElement(); // repository element
		out.writeStartElement(MetadataConstants.NS_OAI_IDENTIFIER,
						      "delimiter");
		out.writeCharacters(":");
		out.writeEndElement(); // delimiter element
		out.writeStartElement(MetadataConstants.NS_OAI_IDENTIFIER,
				              "sampleIdentifier");
		out.writeCharacters(repository
				.makeRecordId(repository.getSampleRecordId()));
		out.writeEndElement(); // sampleIdentifier element
		out.writeEndElement(); // oai-identifier element
		out.writeEndElement(); // description element

		// description/dc
		if (repository.getDescription() != null) {
			out.writeStartElement("description");
			out.writeStartElement(MetadataConstants.NS_OAI_DC,
								  "dc",
								  descsNsDecls);
			out.writeStartElement(MetadataConstants.NS_OAI_DC, "title");
			out.writeCharacters(repository.getName());
			out.writeEndElement(); // title
			out.writeStartElement(MetadataConstants.NS_OAI_DC, "description");
			out.writeCharacters(repository.getDescription());
			out.writeEndElement(); // description
			out.writeEndElement(); // dc element
			out.writeEndElement(); // description element
		}

		out.writeEndElement(); // Identify element
		out.close();
	}

} // class IdentifyVerb
