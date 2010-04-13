package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream.NamespaceDecl;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument.Name;

public class GetRecordVerb extends Verb {
	private static final String NS_OAI_DC =
		"http://www.openarchives.org/OAI/2.0/oai_dc/";
	private static final String NS_OAI_DC_SCHEMA_LOCATION =
		"http://www.openarchives.org/OAI/2.0/oai_dc.xsd";
	private static final String NS_DC =
		"http://purl.org/dc/elements/1.1/";
	private static final List<Argument> s_arguments =
		Arrays.asList(new Argument(Name.IDENTIFIER, true),
					  new Argument(Name.METADATAPREFIX, true));

	@Override
	public String getName() {
		return "GetRecord";
	}

	@Override
	public List<Argument> getArguments() {
		return s_arguments;
	}

	@Override
	public void process(VerbContext ctx) throws OAIException {
		logger.debug("process GET-RECORD");
		
		// XXX: testing only ...
		OAIOutputStream out = ctx.getOutputStream();
		out.writeStartElement("GetRecord");
		out.writeStartElement("record");
		
		out.writeStartElement("header");
		out.writeStartElement("identifier");
		out.writeCharacters(ctx.getArgument("identifier"));
		out.writeEndElement();
		out.writeStartElement("datestamp");
		out.writeDateAsCharacters(new Date());
		out.writeEndElement(); // datestamp element
		out.writeEndElement(); // header element

		out.writeStartElement("metadata");
		
		out.writeStartElement(NS_OAI_DC, "dc", Arrays.asList(
				new NamespaceDecl(NS_OAI_DC, "oai_dc", NS_OAI_DC_SCHEMA_LOCATION),
				new NamespaceDecl(NS_DC, "dc")
		));

		out.writeStartElement(NS_DC, "title");
		out.writeCharacters("Test title");
		out.writeEndElement(); // dc:title element
		out.writeStartElement(NS_DC, "creator");
		out.writeCharacters("Test Creator");
		out.writeEndElement(); // dc:creator element

		out.writeEndElement(); // oai:dc element

		out.writeEndElement(); // metadata element
		out.writeEndElement(); // record element
		out.writeEndElement(); // GetRecord element
		out.close();
	}

} // class GetRecordVerb
