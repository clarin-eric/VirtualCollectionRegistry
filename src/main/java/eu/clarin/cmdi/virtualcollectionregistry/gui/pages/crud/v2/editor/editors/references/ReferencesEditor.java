package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryReferenceValidationJob;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.HandleLinkModel;
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
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
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
    
    private final int uiRefreshTimeInSeconds = 1;

    private boolean currentValidation = false;
    private boolean previousValidation = false;

    private final String editorId = UUID.randomUUID().toString();

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
                VirtualCollectionRegistry registry = Application.get().getRegistry();
                registry.getReferenceValidator().setState(references.get(edit_index).getInternalId(), State.INITIALIZED);
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
                EditableResource ref = (EditableResource)item.getModel().getObject();
                final VirtualCollectionRegistry registry = Application.get().getRegistry();
                VirtualCollectionRegistryReferenceValidationJob job =
                        registry.getReferenceValidator().getJob(ref.getInternalId());
                State state = registry.getReferenceValidator().getState(ref.getInternalId());
                String reason = null;
                if(job != null && state == State.FAILED) {
                    reason = job.getState().getData();
                    logger.info("Reason: {}", reason);
                } else {
                    logger.info("No issue. job="+job+", state="+state);
                }

                ReferencePanel c = new ReferencePanel("pnl_reference", ref, state, reason, advancedEditorMode, getMaxDisplayOrder());
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

                        if(edit_index < 0) {
                            editor.setVisible(false);
                            editor.reset();
                            listview.setVisible(true);
                        } else {
                            editor.setReference(references.get(edit_index));
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
        logger.info("Removing Reference editor");
        VirtualCollectionRegistry registry = Application.get().getRegistry();
        for(EditableResource r : references) {
            registry.getReferenceValidator().removeReferenceValidationJob(r.getInternalId());
        }
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
                addReferenceJob(EditableResource.fromResource(r));
                data.setObject("");
                mdlReferenceTitle.setObject("");
            } else if(handlePid(value)) {
                String actionableValue = HandleLinkModel.getActionableUri(value);
                Resource r = new Resource(Resource.Type.RESOURCE, actionableValue, title);
                r.setDisplayOrder(getNextDisplayOrder());
                addReferenceJob(EditableResource.fromResource(r));
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
                listview.setVisible(!references.isEmpty());
                target.add(this);
            }
        }
        return false;
    }

    private void addReferenceJob(EditableResource r) {
        references.add(r);
        final VirtualCollectionRegistry registry = Application.get().getRegistry();
        registry.getReferenceValidator().addReferenceValidationJob(r.getInternalId(), r);
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
        List<Resource> result = new LinkedList<>();
        for(EditableResource r : references) {
            result.add((Resource)r);
        }
        return result;
    }
    
    public void setData(List<Resource> data) {
        logger.trace("Set resource data: {} reference", data.size());
        for(Resource r : data) {
            addReferenceJob(EditableResource.fromResource(r));
        }
        lblNoReferences.setVisible(references.isEmpty());
        listview.setVisible(!references.isEmpty());
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
        VirtualCollectionRegistry registry = Application.get().getRegistry();
        for(EditableResource r : references) {
            State state = registry.getReferenceValidator().getState(r.getInternalId());
            if(state != State.DONE) {
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
            if(references.get(i).getDisplayOrder() == displayOrder) {
                idx = i;
            }
        }

        logger.info("direction={}, idx={}", direction, idx);

        //Abort if the collection was not found
        if(idx == -1) {
            logger.warn("References list move: reference with displayOrder = {} not found.", displayOrder);
            return;
        }

        //Swap the collection with the collection at the specified destination (up=1, down=-1, beginning=0 or end=i)
        if (direction == -1 && idx > 0) {
            references.get(idx).setDisplayOrder(new Long(idx - 1));
            references.get(idx - 1).setDisplayOrder(new Long(idx));
        } else if(direction == 1 && idx < references.size()-1) {
            references.get(idx).setDisplayOrder(new Long(idx + 1));
            references.get(idx + 1).setDisplayOrder(new Long(idx));
        } else {
            references.get(idx).setDisplayOrder(new Long(direction));
            references.get(direction).setDisplayOrder(new Long(idx));
        }

        //Resort list based on new sort order
        Collections.sort(references);
    }
}
