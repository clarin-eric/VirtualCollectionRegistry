package eu.clarin.cmdi.virtualcollectionregistry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class VirtualCollectionRegistryReferenceValidatorWorker {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionRegistryReferenceValidatorWorker.class);

    private final transient List<ReferenceParser> parsers = new LinkedList<>();

    private String session = null;
    private Thread t;

    public VirtualCollectionRegistryReferenceValidatorWorker() {
        this.parsers.add(new CmdiReferenceParserImpl());
    }

    public void setSession(String sessionId) {
        this.session = sessionId;
    }

    public String getSessionId() {
        return session;
    }

    public class WorkerResult {
        private int httpResponseCode;
        private String httpResponseMsg;
        private String mimeType;

        public int getHttpResponseCode() {
            return httpResponseCode;
        }

        public void setHttpResponseCode(int httpResponseCode) {
            this.httpResponseCode = httpResponseCode;
        }

        public String getHttpResponseMsg() {
            return httpResponseMsg;
        }

        public void setHttpResponseMsg(String httpResponseMsg) {
            this.httpResponseMsg = httpResponseMsg;
        }

        public String getMimeType() {
            return mimeType;
        }

        public void setMimeType(String mimeType) {
            this.mimeType = mimeType;
        }
    }

    public WorkerResult doWork(String ref) {
        try {
            return analyze(ref);
        } catch (Exception ex) {

        }

        return new WorkerResult();
        /*
        session = job.getSessionId();
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                job.setState(ReferencesEditor.State.ANALYZING);
                try {
                    logger.debug("Analysing: session="+job.getSessionId()+", job="+job.getId()+", url="+job.getReference().getRef());
                    analyze(job);
                    job.setState(ReferencesEditor.State.DONE);
                    logger.debug("Analysing done: job="+job.getId());
                } catch (Exception ex) {
                    job.setState(ReferencesEditor.State.FAILED, ex.getMessage());
                    logger.debug("Analysing failed: job="+job.getId());
                } finally {
                    session = null;
                }
            }
        }, "Session-" + session + "-worker");
        t.start();
         */
    }

    /**
     * Try to fetch the reference via HTTP and set it's state based on the response.
     *
     * TODO: currently this runs in one thread, so resource validation might impact creation of collections (collection
     * can only be saved  after validating all resources). Consider alternative approach with multiple threads doing the
     * work in the background.
     *
     * @param ref
     * @throws IOException
     */
    private WorkerResult analyze(final String ref) throws IOException {
        WorkerResult result = new WorkerResult();

        //TODO: initialize only once, but take care of serialization / storage in session
        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(1000)
                .setMaxRedirects(5)
                .build();

        HttpGet httpget = new HttpGet(ref);
        httpget.setConfig(requestConfig);

        logger.trace("Executing request " + httpget.getRequestLine());

        // Create a custom response handler
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                for(Header h : response.getHeaders("Content-Type")) {
                    logger.trace(h.getName() + " - " + h.getValue());

                    String[] parts = h.getValue().split(";");
                    String mediaType = parts[0];

                    logger.trace("Media-Type="+mediaType);
                    if(parts.length > 1) {
                        String p = parts[1].trim();
                        if(p.startsWith("charset=")) {
                            logger.trace("Charset="+p.replaceAll("charset=", ""));
                        } else if(p.startsWith("boundary=")) {
                            logger.trace("Boundary="+p.replaceAll("boundary=", ""));
                        }
                    }

                    result.setMimeType(mediaType);
                }

                for(Header h : response.getHeaders("Content-Length")) {
                    logger.trace(h.getName() + " - " + h.getValue());
                }

                result.setHttpResponseMsg(response.getStatusLine().getReasonPhrase());
                result.setHttpResponseCode(response.getStatusLine().getStatusCode());

                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    String body = entity != null ? EntityUtils.toString(entity) : null;

                    if(body != null) {
                        for(ReferenceParser parser : parsers) {
                            if(parser.parse(body, result.getMimeType())) {
                                break; //exit loop if the parser processed the reference
                            }
                        }
                    }

                    return body;
                } else {
                    throw new ClientProtocolException("Unexpected response status: HTTP " + status + " - " + response.getStatusLine().getReasonPhrase());
                }
            }
        };

        //String responseBody = httpclient.execute(httpget, responseHandler);
        CloseableHttpClient httpclient = HttpClients.createDefault(); //TODO: initialize only once, but take care of serialization / storage in session
        httpclient.execute(httpget, responseHandler);

        return result;
    }

    public interface ReferenceParser {
        boolean parse(final String xml, final String mimeType);
    }

    public class CmdiReferenceParserImpl implements ReferenceParser {
        @Override
        public boolean parse(final String xml, final String mimeType) {
            boolean handled = false;

            try {
                if(mimeType.equalsIgnoreCase("application/x-cmdi+xml")) {
                    parseCmdi(xml);
                } else if(mimeType.equalsIgnoreCase("text/xml") && xml.contains("xmlns=\"http://www.clarin.eu/cmd/\"")) {
                    parseCmdi(xml);
                }
            } catch(IOException | ParserConfigurationException | XPathExpressionException | SAXException ex) {
                logger.error("Failed to parse CMDI", ex);
            }

            return handled;
        }

        private void parseCmdi(final String xml) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
            logger.trace("Parsing CMDI");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

            String profile = getValueForXPath(doc, "//default:CMD/default:Header/default:MdProfile/text()");
            logger.trace("CMDI profile = " + profile);

            String name = getValueForXPath(doc, "//default:CMD/default:Components/default:lat-session/default:Name/text()");
            String description = getValueForXPath(doc, "//default:CMD/default:Components/default:lat-session/default:descriptions/default:Description[lang('eng')]/text()");
            logger.trace("Name = " + name + ", description = " + description);

            /*
            if(name != null) {
                job.getReference().setLabel(name);
            }
            if(description != null) {
                job.getReference().setDescription(description);
            }

             */
        }

        /**
         * Return the first value of the xpath query result, or null if the result is
         * empty
         *
         * @param doc
         * @param xpathQuery
         * @return
         */
        private String getValueForXPath(Document doc, String xpathQuery) {
            List<String> result = getValuesForXPath(doc, xpathQuery);
            if(result.isEmpty()) {
                return null;
            }
            return result.get(0);
        }

        /**
         * Return all values for the xpath query
         *
         * @param doc
         * @param xpathQuery
         * @return
         */
        private List<String> getValuesForXPath(Document doc, String xpathQuery) {
            final XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new NamespaceContext() {
                @Override
                public String getNamespaceURI(String prefix) {
                    return prefix.equals("default") ? "http://www.clarin.eu/cmd/" : null;
                }

                @Override
                public Iterator<String> getPrefixes(String val) {
                    return null;
                }

                @Override
                public String getPrefix(String uri) {
                    return null;
                }
            });

            List<String> result = new ArrayList<>();

            try {
                XPathExpression expr = xpath.compile(xpathQuery);
                Object xpathResult = expr.evaluate(doc, XPathConstants.NODESET);
                NodeList nodes = (NodeList) xpathResult;
                logger.trace("XPatch query = ["+xpathQuery+"], result nodelist.getLength() = "+nodes.getLength());
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node currentItem = nodes.item(i);
                    logger.trace("found node -> " + currentItem.getLocalName() + " (namespace: " + currentItem.getNamespaceURI() + "), value = " + currentItem.getNodeValue());
                    result.add(currentItem.getNodeValue());
                }
            } catch(XPathExpressionException ex) {
                logger.error("XPath query ["+xpathQuery+"] failed.", ex);
            }

            return result;
        }
    }
}
