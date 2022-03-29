package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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

import eu.clarin.cmdi.virtualcollectionregistry.gui.HandleLinkModel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.CreateAndEditPanel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.CancelEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.EventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.MoveListEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.SaveEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.authors.AuthorsEditor;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmAction;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.DataUpdatedEvent;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Event;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.EventType;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Listener;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.*;
import eu.clarin.cmdi.virtualcollectionregistry.model.OrderableComparator;
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
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.jetbrains.annotations.NotNull;
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
public class ReferencesEditor extends ComposedField{
    private static Logger logger = LoggerFactory.getLogger(ReferencesEditor.class);
    
    private final List<ReferenceJob> references = new CopyOnWriteArrayList<>();
    private IModel<String> data = new Model<>();
    private IModel<String> mdlReferenceTitle = new Model<>();
    
    final Label lblNoReferences;
    final ListView listview;
    
    private transient Worker worker = new Worker();
    
    private int edit_index = -1;
    
    private final ReferenceEditor editor;

    private final ModalConfirmDialog localDialog;
    
    private final int workerSleepTime = 1000;
    private final int uiRefreshTimeInSeconds = 1;

    private boolean currentValidation = false;
    private boolean previousValidation = false;

    public class Validator implements InputValidator, Serializable {
        private String message = "";
            
        @Override
        public boolean validate(String input) {
            message = "";
            boolean validUrl = false;
            boolean validPid = false;

            //Try to parse url
            try {
                new URL(input);
                validUrl = true;
            } catch(MalformedURLException ex) {
                message += !message.isEmpty() ? "<br />" : "";
                message += ex.getMessage()+".";
            }

            //Try to parse handle
            if(HandleLinkModel.isSupportedPersistentIdentifier(input)) {
                validPid = true;
            } else {
                message += !message.isEmpty() ? "<br />" : "";
                message += "Not a valid persistent identifier.";
            }

            return (validUrl || validPid);
        }

        @Override
        public String getErrorMessage() {
            return message;
        }
    }
    
    public ReferencesEditor(String id, String label, Model<Boolean> advancedEditorMode, VisabilityUpdater updater) {
        super(id, "References", null, updater);
        setOutputMarkupId(true);
        Component componentToUpdate = this;

        final WebMarkupContainer editorWrapper = new WebMarkupContainer("ref_editor_wrapper");
        editorWrapper.setOutputMarkupId(true);

        final WebMarkupContainer ajaxWrapper = new WebMarkupContainer("ajaxwrapper");
        ajaxWrapper.setOutputMarkupId(true);

        localDialog = new ModalConfirmDialog("references_modal");
        localDialog.addListener(new Listener() {
            @Override
            public void handleEvent(final Event event) {
                switch(event.getType()) {
                    case OK: event.updateTarget(ajaxWrapper); break;
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
                    case CANCEL: event.updateTarget(); break;
                }
            }
        });
        add(localDialog);

        editor = new ReferenceEditor("ref_editor", this, new SaveEventHandler() {
            @Override
            public void handleSaveEvent(AjaxRequestTarget target) {
                //Reset state so this reference is rescanned
                references.get(edit_index).setState(State.INITIALIZED);
                edit_index = -1;
                editor.setVisible(false);
                listview.setVisible(true);
                if(target != null) {
                    target.add(componentToUpdate);
                }

            }
        }, new CancelEventHandler() {
            @Override
            public void handleCancelEvent(AjaxRequestTarget target) {
                edit_index = -1;
                editor.setVisible(false);
                listview.setVisible(true);
                if(target != null) {
                    target.add(componentToUpdate);
                }
            }
        }, advancedEditorMode);
        editor.setVisible(false);
        editorWrapper.add(editor);
        add(editorWrapper);

        lblNoReferences = new Label("lbl_no_references", "No references found.<br />Please add one or more members that make up this virtual collection by means of a (persistent) reference. ");
        lblNoReferences.setEscapeModelStrings(false);

        listview = new ListView("listview", references) {
            @Override
            protected void populateItem(ListItem item) {
                ReferenceJob ref = (ReferenceJob)item.getModel().getObject();
                ReferencePanel c = new ReferencePanel("pnl_reference", ref, advancedEditorMode, getMaxDisplayOrder());
                c.addMoveListEventHandler(new MoveListEventHandler() {
                    @Override
                    public void handleMoveUp(Long displayOrder, AjaxRequestTarget target) {
                        move(-1, displayOrder);
                        if (target != null) {
                            target.add(componentToUpdate);
                        }
                    }
                    @Override
                    public void handleMoveDown(Long displayOrder, AjaxRequestTarget target) {
                        move(1, displayOrder);
                        if (target != null) {
                            target.add(componentToUpdate);
                        }
                    }
                    @Override
                    public void handleMoveTop(Long displayOrder, AjaxRequestTarget target) {
                        move(0, displayOrder);
                        if (target != null) {
                            target.add(componentToUpdate);
                        }
                    }
                    @Override
                    public void handleMoveEnd(Long displayOrder, AjaxRequestTarget target) {
                        move(references.size()-1, displayOrder);
                        if (target != null) {
                            target.add(componentToUpdate);
                        }
                    }
                });
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
                            listview.setVisible(true);
                        } else {
                            editor.setReference(references.get(edit_index).getReference());
                            editor.setVisible(true);
                            listview.setVisible(false);
                        }

                        target.add(componentToUpdate);
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
                //validate(); //make sure this validation is up to date before re rendering the component
                if(target != null) {
                    target.add(ajaxWrapper);
                }
                fireEvent(new CustomDataUpdateEvent(target));
            }
        });

        ajaxWrapper.add(listview);

        lblNoReferences.setVisible(references.isEmpty());
        listview.setVisible(!references.isEmpty());

        ajaxWrapper.add(lblNoReferences);
        ajaxWrapper.add(listview);
        add(ajaxWrapper);

        AbstractField f1 = new VcrTextFieldWithoutLabel("reference", "Add new reference by URL or PID", data, this,null);
        f1.setCompleteSubmitOnUpdate(false);
        f1.setRequired(true);
        f1.addValidator(new Validator());
        add(f1);

        AbstractField f2 = new VcrTextFieldWithoutLabel("reference_title", "Set a title for this new reference", mdlReferenceTitle, this,null);
        f2.setCompleteSubmitOnUpdate(true);
        f2.setRequired(true);
        add(f2);
    }

    private long getMaxDisplayOrder() {
        long max = 0;
        for(ReferenceJob job : references) {
            if(job.getReference().getDisplayOrder() > max) {
                max = job.getReference().getDisplayOrder();
            }
        }
        return max;
    }

    private long getNextDisplayOrder() {
        if(references.size() <= 0) {
            return 0L;
        }
        return getMaxDisplayOrder() + 1;
    }

    public static class CustomDataUpdateEvent extends DataUpdatedEvent {
        public CustomDataUpdateEvent(AjaxRequestTarget target) {
            super(target);
        }
    }
    
    @Override
    protected void onRemove() {
        logger.info("Removing Reference editor");
        worker.stop();
    }

    @Override
    public boolean completeSubmit(AjaxRequestTarget target) {
        String value = data.getObject();
        String title = mdlReferenceTitle.getObject();

        logger.debug("Completing reference submit: value="+value+",title="+title);
        if(value != null && !value.isEmpty() && title != null && !title.isEmpty()) {
            if(handleUrl(value)) {
                Resource r = new Resource(Resource.Type.RESOURCE, value, title);
                r.setDisplayOrder(getNextDisplayOrder());
                references.add(new ReferenceJob(r));
                data.setObject("");
                mdlReferenceTitle.setObject("");
            } else if(handlePid(value)) {
                String actionableValue = HandleLinkModel.getActionableUri(value);
                Resource r = new Resource(Resource.Type.RESOURCE, actionableValue, title);
                r.setDisplayOrder(getNextDisplayOrder());
                references.add(new ReferenceJob(r));
                data.setObject("");
                mdlReferenceTitle.setObject("");
            } else {
                //abort
                logger.warn("Unhandled reference (not url AND not pid)");
                fireEvent(new DataUpdatedEvent(target)); //Is this required?
                return false;
            }

            if(worker == null) {
                worker = new Worker();
            }

            if( !worker.isRunning()) {
                worker.start();
                new Thread(worker).start();
                logger.debug("Reference validation worker thread started");
            } else {
                logger.debug("Reference validation worker thread already running");
            }

            fireEvent(new DataUpdatedEvent(target));
            
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
        return HandleLinkModel.isSupportedPersistentIdentifier(value);
    }
    
    public void reset() {
        editor.setVisible(false);
        editor.reset();
        references.clear();
        lblNoReferences.setVisible(references.isEmpty());
        listview.setVisible(!references.isEmpty());
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
        logger.trace("Set resource data: {} reference", data.size());
        for(Resource r : data) {
            this.references.add(new ReferenceJob(r));
        }
        lblNoReferences.setVisible(references.isEmpty());
        listview.setVisible(!references.isEmpty());

        if(worker == null) {
            worker = new Worker();
        }

         if(!worker.isRunning()) {
            worker.start();
            new Thread(worker).start();
            logger.trace("Reference validation worker thread started");
        } else {
            logger.debug("Reference validation worker thread already running");
        }
    }
    
    public class ReferenceJob implements Serializable, Comparable{
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

        @Override
        public int compareTo(@NotNull Object o) {
            if( o == null) return 0;
            if(o instanceof ReferenceJob) {
                return OrderableComparator.compare(
                        getReference(),
                        ((ReferenceJob) o).getReference());
            }
            return 0;
        }
    }
    
    public class Worker implements Runnable {

        private final Logger logger = LoggerFactory.getLogger(Worker.class);
        
        private boolean running = false;
        
        public Worker() {}
        
        public synchronized void start() {
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
                            //fireEvent(new DataUpdatedEvent(null));
                            try {
                                analyze(job);
                                job.setState(State.DONE);
                                //fireEvent(new DataUpdatedEvent(null));
                            } catch(Exception ex) {
                                job.setState(State.FAILED);
                                //fireEvent(new DataUpdatedEvent(null));
                            }   
                        }
                    }
                }
            }
            logger.trace("Reference Validation worker thread finished");
        }

        private void analyze(final ReferenceJob job) throws IOException {
            logger.debug("Analyzing: {}", job.getReference().getRef());
            
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                HttpGet httpget = new HttpGet(job.getReference().getRef());
                logger.trace("Executing request " + httpget.getRequestLine());

                // Create a custom response handler
                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

                    @Override
                    public String handleResponse(
                            final HttpResponse response) throws ClientProtocolException, IOException {
                        for(Header h : response.getHeaders("Content-Type")) {
                            logger.debug(h.getName() + " - " + h.getValue());
                            
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
                            logger.debug(h.getName() + " - " + h.getValue());
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
        }
    }

    private void parseCmdi(final String xml, final ReferenceJob job) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
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

    /**
     * If one or more validators failed, set error message and return false otherwise reset error message and return
     * true.
     *
     * @return false if one or more validators failed, true otherwise
     */
    @Override
    public boolean validate() {
        previousValidation = currentValidation;

        //Check for value if required == true
        if(required && references.isEmpty()) {
            currentValidation = setError("Required field.");
            return currentValidation;
        }

        //Check if any resource was not valid
        long errorCount = 0;
        for(ReferenceJob job : references) {
            if(job.getState() != State.DONE) {
                errorCount++;
            }
        }

        if(errorCount > 0) {
            String prefix = errorCount == 1 ? "One resource " : errorCount+ " resources ";
            currentValidation = setError(prefix + "failed to validate");
            return currentValidation;
        }

        currentValidation = setError(null);
        return currentValidation;
    }

    public boolean didValidationStateChange() {
        return currentValidation != previousValidation;
    }

    protected void move(int direction, Long displayOrder) {
        //Abort on invalid direction
        if(direction < -1 || direction >= references.size()) {
            logger.warn("References list move: invalid direction={}, references size={}.", direction, references.size());
            return;
        }

        //Find index of specified (by id) collection
        int idx = -1;
        for(int i = 0; i < references.size() && idx == -1; i++) {
            if(references.get(i).getReference().getDisplayOrder() == displayOrder) {
                idx = i;
            }
        }

        //Abort if the collection was not found
        if(idx == -1) {
            logger.warn("References list move: reference with displayOrder = {} not found.", displayOrder);
            return;
        }

        //Swap the collection with the collection at the specified destination (up=1, down=-1, beginning=0 or end=i)
        if (direction == -1 && idx > 0) {
            references.get(idx).getReference().setDisplayOrder(new Long(idx - 1));
            references.get(idx - 1).getReference().setDisplayOrder(new Long(idx));
        } else if(direction == 1 && idx < references.size()-1) {
            references.get(idx).getReference().setDisplayOrder(new Long(idx + 1));
            references.get(idx + 1).getReference().setDisplayOrder(new Long(idx));
        } else {
            references.get(idx).getReference().setDisplayOrder(new Long(direction));
            references.get(direction).getReference().setDisplayOrder(new Long(idx));
        }

        //Resort list based on new sort order
        Collections.sort(references);
    }
}
