package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIErrorCode;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIRepositoryAdapter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.VerbContext;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream.NamespaceDecl;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.SetSpecDesc;

public class VerbListSets extends Verb {
    private static final Argument[] ARGUMENTS = {
        DefaultArguments.RESUMPTIONTOKEN
    };
    private static final List<NamespaceDecl> NS_DECLS_DC = Arrays.asList(
            new NamespaceDecl(MetadataConstants.NS_OAI_DC, "oai_dc",
                              MetadataConstants.NS_OAI_DC_SCHEMA_LOCATION),
            new NamespaceDecl(MetadataConstants.NS_DC, null));

    @Override
    public String getName() {
        return "ListSets";
    }

    @Override
    public Argument[] getArguments() {
        return ARGUMENTS;
    }

    @Override
    public void process(VerbContext ctx) throws OAIException {
        logger.debug("process LIST-SETS");

        OAIRepositoryAdapter repository = ctx.getRepository();
        Set<SetSpecDesc> setDescs = repository.getSetSpecs();
        if (repository.isUsingSets()) {
            OAIOutputStream out = ctx.getOutputStream();
            out.writeStartElement("ListSets");
            for (SetSpecDesc setSpec : setDescs) {
                out.writeStartElement("set");
                out.writeStartElement("setSpec");
                out.writeCharacters(setSpec.getId());
                out.writeEndElement(); // setSpec element
                out.writeStartElement("setName");
                out.writeCharacters(setSpec.getName());
                out.writeEndElement(); // setName element
                if (setSpec.getDescription() != null) {
                    out.writeStartElement("setDescription");
                    out.writeStartElement(MetadataConstants.NS_OAI_DC, "dc",
                            NS_DECLS_DC);
                    out.writeStartElement(MetadataConstants.NS_OAI_DC,
                            "description");
                    out.writeCharacters(setSpec.getDescription());
                    out.writeEndElement(); // description element
                    out.writeEndElement(); // dc element
                    out.writeEndElement(); // setDescription element
                }
                out.writeEndElement(); // set element
            }
            out.writeEndElement(); // ListSets element
            out.close();
        } else {
            ctx.addError(OAIErrorCode.NO_SET_HIERARCHY,
                    "This repository does not support sets");
        }
    }

} // class VerbListSets
