package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.CancelEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.EventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.SaveEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmAction;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Event;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.EventType;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Listener;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.AbstractField;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.ComposedField;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.InputValidator;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.VcrTextField;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author wilelb
 */
public class ReferencesEditor extends ComposedField {
    private static Logger logger = LoggerFactory.getLogger(ReferencesEditor.class);
    
    private final List<ReferenceJob> references = new CopyOnWriteArrayList<>();
    private IModel<String> data = new Model<>();
    
    final Label lblNoReferences;
    final ListView listview;
    
    private transient Worker worker = new Worker();
    
    private int edit_index = -1;
    
    private final ReferenceEditor editor;

    private final ModalConfirmDialog localDialog;
    
    private final int workerSleepTime = 1000;
    private final int uiRefreshTimeInSeconds = 60;
    
    public class Validator implements InputValidator, Serializable {
        private String message = "";
            
            @Override
            public boolean validate(String input) {
                try {
                    //URI.create(input); 
                    new URL(input);
                } catch(MalformedURLException ex) {
                    message = ex.getMessage();
                    return false;
                }
                return true;
            }

            @Override
            public String getErrorMessage() {
                return message;
            }
    }
    
    public ReferencesEditor(String id, String label) {
        super(id, label, null);
        setOutputMarkupId(true);

        final WebMarkupContainer editorWrapper = new WebMarkupContainer("ref_editor_wrapper");
        editorWrapper.setOutputMarkupId(true);
        
        final WebMarkupContainer ajaxWrapper = new WebMarkupContainer("ajaxwrapper");
        ajaxWrapper.setOutputMarkupId(true);
        
        localDialog = new ModalConfirmDialog("modal");
        localDialog.addListener(new Listener() {
            @Override
            public void handleEvent(final Event event) {
                switch(event.getType()) {
                    case OK:                        
                            logger.info("Default confirm");
                            event.updateTarget(ajaxWrapper);
                        break;                        
                    case CONFIRMED_DELETE:
                            if(event.getData() == null) {
                                logger.trace("No reference found for removal");
                            } else {
                                Resource r = (Resource)event.getData();
                                logger.trace("Removing reference: {}", r.getRef());
                                for(int i = 0; i < references.size(); i++) {
                                    String value = references.get(i).getReference().getRef();
                                    if(value.equalsIgnoreCase(r.getRef())) {
                                        references.remove(i);                                        
                                        event.getAjaxRequestTarget().add(ajaxWrapper);
                                        event.getAjaxRequestTarget().add(editorWrapper);
                                    }                                
                                }
                            }
                            event.updateTarget(ajaxWrapper);
                        break;
                    case CANCEL: 
                            event.updateTarget();
                        break;
                }
            }
        });
        add(localDialog);
        
        editor = new ReferenceEditor("ref_editor", this, new SaveEventHandler() {
            @Override
            public void handleSaveEvent() {
                edit_index = -1;
                editor.setVisible(false);
            }
        }, new CancelEventHandler() {
            @Override
            public void handleCancelEvent() {
                edit_index = -1;
                editor.setVisible(false);
            }
        });
        editor.setVisible(false);
        editorWrapper.add(editor);
        add(editorWrapper);
        
        lblNoReferences = new Label("lbl_no_references", "No resources found.");
        
        listview = new ListView("listview", references) {
            @Override
            protected void populateItem(ListItem item) {
                ReferenceJob ref = (ReferenceJob)item.getModel().getObject();
                ReferencePanel c = new ReferencePanel("pnl_reference", ref);
                c.addEventHandler(new EventHandler<Resource>() {
                    @Override
                    public void handleEditEvent(Resource t, AjaxRequestTarget target) {
                        logger.trace("Edit reference: {}", t.getRef());
                        edit_index = -1;
                        for(int i = 0; i < references.size(); i++) {
                            String value = references.get(i).getReference().getRef();
                            if(value.equalsIgnoreCase(t.getRef())) {
                                edit_index = i;
                                break;
                            }
                        }
                        
                        if(edit_index < 0) {
                            editor.setVisible(false);
                            editor.reset();
                        } else {
                            editor.setReference(references.get(edit_index).getReference());
                            /*
                            editor.setReferenceModels(
                                    references.get(edit_index).getReference(), 
                                    new PropertyModel<String>(references.get(edit_index).getReference(), "title"), 
                                    new PropertyModel<String>(references.get(edit_index).getReference(), "description"));
                            */
                            editor.setVisible(true);
                        }
                        target.add(editorWrapper);
                    }

                    @Override
                    public void handleRemoveEvent(Resource t, AjaxRequestTarget target) {
                        String title = "Confirm removal";
                        String body = "Confirm removal of reference: "+t.getLabel();
                        localDialog.update(title, body);                                
                        localDialog.setModalConfirmAction(
                            new ModalConfirmAction<>(
                                EventType.CONFIRMED_DELETE,
                                t));
                        target.add(localDialog);
                        localDialog.show(target);
                    }
                }); 
                item.add(c);
            }
        };

        ajaxWrapper.add(new AbstractAjaxTimerBehavior(Duration.seconds(uiRefreshTimeInSeconds)) {
            @Override
            protected void onTimer(AjaxRequestTarget target) {
                if(target != null) {
                    target.add(ajaxWrapper);                
                }
            }
        });
        ajaxWrapper.add(listview);
       
        lblNoReferences.setVisible(references.isEmpty());
        listview.setVisible(!references.isEmpty());
        
        ajaxWrapper.add(lblNoReferences);
        ajaxWrapper.add(listview);
        add(ajaxWrapper);
        
        //add(lblNoReferences);
        //add(listview);
        
        AbstractField f1 = new VcrTextField("reference", "", "Add new reference by URL or PID", data, this);
        f1.setCompleteSubmitOnUpdate(true);
        f1.addValidator(new Validator());
        add(f1);
    }
    
    @Override
    protected void onRemove() {
        logger.info("Removing Reference editor");
        worker.stop();
    }

    @Override
    public boolean completeSubmit(AjaxRequestTarget target) {
        logger.info("Completing reference submit: value="+data.getObject());
        
        String value = data.getObject();
        if(value != null && !value.isEmpty()) {
            if(handleUrl(value)) {
                references.add(new ReferenceJob(new Resource(Resource.Type.RESOURCE, value)));
                data.setObject("");
            } else if(handlePid(value)) {
                references.add(new ReferenceJob(new Resource(Resource.Type.RESOURCE, value)));
                data.setObject("");
            } else {
//                references.add(new ReferenceJob(new UnkownReference(value, "Not a valid URL or PID.")));
            }

            if(worker == null || !worker.isRunning()) {
                worker = new Worker();
                worker.start();
                new Thread(worker).start();
                logger.info("Worker thread started");
            }
            
            if(target != null) {
                lblNoReferences.setVisible(references.isEmpty());
                listview.setVisible(!references.isEmpty());
                target.add(this);
            }
        }
        return false;
    }
    
    private boolean handleUrl(String value) {
        boolean result = false;
        try {
            URL url = new URL(value);
            result = true;
        } catch(MalformedURLException ex) {
            logger.debug("Failed to parse value: "+value+" as url", ex);
        }
        return result;
    }
    
    private boolean handlePid(String value) {
        return false;
    }
    
    public void reset() {
        editor.setVisible(false);
        editor.reset();
        references.clear();
    }
    
    public enum State {
        INITIALIZED, ANALYZING, DONE, FAILED
    }
    
    public List<Resource> getData() {
        List<Resource> result = new ArrayList<>();
        for(ReferenceJob job : references) {
            result.add(job.getReference());
        }
        return result;
    }
    
    public void setData(List<Resource> data) {
        logger.info("Set resource data: {} resources", data.size());
        for(Resource r : data) {
            this.references.add(new ReferenceJob(r));
        }
    }
    
    public class ReferenceJob implements Serializable {
        private Resource ref;
        private State state;
        
        public ReferenceJob(Resource ref) {
            this.ref = ref;
            this.state = State.INITIALIZED;
        }
        
        public State getState() {
            return this.state;
        }
        
        public synchronized void setState(State newState){
            this.state = newState;
        }
        
        public Resource getReference() {
            return this.ref;
        }
    }
    
    public class Worker implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(Worker.class);
        
        private boolean running = false;
        
        public Worker() {}
        
        public void start() {
            this.running = true;
        }
        
        public synchronized void stop() {
            this.running = false;
        }
        
        public boolean isRunning() {
            return this.running;
        }
        
        @Override
        public void run() {
            while(running) {
                try {
                    Thread.sleep(workerSleepTime);
                } catch(InterruptedException ex) {
                    logger.error("", ex);
                }
                
                synchronized(this){
                    for(ReferenceJob job : references) {
                        if(job.getState() == State.INITIALIZED) {
                            job.setState(State.ANALYZING);
                            try {
                                analyze(job);
                                job.setState(State.DONE);
                            } catch(Exception ex) {
                                job.setState(State.FAILED);
                            }   
                        }
                    }
                }
            }
            logger.info("Worker thread finished");
        }
        
        
        private void analyze(final ReferenceJob job) throws IOException {
            logger.info("Analyzing");
            
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                HttpGet httpget = new HttpGet(job.getReference().getRef());
                logger.info("Executing request " + httpget.getRequestLine());

                // Create a custom response handler
                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                    @Override
                    public String handleResponse(
                            final HttpResponse response) throws ClientProtocolException, IOException {
                        for(Header h : response.getHeaders("Content-Type")) {
                            logger.info(h.getName() + " - " + h.getValue());
                            
                            String[] parts = h.getValue().split(";");
                            String mediaType = parts[0];
                            
                            logger.info("Media-Type="+mediaType);
                            if(parts.length > 1) {
                                String p = parts[1].trim();
                                if(p.startsWith("charset=")) {
                                    logger.info("Charset="+p.replaceAll("charset=", ""));
                                } else if(p.startsWith("boundary=")) {
                                    logger.info("Boundary="+p.replaceAll("boundary=", ""));
                                }
                            }

                            job.getReference().setMimetype(mediaType);
                        }
                        for(Header h : response.getHeaders("Content-Length")) {
                            logger.info(h.getName() + " - " + h.getValue());
                        }
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200 && status < 300) {
                            HttpEntity entity = response.getEntity();
                            job.getReference().setCheck("HTTP "+status+"/"+response.getStatusLine().getReasonPhrase());
                            String body = entity != null ? EntityUtils.toString(entity) : null;
                            
                            if(body != null) {
                                String type = job.getReference().getMimetype();
                                if(type.equalsIgnoreCase("application/x-cmdi+xml")) {
                                    try {
                                        parseCmdi(body, job);
                                    } catch(IOException | ParserConfigurationException | XPathExpressionException | SAXException ex) {
                                        logger.error("Failed to parse CMDI", ex);
                                    }
                                } else if(job.getReference().getMimetype().equalsIgnoreCase("text/xml") &&
                                    body.contains("xmlns=\"http://www.clarin.eu/cmd/\"")) {
                                    try {
                                        parseCmdi(body, job);
                                    } catch(IOException | ParserConfigurationException | XPathExpressionException | SAXException ex) {
                                        logger.error("Failed to parse CMDI", ex);
                                    }
                                }
                            }
                            return body;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }

                };
                String responseBody = httpclient.execute(httpget, responseHandler);
            } finally {
                httpclient.close();
            }
        
            try {
                    Thread.sleep(1000);
                } catch(InterruptedException ex) {
                    logger.error("", ex);
                }
        }
    }

    private void parseCmdi(final String xml, final ReferenceJob job) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        logger.info("Parsing CMDI");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

        String profile = getValueForXPath(doc, "//default:CMD/default:Header/default:MdProfile/text()");
        logger.info("CMDI profile = " + profile);
        
        String name = getValueForXPath(doc, "//default:CMD/default:Components/default:lat-session/default:Name/text()");
        String description = getValueForXPath(doc, "//default:CMD/default:Components/default:lat-session/default:descriptions/default:Description[lang('eng')]/text()");
        logger.info("Name = " + name + ", description = " + description);
        
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
