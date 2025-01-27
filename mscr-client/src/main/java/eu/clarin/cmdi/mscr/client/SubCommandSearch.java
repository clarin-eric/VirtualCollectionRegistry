package eu.clarin.cmdi.mscr.client;

import eu.clarin.cmdi.mscr.client.lib.MscrApiConfiguration;
import eu.clarin.cmdi.mscr.client.lib.MscrClient;
import eu.clarin.cmdi.mscr.client.lib.SearchBuilder;
import eu.clarin.cmdi.mscr.client.lib.impl.MscrClientImpl;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class SubCommandSearch implements MscrCliSubCommand {
        
    private final static Logger logger = LoggerFactory.getLogger(SubCommandSearch.class);
    
    @Option(name = "--schema", usage = "Schema search string", required = false )
    public String schemaQuery = "";
    
    @Option(name = "--crosswalk", usage = "Crosswalk search string", required = false )
    public String crosswalkQuery = "";
    
    @Override
    public void execute(MscrApiConfiguration mscrApiConfig) throws Exception {       
        try {
            MscrClient client = new MscrClientImpl(mscrApiConfig);
            
            if(!schemaQuery.isEmpty()) {
                client.searchSchema(new SearchBuilder()
                    .type(SearchBuilder.Type.SCHEMA)
                    .query(schemaQuery)
                    .namespace("http://test.com")
                );
            }
             
            if(!crosswalkQuery.isEmpty()) {
                client.searchCrosswalk(new SearchBuilder()
                    .type(SearchBuilder.Type.CROSSWALK)
                    .query(crosswalkQuery)
                    .namespace("http://test.com")
                );
            }
        } catch(Exception ex) {
            throw ex;
        }
    }   
    
}
