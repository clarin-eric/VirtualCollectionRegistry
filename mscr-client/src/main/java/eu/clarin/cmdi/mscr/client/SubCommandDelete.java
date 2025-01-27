package eu.clarin.cmdi.mscr.client;

import eu.clarin.cmdi.mscr.client.lib.MscrApiConfiguration;
import eu.clarin.cmdi.mscr.client.lib.MscrClient;
import eu.clarin.cmdi.mscr.client.lib.impl.MscrClientImpl;
import org.kohsuke.args4j.Option;

/**
 *
 * @author wilelb
 */
public class SubCommandDelete implements MscrCliSubCommand {

    @Option(name = "--id", usage = "Id for the schema to delete (id = pid or handle),", required = true )
    public String id = "";
    
    @Override
    public void execute(MscrApiConfiguration mscrApiConfig) throws Exception {
        try {
            MscrClient client = new MscrClientImpl(mscrApiConfig);
            if(!client.remmoveSchema(id)) {
                throw new Exception("Failed to remove crossewalk with id="+id);
            }
            
            System.err.println("Crosswalk with id="+id+" succesfully removed");
        } catch(Exception ex) {
            throw ex;
        }
    }   
}
