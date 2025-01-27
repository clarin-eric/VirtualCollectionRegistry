
package eu.clarin.cmdi.mscr.client;

import eu.clarin.cmdi.mscr.client.lib.MscrApiConfiguration;
import java.io.FileInputStream;
import java.util.Properties;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.SubCommand;
import org.kohsuke.args4j.spi.SubCommandHandler;
import org.kohsuke.args4j.spi.SubCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class MscrClientCli {

    private final static Logger logger = LoggerFactory.getLogger(MscrClientCli.class);
    
    public final static String DEFAULT_CONFIG_FILE_LOCATION = 
        System.getProperty("user.home")+"/.config/mscr/config.properties";
    
    @Argument(required=true,index=0,metaVar="sub command",usage="subcommands, e.g., {search,upload,delete}",handler=SubCommandHandler.class)
    @SubCommands({
        @SubCommand(name="search",impl=SubCommandSearch.class),
        @SubCommand(name="upload",impl=SubCommandUpload.class),
        @SubCommand(name="delete", impl=SubCommandDelete.class),
    })
    protected MscrCliSubCommand action;
    
    public static void main(String[] args) {
        new MscrClientCli().doMain(args);
    }
    
    public void doMain(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.setUsageWidth(80);

        try {
            parser.parseArgument(args);
            
            String configFilePath = DEFAULT_CONFIG_FILE_LOCATION;
              
            final Properties props = new Properties();            
            props.load(new FileInputStream(configFilePath));
            
            logger.info("Search subcommand, loading config from {}", configFilePath);
            for(Object key : props.keySet()) {
                logger.info("{} = {}", key, props.get(key));
            }
            
            MscrApiConfiguration mscrApiConfig = 
                new MscrApiConfiguration("http://"+props.getProperty("api.host")+":"+props.getProperty("api.port")+"/v2", 
                        props.getProperty("api.key"));
                    
            action.execute(mscrApiConfig);
        } catch( Exception e ) {
            //System.err.println("Error:");
            logger.error("Error", e);
            System.err.println();
            System.err.println("Usage:");
            System.err.println("java -jar MscrClientCli [options...] arguments...");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
            System.exit(1);
        }
    }
}
