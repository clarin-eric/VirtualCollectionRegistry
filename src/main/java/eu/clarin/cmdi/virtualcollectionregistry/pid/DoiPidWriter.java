package eu.clarin.cmdi.virtualcollectionregistry.pid;

import de.uni_leipzig.asv.clarin.webservices.pidservices2.Configuration;
import org.apache.commons.httpclient.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class DoiPidWriter {

    private static final Logger logger = LoggerFactory.getLogger(DoiPidWriter.class);

    private final CloseableHttpClient httpclient = HttpClientBuilder.create().build();

    //private final DoiRequestBuilder requestBuilder = new DoiRequestBuilder();

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

        URL url  = new URL(configuration.getServiceBaseURL());
        HttpHost targetHost = new HttpHost(url.getHost(), url.getPort(), url.toURI().getScheme());

        //Configure preemptive authentication
        //  https://stackoverflow.com/a/21592593
        //  http://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(configuration.getUser(), configuration.getPassword()));

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        try {
            HttpPost httppost = new HttpPost("/dois");

            httppost.setHeader("Accept", "application/json");
            httppost.setHeader("Content-type", "application/vnd.api+json");
            httppost.setEntity(new StringEntity(requestJsonBody));

            logger.debug("Executing request: host uri=" + targetHost.toURI()+", request="+httppost.getRequestLine());
            logger.debug("Username={}, password={}", configuration.getUser(), "xxxxxxxxx");
            logger.debug("Request entity json: {}", requestJsonBody);
            logger.debug("Request entity: {}", httppost.getEntity().toString());

            CloseableHttpResponse response = httpclient.execute(targetHost, httppost, context);
            try {
                logger.debug("----------------------------------------");
                logger.debug(response.getStatusLine().toString());
                //HTTP/1.1 201 Created
                //logger.info(EntityUtils.toString(response.getEntity()));

                String responseBody = EntityUtils.toString(response.getEntity());
                logger.debug("Datacite api response: {}", responseBody);
                doi = DoiResponse.parseDoiFromResponse(responseBody);
                logger.debug("DOI minted: {}", doi);
            } finally {
                response.close();
            }
        } catch(Exception ex) {
            throw new IOException("Failed to communicate with DOI API", ex);
        } finally {
            try {
                httpclient.close();
            } catch(IOException ex) {
                throw new IOException("Failed to close DOI API http client", ex);
            }
        }

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
}
