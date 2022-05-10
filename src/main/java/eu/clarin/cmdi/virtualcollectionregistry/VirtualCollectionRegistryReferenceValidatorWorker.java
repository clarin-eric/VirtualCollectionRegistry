package eu.clarin.cmdi.virtualcollectionregistry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

    private final CloseableHttpClient httpclient = HttpClients.createDefault();
    private final RequestConfig requestConfig;

    public class WorkerResult {
        private int httpResponseCode;
        private String httpResponseMsg;
        private String mimeType;
        private String exception;

        private String nameSuggestion;
        private String descriptionSuggestion;

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

        public void setException(String exception) { this.exception = exception; }

        public void setException(Exception exception) { this.exception = exception.getMessage(); }

        public String getException() { return exception; }

        public String getNameSuggestion() {
            return nameSuggestion;
        }

        public void setNameSuggestion(String nameSuggestion) {
            this.nameSuggestion = nameSuggestion;
        }

        public String getDescriptionSuggestion() {
            return descriptionSuggestion;
        }

        public void setDescriptionSuggestion(String descriptionSuggestion) {
            this.descriptionSuggestion = descriptionSuggestion;
        }
    }

    public class ValidationResponseHandler implements ResponseHandler<String> {
        private final WorkerResult result;

        public ValidationResponseHandler() {
            this.result = new WorkerResult();
        }

        public WorkerResult getResult() {
            return this.result;
        }

        @Override
        public String handleResponse(final HttpResponse response) throws IOException {
            for (Header h : response.getHeaders("Content-Type")) {
                logger.trace(h.getName() + " - " + h.getValue());

                String[] parts = h.getValue().split(";");
                String mediaType = parts[0];

                logger.trace("Media-Type=" + mediaType);
                if (parts.length > 1) {
                    String p = parts[1].trim();
                    if (p.startsWith("charset=")) {
                        logger.trace("Charset=" + p.replaceAll("charset=", ""));
                    } else if (p.startsWith("boundary=")) {
                        logger.trace("Boundary=" + p.replaceAll("boundary=", ""));
                    }
                }

                result.setMimeType(mediaType);
            }

            int httpCode = response.getStatusLine().getStatusCode();
            String httpMessage = response.getStatusLine().getReasonPhrase();

            logger.trace("Http response: " + httpCode + " " + httpMessage);
            for (Header h : response.getHeaders("Content-Length")) {
                logger.trace(h.getName() + " - " + h.getValue());
            }

            result.setHttpResponseMsg(httpMessage);
            result.setHttpResponseCode(httpCode);
            if (httpCode >= 200 && httpCode < 300) {
                HttpEntity entity = response.getEntity();
                String body = entity != null ? EntityUtils.toString(entity) : null;

                if (body != null) {
                    for (ReferenceParser parser : parsers) {
                        if (parser.parse(body, result.getMimeType())) {
                            ReferenceParserResult parserResult = parser.getResult();
                            if(parserResult.getName() != null) {
                                result.setNameSuggestion(parserResult.getName());
                            }
                            if(parserResult.getDescription() != null) {
                                result.setDescriptionSuggestion(parserResult.getDescription());
                            }
                            break; //exit loop if the parser processed the reference
                        }
                    }
                }

                return body;
            }

            return null;
        }
    }

    public VirtualCollectionRegistryReferenceValidatorWorker() {
        this.parsers.add(new CmdiReferenceParserImpl());
        this.requestConfig =
            RequestConfig
                .custom()
                .setConnectionRequestTimeout(1000)
                .setMaxRedirects(5)
                .build();
    }

    /**
     * Try to fetch the reference via HTTP and set it's state based on the response.
     *
     * TODO: currently this runs in one thread, so resource validation might impact creation of collections (collection
     * can only be saved  after validating all resources). Consider alternative approach with multiple threads doing the
     * work in the background.
     *
     * @param ref
     */
    public WorkerResult doWork(final String ref) {
        WorkerResult result = new WorkerResult();

        try {
            logger.debug("Validating reference = "+ref);
            ValidationResponseHandler responseHandler = new ValidationResponseHandler();

            HttpGet httpget = new HttpGet(ref);
            httpget.setConfig(requestConfig);
            httpclient.execute(httpget, responseHandler);

            result = responseHandler.getResult();
        } catch(Exception ex) {
            result.setException(ex);
        }

        return result;
    }

    public interface ReferenceParser {
        boolean parse(final String xml, final String mimeType);
        ReferenceParserResult getResult();
    }

    public class ReferenceParserResult {
        private String name;
        private String description;

        public ReferenceParserResult() { }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public class CmdiReferenceParserImpl implements ReferenceParser {
        private final ReferenceParserResult result = new ReferenceParserResult();

        public ReferenceParserResult getResult() {
            return result;
        }

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

            this.result.setName(name);
            this.result.setDescription(description);
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
