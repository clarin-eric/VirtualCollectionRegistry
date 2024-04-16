package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.VirtualCollectionRegistryReferenceValidator;
import eu.clarin.cmdi.virtualcollectionregistry.core.reference.parsers.ReferenceParserResult;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.TimerManager;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.CancelEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.EventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.MoveListEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.SaveEventHandler;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmAction;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.DataUpdatedEvent;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Event;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.EventType;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Listener;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.*;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan.State;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PidLink;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class ReferencesEditor extends ComposedField {
    private static Logger logger = LoggerFactory.getLogger(ReferencesEditor.class);

    private IModel<String> data = new Model<>();
    private IModel<String> mdlReferenceTitle = new Model<>();
    
    private final Label lblNoReferences;
    private final List<EditableResource> references = new LinkedList<>();
    private final ListView<EditableResource> listview;

    private int edit_index = -1;
    
    private final ReferenceEditor editor;

    private final ModalConfirmDialog localDialog;
    
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
            if(PidLink.isSupportedPersistentIdentifier(input)) {
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

    public static class EditableResource extends Resource {
        private final String internalId = UUID.randomUUID().toString();

        private static EditableResource fromResource(Resource r) {
            EditableResource result = new EditableResource();
            result.setCheck(r.getCheck());
            result.setDescription(r.getDescription());
            result.setDisplayOrder(r.getDisplayOrder());
            result.setLabel(r.getLabel());
            result.setMimetype(r.getMimetype());
            result.setOrigin(r.getOrigin());
            result.setOriginalQuery(r.getOriginalQuery());
            result.setRef(r.getRef());
            result.setType(r.getType());
            return result;
        }

        public String getInternalId() {
            return internalId;
        }
    }

    public class Example implements Serializable {
        private final String label;
        private final String url;
        
        public Example(String label, String url) {
            this.label = label;
            this.url = url;
        }

        public String getLabel() {
            return label;
        }

        public String getUrl() {
            return url;
        }
    }
    
    public class ExamplePanel extends Panel implements Serializable {
        public ExamplePanel(String id, Example example) {
            super(id);
            
            add(new Label("label", Model.of(example.getLabel())));
            final TextField tf = new TextField("url", Model.of(example.getUrl()));
            tf.setOutputMarkupId(true);            
            add(tf);
            
            WebMarkupContainer btn = new WebMarkupContainer("btn");
            btn.add(new AttributeAppender("data-clipboard-target",
                new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {                        
                        return "#"+tf.getMarkupId();
                    }
                }, " "));
            
            add(btn);
        }
        
    }
    
    private final Component componentToUpdate;
    private final Filter f;
    private final TimerManager timerManager;
    private final WebMarkupContainer ajaxWrapper;

    private Map<String, Boolean> refReasonCollapseState = new HashMap<>();

    private Map<String, ResourceScan> scanResults = new HashMap<>();
    private final Map<String, Date> scanResultsTimestamp = new HashMap<>();
    private final Map<String, Long> scanResultsTimedout = new HashMap<>();
    
    private boolean useCache = false;
    
    public interface RescanHandler extends Serializable {
        public void rescan(String reg, AjaxRequestTarget target);
    }

    public ReferencesEditor(String id, String label, Model<Boolean> advancedEditorMode, VisabilityUpdater updater, TimerManager timerManager) {
        super(id, "References", null, updater);
        this.timerManager = timerManager;
        setOutputMarkupId(true);
        componentToUpdate = this;

        final WebMarkupContainer editorWrapper = new WebMarkupContainer("ref_editor_wrapper");
        editorWrapper.setOutputMarkupId(true);

        ajaxWrapper = new WebMarkupContainer("ajaxwrapper");
        ajaxWrapper.setOutputMarkupId(true);

        f = new Filter("filter", Application.get().getRegistry().getReferenceValidator());
        add(f);

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
                                    String value = references.get(i).getRef();
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
                updateReferenceJob(references.get(edit_index), useCache);

                //addReferenceJob
                edit_index = -1;
                editor.setVisible(false);
                listview.setModelObject(f.apply(references));
                listview.setVisible(true);

                addToTimerManager(target);

                if(target != null) {
                    target.add(componentToUpdate);
                }

            }
        }, new CancelEventHandler() {
            @Override
            public void handleCancelEvent(AjaxRequestTarget target) {
                edit_index = -1;
                editor.setVisible(false);
                listview.setModelObject(f.apply(references));
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

        listview = new ListView("listview", f.apply(references)) {
            @Override
            protected void populateItem(ListItem item) {
                EditableResource ref = (EditableResource)item.getModel().getObject();

                
                //logger.info("Resource ref={}", ref.getRef());
                
                ResourceScan scan = scanResults.get(ref.getRef());
                State state = State.QUEUED;
                String reason = "";
                
                if(scanResultsTimedout.containsKey(ref.getRef())) {
                    state = State.FAILED;
                    reason += "Queue processing timed out (Exceeded "+(scanResultsTimedout.get(ref.getRef())/1000)+" seconds)";
                } else {             
                    
                    //ResourceScan scan = scanResults.get(ref.getRef());
                    if(scan != null) {
                        state = scan.getState();
                        int httpResponseCode = scan.getHttpResponseCode() != null ? scan.getHttpResponseCode() : 0;
                        reason += "Last scan: "+scan.getLastScanEnd()+"<br />";
                        if(httpResponseCode > 0) {
                            reason += "HTTP response: " + httpResponseCode;
                            if (scan.hasHttpResponseMessage()) {
                                reason += " " + scan.getHttpResponseMessage();
                            }
                            reason += "<br />";
                        }
                        if(scan.getException() != null) {
                            reason += "Exception: "+scan.getException()+"<br />";
                        }
                        reason += "State: "+state.toString()+"<br />";
                    }
                }
                
                RescanHandler rescanHandler = new RescanHandler() {
                    @Override
                    public void rescan(String ref, AjaxRequestTarget target) {
                        rescanReferenceJob(ref, useCache);
                        addToTimerManager(target);
                    }
                };
                /*
                logger.debug("Added ReferencePanel for scan, id={}, ref={}, lastScanStart={}, lastScanEnd={} ==> state={}", scan.getId(), scan.getRef(), scan.getLastScan(), scan.getLastScanEnd(), scan.getState());               
                if(scan.getLogs() == null) {
                    logger.trace("No scan logs yet");
                } else {
                    logger.trace("Scan logs:");
                    for(ResourceScanLog log: scan.getLogs()) {
                        logger.trace("  id={}, processor={}", log.getId(), log.getProcessorId());
                        for(ResourceScanLogKV kv : log.getKvs()) {
                            logger.trace("Log key={}, value={}", kv.getKey(), kv.getValue());
                        }
                    }
                }
                */
                ReferencePanel c = new ReferencePanel("pnl_reference", ref, scan, state, reason, advancedEditorMode, getMaxDisplayOrder(), refReasonCollapseState, rescanHandler);
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
                            String value = references.get(i).getRef();
                            if(value.equalsIgnoreCase(t.getRef())) {
                                edit_index = i;
                                break;
                            }
                        }

                        listview.setModelObject(f.apply(references));
                        if(edit_index < 0) {
                            editor.setVisible(false);
                            editor.reset();
                            listview.setVisible(true);
                        } else { 
                            String ref = references.get(edit_index).getRef();
                            ResourceScan scan = scanResults.get(ref);
                            String title = scan.getResourceScanLogLastValue(ReferenceParserResult.KEY_NAME);
                            String description = scan.getResourceScanLogLastValue(ReferenceParserResult.KEY_DESCRIPTION);
                            editor.setReference(references.get(edit_index), title, description);
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

        ajaxWrapper.add(listview);

        lblNoReferences.setVisible(references.isEmpty());
        listview.setVisible(!references.isEmpty());

        ajaxWrapper.add(lblNoReferences);
        ajaxWrapper.add(listview);
        add(ajaxWrapper);

        AbstractField f1 = new VcrTextFieldWithoutLabel("reference", "Add new reference by URL or PID", data, this,null);
        //f1.setCompleteSubmitOnUpdate(false);
        f1.setCompleteSubmitOnUpdate(true);
        f1.setRequired(true);
        f1.addValidator(new Validator());
        add(f1);

        //AbstractField f2 = new VcrTextFieldWithoutLabel("reference_title", "Set a title for this new reference", mdlReferenceTitle, this,null);
        //f2.setCompleteSubmitOnUpdate(true);
        //f2.setRequired(true);
        //add(f2);
        
        //Add examples
        //TODO: only enable in alpha mode?
        List<Example> examples = new LinkedList<>();
        examples.add(new Example("TEI example 1", "https://llds.ling-phil.ox.ac.uk/llds/xmlui/bitstream/handle/20.500.14106/A39119/A39119.xml"));
        examples.add(new Example("TEI example 2", "https://llds.ling-phil.ox.ac.uk/llds/xmlui/bitstream/handle/20.500.14106/A02252/A02252.xml?sequence=5&isAllowed=y"));
        examples.add(new Example("CMDI example 1", "http://dali.talkbank.org/data-cmdi/childes-data/DutchAfrikaans/Wijnen.cmdi"));
        examples.add(new Example("CMDI example 2", "https://archive.mpi.nl/tla/islandora/object/tla%3A1839_00_0000_0000_0005_783E_5/datastream/CMD/download"));
        examples.add(new Example("Website example 1", "https://www.clarin.eu"));
        
        ListView<Example> listExamples = new ListView("listExample", examples) {
            @Override
            protected void populateItem(ListItem item) {
                item.add(new ExamplePanel("pnl_example", (Example)item.getModel().getObject()));
            }
        };
        add(listExamples);
    }

    private void addToTimerManager(AjaxRequestTarget target) {
       timerManager.addTarget(target, new TimerManager.Update() {
            @Override
            public boolean onUpdate(AjaxRequestTarget target) {
                fireEvent(new CustomDataUpdateEvent(target));

                //Assume analyzing is done, if one if the scans is in the ANALYZING, INITIALIZED or QUEUED state and is below the timeout threshold, 
                //analyzing is set to true (and should not be reverted back to false until the next check).
                //If no scans are analyzing and false is returned, the timer will pause updating and also stop the UI from updating, which then leaves
                //the scans in an incorrect state, eternally waiting to be updated.
                boolean analyzing = false;
                try {
                    //Build list of refs
                    List<String> refs = new ArrayList<>();
                    for(EditableResource ref : references) {
                        refs.add(ref.getRef());
                    }
                    //Query scans for this list of refs
                    List<ResourceScan> scans = Application.get().getRegistry().getResourceScansForRefs(refs);
                    for(ResourceScan scan : scans) {
                        scanResults.put(scan.getRef(), scan);
                        switch (scan.getState()) {
                            case ANALYZING:
                            case INITIALIZED:
                            case QUEUED:
                                long msElapsed = new Date().getTime() - scanResultsTimestamp.get(scan.getRef()).getTime();
                                if(msElapsed < 60*1000) {
                                    analyzing = true;
                                } else {
                                    scanResultsTimedout.put(scan.getRef(), msElapsed);                                    
                                }
                                break;                            
                        }
                    }
                } catch(VirtualCollectionRegistryException ex) {
                    logger.error("Failed to fetch resource scan for reference.", ex);
                }

                return analyzing;
            }

            @Override
            public List<Component> getComponents() {
                List<Component> result = new ArrayList<>();
                result.add(ajaxWrapper);
                return result;
            }
        });
    }

    public class Filter extends WebMarkupContainer implements Serializable {
        private final IModel<String> filterState = new Model<>("ALL");

        private transient final VirtualCollectionRegistryReferenceValidator validator;

        public Filter(String id, VirtualCollectionRegistryReferenceValidator validator) {
            super(id);
            this.validator = validator;

            DropDownChoice choice = new DropDownChoice<String>(
                    "cb_filter",
                    filterState,
                    new LoadableDetachableModel<List<String>>() {
                        @Override
                        protected List<String> load() {
                            List<String> filterableState = new ArrayList<>();
                            filterableState.add("ALL");
                            filterableState.add(State.FAILED.toString());
                            filterableState.add(State.ANALYZING.toString());
                            filterableState.add(State.DONE.toString());
                            return filterableState;
                        }
                    });

            choice.add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(final AjaxRequestTarget target) {
                    listview.setModelObject(apply(references));
                    target.add(componentToUpdate);
                }
            });

            Label lblPrefix = new Label("lbl_filter_prefix", "Filter references: ");
            add(lblPrefix);
            add(choice);
            Label lblPostfix = new Label("lbl_filter_postfix", "");
            add(lblPostfix);
        }

        public List<EditableResource> apply(List<EditableResource> references) {
            List<EditableResource> filtered = new ArrayList<>();
            for(EditableResource r : references) {

                State state = State.QUEUED;
                ResourceScan scan  = scanResults.get(r.getRef());
                if(scan != null) {
                    state = scan.getState();
                }

                if(filterState.getObject() == "ALL" || filterState.getObject() == state.toString()) {
                    filtered.add(r);
                }
            }
            return filtered;
        }
    }

    private long getMaxDisplayOrder() {
        long max = 0;
        for(Resource r : references) {
            if(r.getDisplayOrder() > max) {
                max = r.getDisplayOrder();
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
        /*
        VirtualCollectionRegistry registry = Application.get().getRegistry();
        for(EditableResource r : references) {
            registry.getReferenceValidator().removeReferenceValidationJob(r.getInternalId());
        }
         */
    }

    @Override
    public boolean completeSubmit(AjaxRequestTarget target) {
        String value = data.getObject();
        String title = mdlReferenceTitle.getObject();

        logger.debug("Completing reference submit: value="+value+",title="+title);
        if(value != null && !value.isEmpty()) {// && title != null && !title.isEmpty()) {
            //Check if the supplied url (value) is duplicate or not
            boolean duplicate = false;
            for(EditableResource res : references) {
                if(res.getRef().equalsIgnoreCase(value)) {
                    duplicate = true;
                    break;
                }
            }
            
            //Handle the new url value
            if(duplicate) {
                logger.debug("Skipping duplicate url: {}", value);
                return false;
            } else if(handleUrl(value)) {
                Resource r = new Resource(Resource.Type.RESOURCE, value, title);
                r.setDisplayOrder(getNextDisplayOrder());
                addReferenceJob(EditableResource.fromResource(r), useCache);
                data.setObject("");
                mdlReferenceTitle.setObject("");
            } else if(handlePid(value)) {
                String actionableValue = PidLink.getActionableUri(value);
                Resource r = new Resource(Resource.Type.RESOURCE, actionableValue, title);
                r.setDisplayOrder(getNextDisplayOrder());
                addReferenceJob(EditableResource.fromResource(r), useCache);
                data.setObject("");
                mdlReferenceTitle.setObject("");
            } else {
                //abort
                logger.warn("Unhandled reference (not url AND not pid)");
                fireEvent(new DataUpdatedEvent(target)); //Is this required?
                return false;
            }

            fireEvent(new DataUpdatedEvent(target));
            
            if(target != null) {
                lblNoReferences.setVisible(references.isEmpty());
                listview.setModelObject(f.apply(references));
                listview.setVisible(!references.isEmpty());
                addToTimerManager(target);
                target.add(this);
            }
        }
        return false;
    }

    private void addReferenceJob(EditableResource r, boolean useCache) {
        logger.info("addReferenceJob: ref={}", r.getRef());
        //Add reference to list
        references.add(r);

        //Create new resource scan for this reference
        try {
            String sessionId = null;
            if(getSession() != null) {
                sessionId = getSession().getId();
            }

            //Add resource scan placeholder, will be replaced with instance fetched from the database
            //This ensures we have something to render in the UI, even if there is no database / api communication.
            scanResults.put(r.getRef(), new ResourceScan(r.getRef(), sessionId));
            scanResultsTimestamp.put(r.getRef(), new Date());
            //Insert the scan into the database
            Application.get().getRegistry().addResourceScan(r.getRef(), sessionId, useCache);
        } catch(Exception ex) {
            logger.error("Failed to create new resource scan.", ex);
        }
    }
    
    private void updateReferenceJob(EditableResource r, boolean useCache) {
        logger.info("updateReferenceJob: ref={}", r.getRef());
        //Create new resource scan for this reference
        try {
            String sessionId = null;
            if(getSession() != null) {
                sessionId = getSession().getId();
            }

            Application.get().getRegistry().addResourceScan(r.getRef(), sessionId, useCache);
        } catch(Exception ex) {
            logger.error("Failed to create new resource scan.", ex);
        }
    }

    private void rescanReferenceJob(String ref, boolean useCache) {
        logger.info("rescanReferenceJob: ref={}", ref);
        try {
            String sessionId = null;
            if(getSession() != null) {
                sessionId = getSession().getId();
            }

            scanResultsTimestamp.put(ref, new Date());
            scanResultsTimedout.remove(ref);
            Application.get().getRegistry().rescanResource(ref, sessionId, useCache);
        } catch(Exception ex) {
            logger.error("Failed to create new resource scan.", ex);
        }
    }
    
    private boolean handleUrl(String value) {
        try {
            new URL(value);
        } catch(MalformedURLException ex) {
            logger.debug("Failed to parse value: "+value+" as url", ex);
            return false;
        }
        return true;
    }
    
    private boolean handlePid(String value) {
        return PidLink.isSupportedPersistentIdentifier(value);
    }
    
    public void reset() {
        editor.setVisible(false);
        editor.reset();
        references.clear();
        lblNoReferences.setVisible(references.isEmpty());
        listview.setModelObject(f.apply(references));
        listview.setVisible(!references.isEmpty());
    }
    
    public List<Resource> getData() {
        List<Resource> result = new LinkedList<>();
        for(EditableResource r : references) {
            result.add((Resource)r);
        }
        return result;
    }
    
    public void setData(List<Resource> data) {
        logger.debug("Set resource data: {} reference", data.size());
        for(Resource r : data) {
            addReferenceJob(EditableResource.fromResource(r), useCache);
        }
        lblNoReferences.setVisible(references.isEmpty());
        listview.setModelObject(f.apply(references));
        listview.setVisible(!references.isEmpty());
        addToTimerManager(null);
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
        //VirtualCollectionRegistry registry = Application.get().getRegistry();
        for(EditableResource ref : references) {
            ResourceScan scan = scanResults.get(ref.getRef());
            if( scan == null || scan.getState() != State.DONE) {
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
            if(references.get(i).getDisplayOrder().longValue() == displayOrder) {
                idx = i;
            }
        }

        logger.trace("direction={}, idx={}", direction, idx);

        //Abort if the collection was not found
        if(idx == -1) {
            logger.warn("References list move: reference with displayOrder = {} not found.", displayOrder);
            return;
        }

        //Swap the collection with the collection at the specified destination (up=1, down=-1, beginning=0 or end=i)
        if (direction == -1 && idx > 0) {
            references.get(idx).setDisplayOrder(Long.valueOf(idx - 1));
            references.get(idx - 1).setDisplayOrder(Long.valueOf(idx));
        } else if(direction == 1 && idx < references.size()-1) {
            references.get(idx).setDisplayOrder(Long.valueOf(idx + 1));
            references.get(idx + 1).setDisplayOrder(Long.valueOf(idx));
        } else {
            references.get(idx).setDisplayOrder(Long.valueOf(direction));
            references.get(direction).setDisplayOrder(Long.valueOf(idx));
        }

        //Resort list based on new sort order
        Collections.sort(references);
    }
}
