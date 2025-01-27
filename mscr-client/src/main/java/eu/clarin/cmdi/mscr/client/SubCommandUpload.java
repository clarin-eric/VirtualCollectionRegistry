package eu.clarin.cmdi.mscr.client;

import eu.clarin.cmdi.mscr.client.lib.MscrApiConfiguration;
import eu.clarin.cmdi.mscr.client.lib.MscrClient;
import eu.clarin.cmdi.mscr.client.lib.MscrSchemaUploadRequest;
import eu.clarin.cmdi.mscr.client.lib.impl.MscrClientImpl;
import java.io.File;
import org.kohsuke.args4j.Option;

/**
 *
 * @author wilelb
 */
public class SubCommandUpload implements MscrCliSubCommand {
        
    @Option(name = "--schema-file", usage = "Path to the schema file for uploading into the MSCR", required = true )
    public String schemaFile = "";
    
    @Option(name = "--name", usage = "Name for the uploaded schema", required = true )
    public String name = "";
    
    @Option(name = "--lang", usage = "Language code for the name text (defaults to: en)", required = false )
    public String lang = "en";
    
    @Option(name = "--namespace", usage = "Namespace for this schema (defaults to: http://test2.com)", required = false )
    public String namespace = "http://test2.com";
    
    @Option(name = "--state", usage = "State of this schema. One of DRAFT, PUBLISHED, defaults to DRAFT", required = false )
    public String state = "DRAFT";
    
    @Option(name = "--visibility", usage = "Visibility of this schema. One of PRIVATE, PUBLIC, defaults to PRIVATE", required = false )
    public String visibility = "PRIVATE";
    
    @Override
    public void execute(MscrApiConfiguration mscrApiConfig) throws Exception {   
        try {
            MscrSchemaUploadRequest uploadMetadata =
                MscrSchemaUploadRequest.builder()
                    .addNamespace(namespace)                
                    .addLabel(lang, name)
                    .addLanguage(lang)
                    .setVisibility(visibility)
                    .setState(state) 
                    .build();

            File file = new File(schemaFile);

            MscrClient client = new MscrClientImpl(mscrApiConfig);
            String newSchemaPid = client.uploadSchema(uploadMetadata, file);
            System.err.println("Crosswalk succesfully uploaded");
            System.out.println(newSchemaPid);
        } catch(Exception ex) {
            //System.err.println("Failed to upload crosswalk");
            throw ex;
        }
    }
}
