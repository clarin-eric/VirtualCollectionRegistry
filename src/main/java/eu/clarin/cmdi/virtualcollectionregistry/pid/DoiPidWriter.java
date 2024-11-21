package eu.clarin.cmdi.virtualcollectionregistry.pid;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.utils.Base64;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class DoiPidWriter {

    private static final Logger logger = LoggerFactory.getLogger(DoiPidWriter.class);

    /**
     * References:
     * - https://www.baeldung.com/apache-httpclient-cookbook
     * - https://www.baeldung.com/httpclient-basic-authentication
     */
    private CloseableHttpClient httpclient = HttpClientBuilder.create().build();

    public String registerNewPID(Configuration configuration, PidRequest doiRequest) throws HttpException {
        String generated_pid = null;
        try {
            String json_data = doiRequest.toJsonString();
            generated_pid = doRequest(configuration, json_data);
        } catch(IOException | NullPointerException | URISyntaxException ex) {
            throw new HttpException("Failed to mint DOI", ex);
        }
        return generated_pid;
    }

    private String doRequest(Configuration configuration, String requestJsonBody) throws IOException, NullPointerException, URISyntaxException {
        String doi = null;

        URI uri = URI.create(configuration.getServiceBaseURL());
        HttpHost targetHost = new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort());
/*
        final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        AuthScope authScope = new AuthScope(targetHost);
        credsProvider.setCredentials(authScope, new UsernamePasswordCredentials(configuration.getUser(), configuration.getPassword().toCharArray())); 
           
        final AuthCache authCache = new BasicAuthCache();            
        authCache.put(targetHost, new BasicScheme());

        final HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
*/
        
        /*    
        if(httpclient == null) {                       
            httpclient = HttpClientBuilder.create().build();
        }
        */

        //Configure preemptive authentication
        //  https://stackoverflow.com/a/21592593
        //  http://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html
//        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
  //      credsProvider.setCredentials(
    //            new AuthScope(targetHost.getHostName(), targetHost.getPort()),
      //          new UsernamePasswordCredentials(configuration.getUser(), configuration.getPassword().toCharArray()));

        // Create AuthCache instance
        //AuthCache authCache = new BasicAuthCache();
        //authCache.put(targetHost, new BasicScheme());

        // Add AuthCache to the execution context
        //HttpClientContext context = HttpClientContext.create();
        //context.setCredentialsProvider(credsProvider);
        //context.setAuthCache(authCache);

        try {
            HttpPost httppost = new HttpPost("/dois");

            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/vnd.api+json");
            
            //Add basic authentication header
            final String auth = configuration.getUser() + ":" + configuration.getPassword();
            final byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
            final String authHeader = "Basic " + new String(encodedAuth);
            httppost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            //Add request content
            httppost.setEntity(new StringEntity(requestJsonBody, StandardCharsets.UTF_8));

            logger.debug("Executing request: host uri=" + targetHost.toURI()+", "+httppost.getMethod()+" request="+httppost.getPath());
            logger.debug("Username={}, password={}", configuration.getUser(), "xxxxxxxxx");
            logger.debug("Request entity json: {}", requestJsonBody);
            logger.debug("Request entity: {}", httppost.getEntity().toString());
            
            /*
            doi = httpclient.execute(targetHost, httppost, response -> {
                String newDoi = null;
                try {
                    logger.debug("----------------------------------------");
                    logger.debug("HTTP "+response.getCode()+" "+response.getReasonPhrase());
                    String responseBody = EntityUtils.toString(response.getEntity());
                    logger.debug("Datacite api response: {}", responseBody);
                    newDoi = DoiResponse.parseDoiFromResponse(responseBody);
                    logger.debug("DOI minted: {}", newDoi);
                } catch (Exception ex) {
                    logger.debug("Failed to parse datacite api response", ex);
                } finally {
                    response.close();
                }
                return newDoi;
            });
            */
            DoiApiResponseHandler responseHandler = new DoiApiResponseHandler();
            doi = httpclient.execute(targetHost, httppost, responseHandler);
            if(responseHandler.hasError()) {
                throw responseHandler.getError();
            }
            
        } catch(Exception ex) {
            throw new IOException("Failed to communicate with DOI API", ex);
        } /*finally {
            try {
                httpclient.close();
            } catch(IOException ex) {
                throw new IOException("Failed to close DOI API http client", ex);
            }
        }*/

        return doi;
    }
/*
    public static class DoiApiException extends Exception {
        public DoiApiException(String msg) {
            super(msg);
        }

        public DoiApiException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

 */
    class DoiApiResponseHandler extends AbstractHttpClientResponseHandler<String> {

        private Exception error = null;
        
        @Override
        public String handleEntity(HttpEntity entity) throws IOException {
            try {
                return EntityUtils.toString(entity);
            } catch (final ParseException ex) {
                throw new ClientProtocolException(ex);
            }
        }
        
        @Override
        public String handleResponse(final ClassicHttpResponse response) throws IOException {
            logger.trace("----------------------------------------");
            logger.trace("HTTP "+response.getCode()+" "+response.getReasonPhrase());
                    
            String newDoi = null;
            try {
                String responseBody = super.handleResponse(response);                                
                logger.trace("Datacite api response: {}", responseBody);
                newDoi = DoiResponse.parseDoiFromResponse(responseBody);
                logger.debug("DOI minted: {}", newDoi);
            } catch (Exception ex) {
                error = new Exception("Failed to parse datacite api response", ex);
            } finally {
                response.close();
            }
            return newDoi;
        }

        /**
         * @return the error
         */
        public Exception getError() {
            return error;
        }
        
        public boolean hasError() {
            return error != null;
        }
    }
}
