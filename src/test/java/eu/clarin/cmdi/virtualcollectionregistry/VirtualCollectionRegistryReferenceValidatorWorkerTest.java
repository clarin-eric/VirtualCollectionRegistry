package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryReferenceValidatorWorker;
import org.apache.http.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.wicket.util.string.Strings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.Spy;
import org.mockito.MockitoAnnotations;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryReferenceValidatorWorker.WorkerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *  Get actionable form:
 *      hdl:21.11114/COLL-0000-000B-CAD0-6 -- rewrite into -->  https://hdl.handle.net/21.11114/COLL-0000-000B-CAD0-6
 *
 *  Redirect chain
 *      https://hdl.handle.net/21.11114/COLL-0000-000B-CAD0-6 -->
 *          http://cls.ru.nl/registry/cbmetadata_00060 -->
 *              https://cls.ru.nl/registry/cbmetadata_00060
 *
 *> GET /21.11114/COLL-0000-000B-CAD0-6 HTTP/2
 * > Host: hdl.handle.net
 * > User-Agent: curl/7.54.0
 * > Accept: * /*
 *
 * < HTTP/2 302
 * < location: http://cls.ru.nl/registry/cbmetadata_00008
 * < expires: Wed, 18 May 2022 11:51:04 GMT
 * < content-type: text/html;charset=utf-8
 * < content-length: 169
 * < date: Tue, 17 May 2022 11:51:03 GMT
 *
 */
public class VirtualCollectionRegistryReferenceValidatorWorkerTest {

    private final Logger logger = LoggerFactory.getLogger(VirtualCollectionRegistryReferenceValidatorWorkerTest.class);

    private final RequestConfig config;

    public VirtualCollectionRegistryReferenceValidatorWorkerTest() {
        config = RequestConfig
                .custom()
                .setConnectionRequestTimeout(1000)
                .setMaxRedirects(5)
                .build();
    }

   private Header[] buildHeaders(String[] headerStrings) {
        Header[] headers = new Header[headerStrings.length];
        for(int i = 0; i < headerStrings.length; i++) {
            String headerString = headerStrings[i];
            String[] parts = Strings.split(headerString, ':');
            headers[i] = new Header() {
                @Override
                public String getName() {
                    return parts[0];
                }

                @Override
                public String getValue() {
                    return parts[1];
                }

                @Override
                public HeaderElement[] getElements() throws ParseException {
                    return new HeaderElement[0];
                }
            };
        }
        return headers;
    }

    private class MockHttpClientBuilder {
        private List<MockRequestWithResponse> requests = new LinkedList<>();

        public MockHttpClientBuilder add(MockRequestWithResponse r) {
            requests.add(r);
            return this;
        }

        public CloseableHttpClient build() throws Exception {
            CloseableHttpClient client = spy(CloseableHttpClient.class);
            for(MockRequestWithResponse r : requests) {
                CloseableHttpResponse response = r.getMockedResponse();
                when(client.execute(any(HttpHost.class), any(HttpRequest.class), (HttpContext) isNull())).thenReturn(response);
            }

            return client;
        }
    }

    private class MockRequestWithResponse {
        private String url;
        private int httpResponseCode;
        private String httpResponseMsg;

        private Map<String, Header[]> requestHeaders = new HashMap<>();
        private Map<String, Header[]> responseHeaders = new HashMap<>();

        private StatusLine getMockedStatusLine() {
            StatusLine mockedStatus = mock(StatusLine.class);
            when(mockedStatus.getReasonPhrase()).thenReturn(httpResponseMsg);
            when(mockedStatus.getStatusCode()).thenReturn(httpResponseCode);
            return mockedStatus;
        }

        private HttpEntity getMockedEntity() {
            HttpEntity entity = mock(HttpEntity.class);
            return entity;
        }

        public HttpRequest getMockedRequest() {
            HttpRequest r = mock(HttpRequest.class);
            return r;
        }

        public CloseableHttpResponse getMockedResponse() {
            StatusLine mockedStatusLine = getMockedStatusLine();
            HttpEntity mockedEntity = getMockedEntity();

            CloseableHttpResponse response = mock(CloseableHttpResponse.class);
            when(response.getStatusLine()).thenReturn(mockedStatusLine);
            when(response.getEntity()).thenReturn(mockedEntity);
            for(String header : responseHeaders.keySet()) {
                when(response.getHeaders(header)).thenReturn(responseHeaders.get(header));
            }

            return response;
        }

        public MockRequestWithResponse forUrl(String url) {
            this.url = url;
            return this;
        }

        public MockRequestWithResponse addRequestHeader(String h) {
            Header[] headers = buildHeaders(new String[] {h});
            if(headers.length > 0) {
                requestHeaders.put(headers[0].getName(), headers);
            }
            return this;
        }

        public MockRequestWithResponse addResponseHeader(String h) {
            Header[] headers = buildHeaders(new String[] {h});
            if(headers.length > 0) {
                responseHeaders.put(headers[0].getName(), headers);
            }
            return this;
        }

        public MockRequestWithResponse setResponseStatus(int code, String msg) {
            this.httpResponseCode = code;
            this.httpResponseMsg = msg;
            return this;
        }

        public MockRequestWithResponse setResponseBody() {
            return this;
        }
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testStandardURL() throws Exception {
        CloseableHttpClient client = new MockHttpClientBuilder()
            .add(
                new MockRequestWithResponse()
                    .forUrl("https://cls.ru.nl/registry/cbmetadata_00060")
                    .addResponseHeader("Content-Type: text/html; charset=utf-8")
                    .addResponseHeader("Content-Length: 1000")
                    .setResponseStatus(200, "OK")
                    .setResponseBody()
            ).build();



        WorkerResult result = new VirtualCollectionRegistryReferenceValidatorWorker(client, config).doWork("https://cls.ru.nl/registry/cbmetadata_00060");
        assertEquals("", 200, result.getHttpResponseCode());
    }

    @Test
    public void testHandleURL() throws Exception {
        CloseableHttpClient client = new MockHttpClientBuilder()
                .add(
                        new MockRequestWithResponse()
                                .forUrl("https://hdl.handle.net/21.11114/COLL-0000-000B-CAD0-6")
                                .addResponseHeader("Location: http://cls.ru.nl/registry/cbmetadata_00008")
                                .addResponseHeader("Content-Type: text/html;charset=utf-8")
                                .addResponseHeader("Content-Length: 169")
                                .setResponseStatus(302, "FOUND")
                                .setResponseBody()
                ).build();

        WorkerResult result = new VirtualCollectionRegistryReferenceValidatorWorker(client, config).doWork("https://hdl.handle.net/21.11114/COLL-0000-000B-CAD0-6");
        assertEquals("", 302, result.getHttpResponseCode());
    }

    @Test
    public void testHandleURN() throws Exception {
        CloseableHttpClient client = new MockHttpClientBuilder()
                .add(
                        new MockRequestWithResponse()
                                .forUrl("https://hdl.handle.net/21.11114/COLL-0000-000B-CAD0-6")
                                .addResponseHeader("Location: http://cls.ru.nl/registry/cbmetadata_00008")
                                .addResponseHeader("Content-Type: text/html;charset=utf-8")
                                .addResponseHeader("Content-Length: 169")
                                .setResponseStatus(302, "FOUND")
                                .setResponseBody()
                ).build();

        WorkerResult result = new VirtualCollectionRegistryReferenceValidatorWorker(client, config).doWork("hdl:21.11114/COLL-0000-000B-CAD0-6");
        assertEquals("", 302, result.getHttpResponseCode());
    }
}
