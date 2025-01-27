package eu.clarin.cmdi.mscr.client.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 *
 * @author wilelb
 */
public interface MscrClient {
    public List<MscrSchema> searchSchema(SearchBuilder search) 
        throws MscrApiException, UnsupportedEncodingException;    
    public String uploadSchema(MscrSchemaUploadRequest uploadMetadata, File file) 
        throws JsonProcessingException, MscrApiException;    
    public boolean remmoveSchema(String schemaPid) 
        throws MscrApiException;
    
    public List<MscrCrosswalk> searchCrosswalk(SearchBuilder search) 
        throws MscrApiException, UnsupportedEncodingException;    
    public String uploadCrosswalk(MscrCrosswalkUploadRequest uploadMetadata, File file) 
        throws JsonProcessingException, MscrApiException;    
    public MscrCrosswalkMetadata fetchCrosswalk(String crosswalkId) 
        throws MscrApiException;
    public String fetchCrosswalkFile(String crosswalkId, String fileId) 
        throws MscrApiException;
}
