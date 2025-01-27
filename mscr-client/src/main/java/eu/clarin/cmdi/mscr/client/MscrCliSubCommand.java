package eu.clarin.cmdi.mscr.client;

import eu.clarin.cmdi.mscr.client.lib.MscrApiConfiguration;

/**
 *
 * @author wilelb
 */
public interface MscrCliSubCommand {
    /**
     * Execute a command
     * @param mscrApiConfig
     * @throws java.lang.Exception
     */
    void execute(MscrApiConfiguration mscrApiConfig) throws Exception;
}
