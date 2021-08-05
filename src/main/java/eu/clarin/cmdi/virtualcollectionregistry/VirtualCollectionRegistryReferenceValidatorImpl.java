package eu.clarin.cmdi.virtualcollectionregistry;

import eu.clarin.cmdi.virtualcollectionregistry.config.VcrConfig;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references.ReferencesEditor;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
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
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class VirtualCollectionRegistryReferenceValidatorImpl implements VirtualCollectionRegistryReferenceValidator, InitializingBean {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionRegistryReferenceValidatorImpl.class);

    private final transient List<VirtualCollectionRegistryReferenceValidationJob> jobs = new CopyOnWriteArrayList<>();

    private final transient List<ReferenceParser> parsers = new LinkedList<>();

    private boolean running = false;

    private final CloseableHttpClient httpclient;
    private RequestConfig requestConfig;

    @Autowired
    private VcrConfig vcrConfig;

    public VirtualCollectionRegistryReferenceValidatorImpl() {
        this.parsers.add(new CmdiReferenceParserImpl());
        this.httpclient = HttpClients.createDefault();
        this.requestConfig = RequestConfig
                                .custom()
                                .setConnectionRequestTimeout(1000)
                                .setMaxRedirects(5)
                                .build();
    }

    // called by Spring directly after Bean construction
    @Override
    public void afterPropertiesSet() {
        this.requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(vcrConfig.getHttpTimeout())
                .setMaxRedirects(vcrConfig.getHttpRedirects())
                .build();
    }

    @Override
    public void perform(long now) {
        if(!running) {
            running = true;
            for (VirtualCollectionRegistryReferenceValidationJob job : jobs) {
                if (job.getState().getState() == ReferencesEditor.State.INITIALIZED) {
                    job.setState(ReferencesEditor.State.ANALYZING);
                    try {
                        analyze(job);
                        job.setState(ReferencesEditor.State.DONE);
                    } catch (Exception ex) {
                        job.setState(ReferencesEditor.State.FAILED, ex.getMessage());
                    }
                }
            }
            running = false;
        }
    }

    @Override
    public ReferencesEditor.State getState(String id) {
        for(VirtualCollectionRegistryReferenceValidationJob job : jobs) {
            if(job.getId().equalsIgnoreCase(id)) {
                return job.getState().getState();
            }
        }
        return ReferencesEditor.State.FAILED;
    }

    @Override
    public void setState(String id, ReferencesEditor.State state) {
        for(VirtualCollectionRegistryReferenceValidationJob job : jobs) {
            if(job.getId().equalsIgnoreCase(id)) {
                job.setState(state);
            }
        }
    }

    @Override
    public void addReferenceValidationJob(String id, Resource r) {
        VirtualCollectionRegistryReferenceValidationJob job = new VirtualCollectionRegistryReferenceValidationJob(r, id);
        jobs.add(job);
    }

    @Override
    public void removeReferenceValidationJob(String id) {
        for(int i = 0; i < jobs.size(); i++) {
            VirtualCollectionRegistryReferenceValidationJob job = jobs.get(i);
            if(job.getId().equalsIgnoreCase(id)) {
                jobs.remove(i);
            }
        }
    }

    @Override
    public List<VirtualCollectionRegistryReferenceValidationJob> getJobs() {
        return jobs;
    }

    /**
     * Try to fetch the reference via HTTP and set it's state based on the response.
     *
     * TODO: currently this runs in one thread, so resource validation might impact creation of collections (collection
     * can only be saved  after validating all resources). Consider alternative approach with multiple threads doing the
     * work in the background.
     *
     * @param job
     * @throws IOException
     */
    private void analyze(final VirtualCollectionRegistryReferenceValidationJob job) throws IOException {
        logger.trace("Analyzing: {}", job.getReference().getRef());

        HttpGet httpget = new HttpGet(job.getReference().getRef());
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

                    job.getReference().setMimetype(mediaType);
                }

                for(Header h : response.getHeaders("Content-Length")) {
                    logger.trace(h.getName() + " - " + h.getValue());
                }

                job.setHttpResponseReason(response.getStatusLine().getReasonPhrase());
                job.setHttpResponseCode(response.getStatusLine().getStatusCode());

                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    job.getReference().setCheck("HTTP "+status+"/"+response.getStatusLine().getReasonPhrase());
                    String body = entity != null ? EntityUtils.toString(entity) : null;

                    if(body != null) {
                        for(ReferenceParser parser : parsers) {
                            if(parser.parse(body, job)) {
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
        httpclient.execute(httpget, responseHandler);
    }

    public interface ReferenceParser {
        boolean parse(final String xml, final VirtualCollectionRegistryReferenceValidationJob job);
    }

    public class CmdiReferenceParserImpl implements ReferenceParser {
        @Override
        public boolean parse(final String xml, final VirtualCollectionRegistryReferenceValidationJob job) {
            boolean handled = false;
            final String type = job.getReference().getMimetype();

            try {
                if(type.equalsIgnoreCase("application/x-cmdi+xml")) {
                    parseCmdi(xml, job);
                } else if(job.getReference().getMimetype().equalsIgnoreCase("text/xml") && xml.contains("xmlns=\"http://www.clarin.eu/cmd/\"")) {
                    parseCmdi(xml, job);
                }
            } catch(IOException | ParserConfigurationException | XPathExpressionException | SAXException ex) {
                logger.error("Failed to parse CMDI", ex);
            }

            return handled;
        }

        private void parseCmdi(final String xml, final VirtualCollectionRegistryReferenceValidationJob job) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
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

            if(name != null) {
                job.getReference().setLabel(name);
            }
            if(description != null) {
                job.getReference().setDescription(description);
            }
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