package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.ActionablePanel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.authors.AuthorsEditor;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references.ReferencesEditor;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.AbstractEvent;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Event;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.EventType;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Listener;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.*;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Form to create or edit a virtual collection
 * 
 * @author wilelb
 */
public class CreateAndEditPanel extends ActionablePanel implements Listener {
    
    private final static Logger logger = LoggerFactory.getLogger(CreateAndEditPanel.class);

    //Keep track of the original collection, used to detect changes and reset the form
    private VirtualCollection originalCollection;
    
    private final IModel<String> nameModel = Model.of("");
    private final IModel<String> descriptionModel= Model.of("");
    private final IModel<String> typeModel = Model.of(VirtualCollection.DEFAULT_TYPE_VALUE.toString());
    private final IModel<String> purposeModel = Model.of(VirtualCollection.DEFAULT_PURPOSE_VALUE.toString());
    private final IModel<String> reproModel = Model.of(VirtualCollection.DEFAULT_REPRODUCIBILIY_VALUE.toString());
    private final IModel<String> reproNoticeModel = Model.of("");
    private final IModel<String> keywordsModel= Model.of("");


    private final AuthorsEditor authorsEditor;
    private final ReferencesEditor referencesEditor;
    
    private final List<AbstractField> fields = new ArrayList<>();
    private final List<AbstractField> modeSimpleFields = new ArrayList<>();
    private final List<AbstractField> modeAdvancedFields = new ArrayList<>();

    private final ModalConfirmDialog dialog;

    private final AjaxFallbackLink btnSave;

    private enum Mode {
        SIMPLE,
        ADVANCED
    }
    private final static Mode DEFAULT_EDITOR_MODE = Mode.SIMPLE;

    /**
     * Create a new virtual collection
     * @param id 
     * @param dialog 
     */
    public CreateAndEditPanel(String id, ModalConfirmDialog dialog) {
        this(id, null, dialog);
    }
    
    /**
     * Edit the supplied virtual collection or create a new virtual collection if
     * the supplied collection is null
     * 
     * @param id
     * @param collection 
     * @param dialog 
     */
    public CreateAndEditPanel(String id, VirtualCollection collection, ModalConfirmDialog dialog) {
        super(id);
        this.dialog = dialog;
        this.setOutputMarkupId(true);
        
        final Component ajax_update_component = this;

        addRequiredField(
            new VcrTextField("name", "Name", "New collection name", nameModel),
            new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        addRequiredField(
            new VcrTextArea("description", "Description", "New collection description", descriptionModel),
            new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        addRequiredField(
                new VcrChoiceField(
                        "type",
                        "Type",
                        enumValuesAsList(VirtualCollection.Type.values()),
                        typeModel),
                new Mode[]{Mode.ADVANCED});

        addRequiredField(
            new VcrChoiceField(
                "purpose",
                "Purpose",
                enumValuesAsList(VirtualCollection.Purpose.values()),
                purposeModel),
            new Mode[]{Mode.ADVANCED});

        addRequiredField(
            new VcrChoiceField(
                "repro",
                "Reproducibility",
                enumValuesAsList(VirtualCollection.Reproducibility.values()),
                reproModel),
            new Mode[]{Mode.ADVANCED});

        addOptionalField(
            new VcrTextArea(
                "repro_notice",
                "Reproducibility Notice",
                "Describe the expected reproducibility of processing results in more detail",
                reproNoticeModel),
            new Mode[]{Mode.ADVANCED});

        addOptionalField(
            new VcrTextField("keywords", "Keywords", "List of keywords, separated by space or comma.", keywordsModel),
            new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        this.authorsEditor = new AuthorsEditor("authors", "Authors");
        addRequiredField(this.authorsEditor, new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        this.referencesEditor = new ReferencesEditor("references", "Resources");
        addRequiredField(this.referencesEditor, new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        btnSave = new AjaxFallbackLink("btn_save") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if(validate()) {
                    persist(target);
                    reset();
                } else {
                    logger.info("Failed to validate");
                }

                if (target != null) {
                    target.add(ajax_update_component);
                }
            }
        };
        //disableSaveButton();
        add(btnSave);

        add(new AjaxFallbackLink("btn_cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                logger.info("Cancel button clicked");
                reset();
                if (target != null) {
                    target.add(ajax_update_component);
                }

                fireEvent(
                    new AbstractEvent<VirtualCollection>(
                            EventType.CANCEL,
                            null,
                            target));
            }
        });

        Model<Boolean> advancedEditorModeModel = Model.of(false);
        add(new AjaxCheckBox("btn_editor_mode", advancedEditorModeModel) {
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                updateMode(advancedEditorModeModel.getObject() ? Mode.ADVANCED : Mode.SIMPLE);
                if (target != null) {
                    target.add(ajax_update_component);
                }
            }
        });

        //Update field visibility based on the active editor mode
        updateMode(DEFAULT_EDITOR_MODE);
    }

    private void enableSaveButton() {
        btnSave.setEnabled(false);
        btnSave.add(new AttributeModifier("disabled", "true"));
    }

    private void disableSaveButton() {
        btnSave.setEnabled(true);
        btnSave.add(AttributeModifier.remove("disabled"));
    }
    private List<String> enumValuesAsList(Object[] values) {
        List<String> result = new ArrayList<>();
        for(Object o : values) {
            result.add(o.toString());
        }
        return result;
    }

    private void addOptionalField(AbstractField c, Mode[] modes) {
        addField(c, modes, false);
    }

    private void addRequiredField(AbstractField c, Mode[] modes) {
        addField(c, modes, true);
    }


    @Override
    public void handleEvent(Event event) {
        if(validate()) {
            //enableSaveButton();
        } else {
            //disableSaveButton();
        }
        event.getAjaxRequestTarget().add(this);
    }

    private void addField(AbstractField c, Mode[] modes, boolean required) {
        c.setRequired(required);
        add(c);
        fields.add(c);
        c.addListener(this);

        //Add field to associated mode list
        for(Mode m: modes) {
            switch(m) {
                case SIMPLE: modeSimpleFields.add(c); break;
                case ADVANCED: modeAdvancedFields.add(c); break;
            }
        }
    }

    private void updateMode(Mode editorMode) {
        //Select list of fields for the active editor mode
        List<AbstractField> activeFields = new ArrayList<>();
        switch(editorMode) {
            case SIMPLE: activeFields = modeSimpleFields; break;
            case ADVANCED: activeFields = modeAdvancedFields; break;
            default: activeFields = modeSimpleFields;
        }
        //Hide all fields
        for(AbstractField f: fields) {
            f.setVisible(false);
        }
        //Show fields enabled for the current editor mode
        for(AbstractField f : activeFields) {
            f.setVisible(true);
        }
        authorsEditor.setVisible(true);
        referencesEditor.setVisible(true);
    }

    public void editCollection(VirtualCollection c) {
        logger.info("edit collection: {}", c.getId());
        this.originalCollection = c; //TODO: deep clone?
        nameModel.setObject(c.getName());
        descriptionModel.setObject(c.getDescription());
        typeModel.setObject(c.getType().toString());

        String keywords = "";
        for(String keyword : c.getKeywords()) {
            keywords += keywords == "" ? keyword : ", "+keyword;
        }
        keywordsModel.setObject(keywords);

        purposeModel.setObject(c.getPurpose().toString());
        reproModel.setObject(c.getReproducibility().toString());
        reproNoticeModel.setObject(c.getReproducibilityNotice());
        authorsEditor.setData(c.getCreators());
        referencesEditor.setData(c.getResources());
    }

    private void reset() {
        logger.info("reset");
        this.originalCollection = null;
        nameModel.setObject("");
        descriptionModel.setObject("");
        keywordsModel.setObject("");
        purposeModel.setObject(VirtualCollection.DEFAULT_PURPOSE_VALUE.toString());
        reproModel.setObject(VirtualCollection.DEFAULT_REPRODUCIBILIY_VALUE.toString());
        reproNoticeModel.setObject("");
        authorsEditor.reset();
        referencesEditor.reset();
        disableSaveButton();
    }

    public boolean isEditing() {
        return this.originalCollection != null;
    }

    private boolean validate() {
        logger.info("Validating collection");
        for(Field f : fields) {
            if(!f.validate()) {
                return false;
            }
        }
        return true;
    }

    private void persist(final AjaxRequestTarget target) {
        VirtualCollection newCollection = new VirtualCollection();
        newCollection.setCreationDate(new Date()); // FIXME: get date from GUI?

        if (this.originalCollection != null && this.originalCollection.getId() != null) {
            newCollection = originalCollection;
        }

        newCollection.setDateModified(new Date());
        newCollection.setName(nameModel.getObject());
        newCollection.setDescription(descriptionModel.getObject());
        newCollection.setType(VirtualCollection.Type.valueOf(typeModel.getObject()));
        newCollection.setPurpose(VirtualCollection.Purpose.valueOf(purposeModel.getObject()));
        newCollection.setReproducibility(VirtualCollection.Reproducibility.valueOf(reproModel.getObject()));
        newCollection.setReproducibilityNotice(reproNoticeModel.getObject());
        //newCollection.setGeneratedBy();
        //newCollection.setOwner();

        List<String> keywords = new ArrayList<>();
        StringTokenizer tokens = new StringTokenizer(keywordsModel.getObject(), " \t\n\r\f,;");
        while (tokens.hasMoreTokens()) {
            keywords.add(tokens.nextToken().trim());
        }
        newCollection.getKeywords().clear();
        newCollection.getKeywords().addAll(keywords);

        newCollection.getCreators().clear();
        newCollection.getCreators().addAll(authorsEditor.getData());

        newCollection.getResources().clear();
        newCollection.getResources().addAll(referencesEditor.getData());

        fireEvent(
                new AbstractEvent<VirtualCollection>(
                        EventType.SAVE,
                        newCollection,
                        target));
    }
}
