package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor;

import java.util.ArrayList;
import java.util.List;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.ActionablePanel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.authors.AuthorsEditor;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.references.ReferencesEditor;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.AbstractEvent;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.EventType;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.fields.*;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
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
public class CreateAndEditPanel extends ActionablePanel {
    
    private final static Logger logger = LoggerFactory.getLogger(CreateAndEditPanel.class);

    //Keep track of the original collection, used to detect changes and reset the
    //form
    private VirtualCollection originalCollection;
    
    private final IModel<String> nameModel = Model.of("");
    private final IModel<String> descriptionModel= Model.of("");
    private final IModel<String> purposeModel = Model.of("");
    private final IModel<String> reproModel = Model.of("");
    private final IModel<String> reproNoticeModel = Model.of("");
    private final IModel<String> keywordsModel= Model.of("");

    private final AuthorsEditor authorsEditor;
    private final ReferencesEditor referencesEditor;
    
    private final List<AbstractField> fields = new ArrayList<>();
    private final List<AbstractField> modeSimpleFields = new ArrayList<>();
    private final List<AbstractField> modeAdvancedFields = new ArrayList<>();

    private final ModalConfirmDialog dialog;

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
            new VcrTextField("name", "Name", "", nameModel),
            new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        addRequiredField(
            new VcrTextArea("description", "Description", "", descriptionModel),
            new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        addRequiredField(
            new VcrChoiceField(
                "purpose",
                "Purpose",
                enumValuesAsList(VirtualCollection.Purpose.values()),
                VirtualCollection.DEFAULT_PURPOSE_VALUE.toString(),
                purposeModel,
                null),
            new Mode[]{Mode.ADVANCED});

        addRequiredField(
            new VcrChoiceField(
                "repro",
                "Reproducibility",
                enumValuesAsList(VirtualCollection.Reproducibility.values()),
                VirtualCollection.DEFAULT_REPRODUCIBILIY_VALUE.toString(),
                reproModel,
                null),
            new Mode[]{Mode.ADVANCED});

        addOptionalField(
            new VcrTextArea(
                "repro_notice",
                "Reproducibility Notice",
                "",
                reproNoticeModel),
            new Mode[]{Mode.ADVANCED});

        addOptionalField(
            new VcrTextField("keywords", "Keywords", "", keywordsModel),
            new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        this.authorsEditor = new AuthorsEditor("authors", "Authors");
        //addRequiredField(this.authorsEditor, true);
        this.authorsEditor.setRequired(true);
        add(authorsEditor);
        fields.add(authorsEditor);

        this.referencesEditor = new ReferencesEditor("references", "Resources");
        this.referencesEditor.setRequired(true);
        add(referencesEditor);
        fields.add(referencesEditor);

        add(new AjaxFallbackLink("btn_save") {
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
        });
        add(new AjaxFallbackLink("btn_cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                logger.info("Cancel button clicked");
                reset();
                if (target != null) {
                    target.add(ajax_update_component);
                }
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

    private void addField(AbstractField c, Mode[] modes, boolean required) {
        c.setRequired(required);
        add(c);
        fields.add(c);

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
        this.originalCollection = c; //TODO: deep clone?
        nameModel.setObject(c.getName());
        descriptionModel.setObject(c.getDescription());

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
    
    private boolean validate() {
        for(Field f : fields) {
            if(!f.validate()) {
                return false;
            }
        }
        return true;
    }
    
     //TODO: externalise persist action via interface
    private void persist(final AjaxRequestTarget target) {
        final VirtualCollection newCollection = new VirtualCollection();
        /*
        if(this.originalCollection != null && this.originalCollection.getId() != null && !this.originalCollection.getId().isEmpty()) {
            newCollection.setId(this.originalCollection.getId());
        } else {
            newCollection.setId(UUID.randomUUID().toString());
        }
        newCollection.setName(nameModel.getObject());
        newCollection.setType(typeModel.getObject());
        newCollection.setAuthors(authorsEditor.getData());
        newCollection.setReferences(referencesEditor.getData());
        */
        fireEvent(
            new AbstractEvent<VirtualCollection>(
                EventType.SAVE,
                newCollection, 
                target));
    }
    
    private void reset() {
        nameModel.setObject("");
        purposeModel.setObject(VirtualCollection.DEFAULT_PURPOSE_VALUE.toString());
        reproModel.setObject(VirtualCollection.DEFAULT_REPRODUCIBILIY_VALUE.toString());
        reproNoticeModel.setObject("");
        authorsEditor.reset();
        referencesEditor.reset();
    }
    
}
