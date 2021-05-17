package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor;

import java.util.*;

import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
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
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedByQuery;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
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

    private final IModel<String> intDescription = Model.of("");
    private final IModel<String> intQueryUri = Model.of("");
    private final IModel<String> intQueryProfile = Model.of("");
    private final IModel<String> intQueryParameters = Model.of("");

    private final AuthorsEditor authorsEditor;
    private final ReferencesEditor referencesEditor;

    private final IModel<String> mdlErrorMessage = Model.of("");
    private final Label lblErrorMessage;

    //List of fields
    private final List<AbstractField> fields = new ArrayList<>();
    //Map of field id to support editor modes for that field
    private final Map<String, Mode[]> fieldMode = new HashMap<>();

    //private final ModalConfirmDialog dialog;

    private final AjaxFallbackLink btnSave;
    private final WebMarkupContainer cbHelpLabel;

    public static enum Mode {
        SIMPLE,
        ADVANCED
    }
    private final static Mode DEFAULT_EDITOR_MODE = Mode.SIMPLE;

    private Model<Boolean> advancedEditorModeModel = Model.of(false);
    private Model<Boolean> toggleHelpModeModel = Model.of(false);

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
        //this.dialog = dialog;
        this.setOutputMarkupId(true);
        
        final Component ajax_update_component = this;

        //Default updater to toggle visibility based on editor mode
        VisabilityUpdater v = new VisabilityUpdater() {
            @Override
            public void updateVisability(Component componentToUpdate) {
                Mode currentMode = advancedEditorModeModel.getObject() ? Mode.ADVANCED : Mode.SIMPLE;
                Mode[] modes = fieldMode.get(componentToUpdate.getId());

                boolean visible = false;
                for(Mode m : modes) {
                    if(m == currentMode) {
                        visible = true;
                    }
                }

                componentToUpdate.setVisible(visible);
            };
        };

        //Updater to only show fields if the collection type is extensional
        VisabilityUpdater vExtensional = new VisabilityUpdater() {
            @Override
            public void updateVisability(Component componentToUpdate) {
                boolean visible = false;
                VirtualCollection.Type t = VirtualCollection.Type.valueOf(typeModel.getObject());
                if(t == VirtualCollection.Type.EXTENSIONAL) {
                    Mode currentMode = advancedEditorModeModel.getObject() ? Mode.ADVANCED : Mode.SIMPLE;
                    Mode[] modes = fieldMode.get(componentToUpdate.getId());

                    for (Mode m : modes) {
                        if (m == currentMode) {
                            visible = true;
                        }
                    }
                }
                componentToUpdate.setVisible(visible);
            };
        };

        //Updater to onlu show fields if the collection type is intensional
        VisabilityUpdater vIntensional = new VisabilityUpdater() {
            @Override
            public void updateVisability(Component componentToUpdate) {
                boolean visible = false;
                VirtualCollection.Type t = VirtualCollection.Type.valueOf(typeModel.getObject());
                if(t == VirtualCollection.Type.INTENSIONAL) {
                    Mode currentMode = advancedEditorModeModel.getObject() ? Mode.ADVANCED : Mode.SIMPLE;
                    Mode[] modes = fieldMode.get(componentToUpdate.getId());
                    for (Mode m : modes) {
                        if (m == currentMode) {
                            visible = true;
                        }
                    }
                }
                componentToUpdate.setVisible(visible);
            };
        };

        addRequiredField(
            new VcrTextField("name", "Name", "New collection name", nameModel, v),
            new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        addRequiredField(
            new VcrTextArea("description", "Description", "New collection description", descriptionModel, v),
            new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        VcrChoiceField field = new VcrChoiceField(
                "type",
                "Type",
                enumValuesAsList(VirtualCollection.Type.values()),
                "Specifies the type of this collection: "+
                        "<ul>"+
                            "<li>Extensional: Type of virtual collection that explicitly enumerates the references to resources or metadata documents that make up the collection.</li>"+
                            "<li>Intensional: Type of virtual collection that defines a query by which the items of the collection can be retrieved ad-hoc from a secondary service.</li>"+
                        "</ul>"+
                        "Note: changing this value will affect wich collection fields you can edit.",
                typeModel, v);
        field.addListener(new Listener() {
            @Override
            public void handleEvent(Event event) {
                updateAllFieldVisability();
                event.getAjaxRequestTarget().add(ajax_update_component);
            }
        });
        addRequiredField(field, new Mode[]{Mode.ADVANCED});

        addRequiredField(
            new VcrChoiceField(
                "purpose",
                "Purpose",
                enumValuesAsList(VirtualCollection.Purpose.values()),
                "An indication of the intended usage of the present virtual collection:"+
                    "<ul>"+
                        "<li>research: the virtual collection bundles resources that are relevant to a specific research (question).</li>"+
                        "<li>reference: the virtual collection bundles resources, that are to be cited in a publication.</li>"+
                        "<li>sample: this virtual collection bundles is intended to serve as an sample for research data</li>"+
                        "<li>future-use: the purpose of this virtual collection is not specified yet. Used in published collection is advised against.</li>"+
                    "</ul>",
                purposeModel, v),
            new Mode[]{Mode.ADVANCED});

        addRequiredField(
            new VcrChoiceField(
                "repro",
                "Reproducibility",
                enumValuesAsList(VirtualCollection.Reproducibility.values()),
                "An indication of the degree to which results obtained from processing of the present collection can be expected to be stable:"+
                        "<ul>"+
                            "<li>intended: processing results can be expected to remain stable</li>"+
                            "<li>fluctuating: Processing results may vary</li>"+
                            "<li>untended: no claims with respect to the stability of the processing results are made</li>"+
                        "</ul>",
                reproModel, v),
            new Mode[]{Mode.ADVANCED});

        addOptionalField(
            new VcrTextArea(
                "repro_notice",
                "Reproducibility Notice",
                "Describe the expected reproducibility of processing results in more detail",
                reproNoticeModel, v),
            new Mode[]{Mode.ADVANCED});

        addOptionalField(
            new VcrTextField("keywords", "Keywords", "List of keywords, separated by space or comma.", keywordsModel, v),
            new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        this.authorsEditor = new AuthorsEditor("authors", "Authors");
        addRequiredField(this.authorsEditor, new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        this.referencesEditor = new ReferencesEditor("references", "Resources", advancedEditorModeModel, vExtensional);
        addRequiredField(this.referencesEditor, new Mode[]{Mode.SIMPLE, Mode.ADVANCED});

        addOptionalField(
                new VcrTextArea(
                        "int_description",
                        "Query description",
                        "A prose description of the procedure by which the collection items can be retrieved from the external service.",
                        intDescription, vIntensional),
                new Mode[]{Mode.ADVANCED});

        addOptionalField(
                new VcrTextField(
                        "int_uri",
                        "Query URI",
                        "The location of the service from which the items should be retrieved.",
                        "This value may be a persistent identifier, but could also be a plain URI although this is advised against for published collections.",
                        intQueryUri, vIntensional),
                new Mode[]{Mode.ADVANCED});

        addOptionalField(
                new VcrTextField(
                        "int_query_profile",
                        "Query profile",
                        "Identifier of the mechanism, i.e. the protocol to be used.",
                            "A application exploiting the query can use this to use a proper driver to talk to the service. Example: In case of CLARIN-FCS access the endpoint by FCS-compatible means.",
                        intQueryProfile, vIntensional),
                new Mode[]{Mode.ADVANCED});

        addOptionalField(
                new VcrTextArea(
                        "int_query_parameters",
                        "Query parameters",
                        "The query that should be passed on to the service by which it can look up the items that are part of this collection.",
                        "This value should be in the query language supported by the service. Example: In case of CLARIN-FCS, this value is the CQL query to be send to the endpoint.",
                        intQueryParameters, vIntensional, false),
                new Mode[]{Mode.ADVANCED});

        btnSave = new AjaxFallbackLink("btn_save") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if(validate()) {
                    mdlErrorMessage.setObject("");
                    lblErrorMessage.setVisible(false);
                    persist(target);
                    reset();
                } else {
                    logger.info("Failed to validate");
                    mdlErrorMessage.setObject("Collection failed to validate.<br />Please fix all issues and try again.");
                    lblErrorMessage.setVisible(true);
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

        //Toggle help checkbox
        cbHelpLabel = new WebMarkupContainer("btn_editor_toggle_help_lbl", Model.of("Show Help"));
        AjaxCheckBox cbHelp = new AjaxCheckBox("btn_editor_toggle_help", toggleHelpModeModel) {
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                toggleHelpMode();
                if (target != null) {
                    target.add(ajax_update_component);
                }
            }
        };
        cbHelpLabel.setVisible(false);
        cbHelpLabel.add(cbHelp);
        add(cbHelpLabel);

        //Toggle editor mode checkbox
        add(new AjaxCheckBox("btn_editor_mode", advancedEditorModeModel) {
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                updateAllFieldVisability();
                if (target != null) {
                    target.add(ajax_update_component);
                }
            }
        });

        lblErrorMessage = new Label("error_message", mdlErrorMessage);
        lblErrorMessage.setVisible(false);
        lblErrorMessage.setEscapeModelStrings(false);
        add(lblErrorMessage);

        //Update field visibility based on the active editor mode
        updateAllFieldVisability();
    }

    private void toggleHelpMode() {
        boolean showHelp = toggleHelpModeModel.getObject();
        for(AbstractField f: fields) {
            f.showHelp(showHelp);
        }
    }

    private void updateAllFieldVisability() {
        cbHelpLabel.setVisible(advancedEditorModeModel.getObject());
        for(AbstractField f: fields) {
            f.updateVisability();
        }
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
        fieldMode.put(c.getId(), modes);
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

        if(c.getGeneratedBy() != null) {
            if (c.getGeneratedBy().getQuery() != null) {
                intQueryParameters.setObject(c.getGeneratedBy().getQuery().getValue());
                intQueryProfile.setObject(c.getGeneratedBy().getQuery().getProfile());
            }
            intQueryUri.setObject(c.getGeneratedBy().getURI());
            intDescription.setObject(c.getGeneratedBy().getDescription());
        }
    }

    private void reset() {
        this.originalCollection = null;
        nameModel.setObject("");
        descriptionModel.setObject("");
        keywordsModel.setObject("");
        typeModel.setObject(VirtualCollection.DEFAULT_TYPE_VALUE.toString());
        purposeModel.setObject(VirtualCollection.DEFAULT_PURPOSE_VALUE.toString());
        reproModel.setObject(VirtualCollection.DEFAULT_REPRODUCIBILIY_VALUE.toString());
        reproNoticeModel.setObject("");
        authorsEditor.reset();
        referencesEditor.reset();
        intQueryParameters.setObject("");
        intQueryProfile.setObject("");
        intQueryUri.setObject("");
        intDescription.setObject("");
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
        //Make sure we have a principal, otherwise fail early
        ApplicationSession session = (ApplicationSession)getSession();
        if(session.getPrincipal() == null) {
            logger.warn("Collection persist attempt with null principal (expired session?).");
            mdlErrorMessage.setObject("Save cancelled.<br />Session seemed to have expired.");
            lblErrorMessage.setVisible(true);
            target.add(this);
            return;
        }

        VirtualCollection newCollection = new VirtualCollection();
        newCollection.setCreationDate(new Date()); // FIXME: get date from GUI?

        if (this.originalCollection != null && this.originalCollection.getId() != null) {
            newCollection = originalCollection;
        } else if(this.originalCollection != null) {
            newCollection.setOrigin(originalCollection.getOrigin());
        }

        newCollection.setDateModified(new Date());
        newCollection.setName(nameModel.getObject());
        newCollection.setDescription(descriptionModel.getObject());
        newCollection.setType(VirtualCollection.Type.valueOf(typeModel.getObject()));
        newCollection.setPurpose(VirtualCollection.Purpose.valueOf(purposeModel.getObject()));
        newCollection.setReproducibility(VirtualCollection.Reproducibility.valueOf(reproModel.getObject()));
        newCollection.setReproducibilityNotice(reproNoticeModel.getObject());
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

        VirtualCollection.Type t = VirtualCollection.Type.valueOf(typeModel.getObject());
        if(t == VirtualCollection.Type.EXTENSIONAL) {
            newCollection.getResources().clear();
            newCollection.getResources().addAll(referencesEditor.getData());
        } else if(t == VirtualCollection.Type.INTENSIONAL) {
            GeneratedBy genBy = new GeneratedBy();
            genBy.setDescription(intDescription.getObject());
            genBy.setURI(intQueryUri.getObject());
            genBy.setQuery(new GeneratedByQuery(intQueryProfile.getObject(), intQueryParameters.getObject()));
            newCollection.setGeneratedBy(genBy);
        }

        fireEvent(
                new AbstractEvent<VirtualCollection>(
                        EventType.SAVE,
                        session.getPrincipal(),
                        newCollection,
                        target));
    }
}
