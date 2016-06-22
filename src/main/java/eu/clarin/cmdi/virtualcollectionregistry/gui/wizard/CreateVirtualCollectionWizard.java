package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.VolatileEntityModel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ConfirmationDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.ReferenceLinkPanel;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.CreatorProvider;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.AttributeModifier;

import org.apache.wicket.Component;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardModel;
import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardStep;
import org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.OddEvenListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class CreateVirtualCollectionWizard extends WizardBase {

    private final Logger logger = LoggerFactory.getLogger(CreateVirtualCollectionWizard.class);
    
    @SpringBean
    private CreatorProvider creatorProvider;

    private final static ResourceReference TOOLTIP_ACTIVATE_JAVASCRIPT_REFERENCE = 
            new PackageResourceReference(CreateVirtualCollectionWizard.class, "wizardhelp.js");
    private final static ResourceReference TOOLTIP_JAVASCRIPT_REFERENCE = 
            new PackageResourceReference(CreateVirtualCollectionWizard.class, "jquery.qtip.min.js");
    private final static ResourceReference TOOLTIP_STYLE_REFERENCE = 
            new CssResourceReference(CreateVirtualCollectionWizard.class, "jquery.qtip.min.css");
    
    private AjaxLink addMeLink;
    
    public CreateVirtualCollectionWizard() {
        super(null);
        this.vc = null;
    }
    
    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        // Javascript dependencies for this page (tooltips)
        response.render(JavaScriptHeaderItem.forReference(TOOLTIP_ACTIVATE_JAVASCRIPT_REFERENCE));
        response.render(JavaScriptHeaderItem.forReference(TOOLTIP_JAVASCRIPT_REFERENCE));
        response.render(CssHeaderItem.forReference(TOOLTIP_STYLE_REFERENCE));
    }
    
    private class PreventSubmitOnEnterBehavior extends Behavior {
        private static final long serialVersionUID = 1496517082650792177L;

        public PreventSubmitOnEnterBehavior(){}

        @Override
        public void bind( Component component ) {
            super.bind( component );
            component.add( AttributeModifier.replace( "onkeydown", Model.of( "if(event.keyCode == 13) {event.preventDefault();}" ) ) );
        }
    }
    
    private final class GeneralStep extends DynamicWizardStep {

        private final class DeleteKeywordDialog extends ConfirmationDialog {

            private String keyword;

            public DeleteKeywordDialog(String id,
                    final Component updateComponent) {
                super(id, updateComponent);
            }

            @Override
            public void onConfirm(AjaxRequestTarget target) {
                vc.getObject().getKeywords().remove(keyword);
            }

            public void show(AjaxRequestTarget target, String keyword) {
                this.keyword = keyword;
                IModel message = new StringResourceModel(
                        "keywords.deleteconfirm", null, new Model<>(keyword));
                super.show(target, message);
            }
        } // class CreateVirtualCollectionWizard.GeneralStep.DeleteKeywordDialog

        private final class KeywordsList extends WebMarkupContainer {

            private final ListView<String> itemsView;

            public KeywordsList(String id, final IModel<List<String>> itemsModel) {
                super(id);
                setOutputMarkupId(true);

                itemsView = new ListView<String>("keywords", itemsModel) {
                    @Override
                    protected void populateItem(final ListItem<String> item) {
                        final IModel<String> model = item.getModel();
                        item.add(new Label("itemText", model.getObject()));
                        item.add(new AjaxLink<String>("itemRemove") {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                deleteKeywordDialog.show(target,
                                        model.getObject());
                            }
                        });
                    }

                    @Override
                    protected ListItem<String> newItem(int index, IModel<String> model) {
                        return new OddEvenListItem<String>(index, model) {
                            @Override
                            protected void onComponentTag(ComponentTag tag) {
                                super.onComponentTag(tag);
                                if (getIndex() == 0) {
                                    tag.append("class", "first", " ");
                                }
                            }
                        };
                    }
                };
                add(itemsView);
            }
        } // class CreateVirtualCollectionWizard.GeneralStep.KeywordsList

        //private final AddKeywordDialog addKeywordDialog;
        private final DeleteKeywordDialog deleteKeywordDialog;

        public GeneralStep() {
            super(null, "General", null, vc);
            setDefaultModel(new CompoundPropertyModel<>(vc));
            final TextField<String> nameField
                    = new RequiredTextField<>("name");
            nameField.add(Application.MAX_LENGTH_VALIDATOR);
            nameField.add(new PreventSubmitOnEnterBehavior());
            add(nameField);
            
            final DropDownChoice<VirtualCollection.Type> typeChoice
                    = new DropDownChoice<>("type",
                            Arrays.asList(VirtualCollection.Type.values()),
                            new EnumChoiceRenderer<VirtualCollection.Type>(this));
            typeChoice.setRequired(true);
            add(typeChoice);
            
            add(new TextArea<String>("description"));
            final DropDownChoice<VirtualCollection.Purpose> purposeChoice
                    = new DropDownChoice<>("purpose",
                            Arrays.asList(VirtualCollection.Purpose.values()),
                            new EnumChoiceRenderer<VirtualCollection.Purpose>(this));
            add(purposeChoice);
            final DropDownChoice<VirtualCollection.Reproducibility> reproducibilityChoice
                    = new DropDownChoice<>("reproducibility",
                            Arrays.asList(VirtualCollection.Reproducibility.values()),
                            new EnumChoiceRenderer<VirtualCollection.Reproducibility>(this));
            add(reproducibilityChoice);
            final TextArea<String> reproducibilityNoticeArea
                    = new TextArea<>("reproducibilityNotice");
            add(reproducibilityNoticeArea);

            /*
            StatelessForm form = new StatelessForm("add_keyword_form") {
                @Override
                public void onSubmit() {
                    logger.info("Add keyword form submitted");
                }
            };
            */

            final TextField<String> addKeywordField
                    = new TextField<>("add_keyword", new Model<>("keyword"));
            
            /*
            addKeywordField.add(new OnChangeAjaxBehavior() {
                private static final long serialVersionUID = 2462233190993745889L;

                
                @Override
                protected void onUpdate(final AjaxRequestTarget target){
                    // Maybe you want to update some components here?
                    // Access the updated model object:
                    final Object value = getComponent().getDefaultModelObject();
                    // or:
                    final String valueAsString =
                        ((TextField<String>) getComponent()).getModelObject();
                    
                    logger.info("Keyword value: {}", valueAsString);
                    
                }
            });
            */
            /*
            addKeywordField.add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    logger.info("Textfield submit!");
                }
                
            });
            */
            //addKeywordField.add(new PreventSubmitOnEnterBehavior());
            addKeywordField.add(onChangeBehavior());
            addKeywordField.add(onKeyDownBehavior());
            //form.add(addKeywordField);
            add(addKeywordField);
            /*
            form.add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    logger.info("Textfield submit!");
                }
                
            });
            */
            /*
            form.add(new AjaxEventBehavior("onsubmit") {
                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    logger.info("Textfield submit!");
                }
                
            });
            */
            //add(form);
            
            final KeywordsList keywordList
                    = new KeywordsList("keywordsList",
                            new PropertyModel<List<String>>(vc, "keywords"));
            add(keywordList);
            
            /*
            add(new AjaxFallbackLink<String>("keywordsAdd") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    if(target != null) {
                        addKeywordDialog.show(target);
                    }
                }
            });
            */

            /*
            addKeywordDialog = new AddKeywordDialog("addKeywordDialog") {
                @Override
                public void onSubmit(AjaxRequestTarget target, String keyword) {
                    final List<String> keywords = vc.getObject().getKeywords();
                    if (!keywords.contains(keyword)) {
                        keywords.add(keyword);
                    }
                    target.add(keywordList);
                }
            };
            add(addKeywordDialog);
            */
            deleteKeywordDialog
                    = new DeleteKeywordDialog("deleteKeywordDialog", keywordList);
            add(deleteKeywordDialog);
        }
        
        private OnChangeAjaxBehavior onChangeBehavior() {
        return new OnChangeAjaxBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget ajaxRequestTarget) {
                logger.info("{}", ((TextField<String>) getComponent()).getModelObject());
            }
        };
    }
        
         private AjaxEventBehavior onKeyDownBehavior() {
            return new AjaxEventBehavior("keydown") {
                @Override
                protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                    super.updateAjaxAttributes(attributes);

                    IAjaxCallListener listener = new AjaxCallListener() {
                        @Override
                        public CharSequence getPrecondition(Component component) {
                            //this javascript code evaluates whether an Ajax call is necessary.
                            //Here only by keyocdes for enter (13)
                            return "var keycode = Wicket.Event.keyCode(attrs.event);" +
                                    "if (keycode == 13)" +
                                    "    return true;" +
                                    "else" +
                                    "    return false;";
                        }
                    };

                    attributes.getAjaxCallListeners().add(listener);
                    attributes.getDynamicExtraParameters()
                            .add("var eventKeycode = Wicket.Event.keyCode(attrs.event);" +
                                    "return {keycode: eventKeycode};");

                    //without setting, no keyboard events will reach any inputfield
                    attributes.setPreventDefault(true);
                }

                @Override
                protected void onEvent(AjaxRequestTarget target) {
                    /*
                    final String queryTerm = searchBox.getDefaultModelObjectAsString();
                    searchForMovies(queryTerm);
                    youTubeMovieListView.setList(movieList);
                    target.add(searchResultWrapper);
                    */
                }
            };
        }

        @Override
        public boolean isLastStep() {
            return false;
        }

        @Override
        public IDynamicWizardStep next() {
            return new CreatorsStep(this);
        }

        @Override
        public void applyState() {
            super.applyState();
            switch (vc.getObject().getType()) {
                case EXTENSIONAL:
                    vc.getObject().setGeneratedBy(null);
                    break;
                case INTENSIONAL:
                    vc.getObject().getResources().clear();
                    break;
            }
        }
    } // class CreateVirtualCollectionWizard.GeneralStep

    private final class CreatorsStep extends DynamicWizardStep {

        private final class ActionsPanel extends Panel {

            public ActionsPanel(String id, final IModel<Creator> model) {
                super(id, model);
                setRenderBodyOnly(true);
                final AjaxLink<Creator> editLink
                        = new AjaxLink<Creator>("edit") {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                editCreatorDialog.show(target, model);
                            }
                        };
                add(editLink);
                final AjaxLink<Creator> deleteLink
                        = new AjaxLink<Creator>("delete") {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                deleteCreatorDialog.showCreator(target, model);
                            }
                        };
                add(deleteLink);
            }
        } // class CreateVirtualCollectionWizard.CreatorsStep.ActionsPanel

        private final class DeleteCreatorDialog extends ConfirmationDialog {

            private IModel<Creator> creator;

            public DeleteCreatorDialog(String id,
                    final Component updateComponent) {
                super(id, updateComponent);
                setInitialWidth(400);
            }

            @Override
            public void onConfirm(AjaxRequestTarget target) {
                if (creator != null) {
                    vc.getObject().getCreators().remove(creator.getObject());
                    final Creator _creator = creatorProvider.getCreator(ApplicationSession.get().getPrincipal());
                    addMeLink.setVisible(!vc.getObject().hasCreator(_creator));  
                    target.add(addMeLink);
                }
            }

            public void showCreator(AjaxRequestTarget target, IModel<Creator> creator) {
                this.creator = creator;
                super.show(target,
                        new StringResourceModel("creators.deleteconfirm",
                                creator));
            }
        } // class CreateVirtualCollectionWizard.CreatorsStep.DeleteCreatorDialog

        private final EditCreatorDialog editCreatorDialog;
        private final DeleteCreatorDialog deleteCreatorDialog;

        public CreatorsStep(IDynamicWizardStep previousStep) {
            super(previousStep, "Creators", null, vc);
            final DataTable<Creator, String> creatorsTable
                    = new AjaxFallbackDefaultDataTable<>("creatorsTable",
                            createColumns(),
                            new SortableDataProvider<Creator, String>() {
                                @Override
                                public Iterator<? extends Creator>
                                iterator(long first, long count) {
                                    return vc.getObject().getCreators().listIterator((int)first);
                                }

                                @Override
                                public IModel<Creator> model(Creator creator) {
                                    return new VolatileEntityModel<Creator>(creator);
                                }

                                @Override
                                public long size() {
                                    return vc.getObject().getCreators().size();
                                }
                            },
                            8);
            creatorsTable.setOutputMarkupId(true);
            add(creatorsTable);

            editCreatorDialog = new EditCreatorDialog("editCreatorDialog") {
                @Override
                public void onSubmit(AjaxRequestTarget target, Creator creator) {
                    // create a copy first because retrieving the creators may
                    // reset the state of the object
                    final Creator copy = creator.getCopy();
                    final List<Creator> creators = vc.getObject().getCreators();
                    if (creators.contains(creator)) {
                        // update existig creator (needed since we're dealing with a volatile instance)
                        creator.setValuesFrom(copy);
                    } else {
                        // new entry, add
                        creators.add(creator);
                    }
                    target.add(creatorsTable);
                }
            };
            add(editCreatorDialog);

            deleteCreatorDialog
                    = new DeleteCreatorDialog("deleteCreatorDialog", creatorsTable);
            add(deleteCreatorDialog);

            add(new AjaxLink<Object>("add") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    editCreatorDialog.show(target);
                }
            });
            addMeLink = new AjaxLink<Object>("addme") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    final Creator creator = creatorProvider.getCreator(ApplicationSession.get().getPrincipal());
                    if (creator.getPerson() == null) {
                        Session.get().error("Could not retrieve required user information");
                        setResponsePage(getPage());
                    } else {
                        if(!vc.getObject().hasCreator(creator)) {
                            vc.getObject().getCreators().add(creator);
                            addMeLink.setVisible(false);      
                            target.add(creatorsTable);
                            target.add(addMeLink);
                        }
                    }
                }
            };
            addMeLink.setOutputMarkupId(true);
            addMeLink.setOutputMarkupPlaceholderTag(true);
            //Hide the "add me" link if the current user already exist as a creator
            final Creator creator = creatorProvider.getCreator(ApplicationSession.get().getPrincipal());
            addMeLink.setVisible(!vc.getObject().hasCreator(creator));                
            add(addMeLink);
        }

        @Override
        public boolean isLastStep() {
            return false;
        }

        @Override
        public IDynamicWizardStep next() {
            switch (vc.getObject().getType()) {
                case EXTENSIONAL:
                    return new ResourcesStep(this);
                case INTENSIONAL:
                    return new GeneratedByStep(this);
                default:
                    throw new InternalError("bad vc type");
            } // switch
        }

        @SuppressWarnings("unchecked")
        //private IColumn<Creator>[] createColumns() {
        private List<IColumn<Creator, String>> createColumns() {
            List<IColumn<Creator, String>> columns = new ArrayList<>();
            columns.add(
                new PropertyColumn<Creator, String>(new Model<>("Person"),
                "person"));
            columns.add(
                new PropertyColumn<Creator, String>(new Model<>("EMail"),
                "email"));
            columns.add(
                new PropertyColumn<Creator, String>(new Model<>(
                "Organisation"), "organisation"));
            columns.add(
                new HeaderlessColumn<Creator, String>() {
                    @Override
                    public void populateItem(Item<ICellPopulator<Creator>> item, String compontentId, IModel<Creator> model) {
                        item.add(new ActionsPanel(compontentId, model));
                    }

                    @Override
                    public String getCssClass() {
                        return "action";
                    }
                }
            );
            return columns;
        }
    } // class CreateVirtualCollectionWizard.CreatorsStep

    private final class ResourcesStep extends DynamicWizardStep {

        private final WebMarkupContainer resourcesContainer;

        private final class ActionsPanel extends Panel {

            public ActionsPanel(String id, final IModel<Resource> model) {
                super(id, model);
                setRenderBodyOnly(true);
                final AjaxLink<Resource> editLink
                        = new AjaxLink<Resource>("edit") {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                editResourceDialog.show(target, model);
                            }

                            @Override
                            protected void onConfigure() {
                                // only allow when not moving
                                setVisible(movingResource.getObject() == null);
                            }
                        };
                add(editLink);
                final AjaxLink<Resource> deleteLink
                        = new AjaxLink<Resource>("delete") {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                deleteResourceDialog.showResource(target, model);
                            }

                            @Override
                            protected void onConfigure() {
                                // only allow when not moving
                                setVisible(movingResource.getObject() == null);
                            }
                        };
                add(deleteLink);
            }

        }

        private final class MoveItemPanel extends Panel {

            public MoveItemPanel(String id, final IModel<Resource> model) {
                super(id, model);

                final AjaxLink<Resource> moveLink
                        = new AjaxLink<Resource>("move") {

                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                movingResource.setObject(model.getObject());
                                target.add(resourcesContainer);
                            }

                            @Override
                            protected void onConfigure() {
                                // only allow to start moving when not moving
                                setVisible(movingResource.getObject() == null);
                            }

                        };
                add(moveLink);

                final AjaxLink<Resource> cancelLink
                        = new AjaxLink<Resource>("cancel") {

                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                movingResource.setObject(null);
                                target.add(resourcesContainer);
                            }

                            @Override
                            protected void onConfigure() {
                                // only allow cancelling resource being moved
                                setVisible(model.getObject().equals(movingResource.getObject())
                                );
                            }

                        };
                add(cancelLink);

                final AjaxLink<Resource> targetLink
                        = new AjaxLink<Resource>("target") {

                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                final List<Resource> resources = vc.getObject().getResources();
                                final Resource toMove = movingResource.getObject();
                                try {
                                    if (resources.remove(toMove)) {
                                        final int targetIndex = resources.indexOf(model.getObject());
                                        if (targetIndex >= 0) {
                                            resources.add(targetIndex, toMove);
                                            return;
                                        }
                                    }
                                    Session.get().warn("Could not move reference");

                                } finally {
                                    movingResource.setObject(null);
                                    target.add(resourcesContainer);
                                }
                            }

                            @Override
                            protected void onConfigure() {
                                final Resource toMove = movingResource.getObject();
                                // only allow to drop when moving
                                setVisible(toMove != null
                                        && !toMove.equals(model.getObject())
                                );
                            }

                        };

                add(targetLink);
            }
        } // class CreateVirtualCollectionWizard.ResourcesStep.ActionsPanel

        private final class DeleteResourceDialog extends ConfirmationDialog {

            private IModel<Resource> resource;

            public DeleteResourceDialog(String id,
                    final Component updateComponenet) {
                super(id, updateComponenet);
                setInitialWidth(400);
            }

            @Override
            public void onConfirm(AjaxRequestTarget target) {
                if (resource != null) {
                    vc.getObject().getResources().remove(resource.getObject());
                }
            }

            public void showResource(AjaxRequestTarget target, IModel<Resource> resource) {
                this.resource = resource;
                super.show(target,
                        new StringResourceModel("resources.deleteconfirm",
                                resource));
            }
        } // class CreateVirtualCollectionWizard.CreatorsStep.DeleteResourceDialog

        private final EditResourceDialog editResourceDialog;
        private final DeleteResourceDialog deleteResourceDialog;

        private IModel<Resource> movingResource = new Model<>();

        public ResourcesStep(IDynamicWizardStep previousStep) {
            super(previousStep, "Resources", null, vc);
            resourcesContainer = new WebMarkupContainer("resourcesContainer");
            resourcesContainer.setOutputMarkupId(true);
            add(resourcesContainer);
            final DataTable<Resource, String> resourcesTable
                    = new AjaxFallbackDefaultDataTable<Resource, String>("resourcesTable",
                            createColumns(),
                            new SortableDataProvider<Resource, String>() {
                                @Override
                                public Iterator<? extends Resource>
                                iterator(long first, long count) {
                                    return vc.getObject().getResources().listIterator((int)first);
                                }

                                @Override
                                public IModel<Resource> model(Resource resource) {
                                    return new VolatileEntityModel<Resource>(resource);
                                }

                                @Override
                                public long size() {
                                    return vc.getObject().getResources().size();
                                }
                            },
                            64) {

                        @Override
                        protected Item<Resource> newRowItem(String id, int index, final IModel<Resource> model) {
                            final Item<Resource> item = super.newRowItem(id, index, model);
                            // mark the row that is being moved
                            item.add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {

                                @Override
                                public String getObject() {
                                    if (model.getObject().equals(movingResource.getObject())) {
                                        return "moving";
                                    } else {
                                        return "";
                                    }
                                }
                            }, " "));
                            return item;
                        }

                    };
            resourcesTable.setOutputMarkupId(true);

            // add 'moving' class if table is in moving mode
            resourcesTable.add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {

                @Override
                public String getObject() {
                    return movingResource.getObject() == null ? "" : "moving";
                }
            }, " "));
            resourcesContainer.add(resourcesTable);

            editResourceDialog = new EditResourceDialog("editResourceDialog") {
                @Override
                public void onSubmit(AjaxRequestTarget target,
                        Resource resource) {
                    // create a copy first because retrieving the resources may
                    // reset the state of the object
                    final Resource copy = resource.getCopy();
                    final List<Resource> resources = vc.getObject().getResources();
                    if (resources.contains(resource)) {
                        // update existig resource (needed since we're dealing with a volatile instance)
                        resource.valuesFrom(copy);
                    } else {
                        // new entry, add
                        resources.add(resource);
                    }
                    target.add(resourcesTable);
                }
            };
            add(editResourceDialog);

            deleteResourceDialog = new DeleteResourceDialog(
                    "deleteResourceDialog", resourcesTable);
            add(deleteResourceDialog);

            final AddResourcesDialog addResourcesDialog
                    = new AddResourcesDialog("addResourcesDialog") {
                        @Override
                        public void onSubmit(AjaxRequestTarget target,
                                Resource[] resources) {
                            for (Resource resource : resources) {
                                if (!vc.getObject().getResources().contains(resource)) {
                                    vc.getObject().getResources().add(resource);
                                }
                            }
                            target.add(resourcesTable);
                        }

                    };
            add(addResourcesDialog);

            resourcesContainer.add(new AjaxLink<Object>("add") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    editResourceDialog.show(target);
                }
            });

            resourcesContainer.add(new AjaxLink<Object>("addMore") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    addResourcesDialog.show(target);
                }
            });

            add(new AbstractFormValidator() {
                @Override
                public FormComponent<?>[] getDependentFormComponents() {
                    return null;
                }

                @Override
                public void validate(Form<?> form) {
                    if (vc.getObject().getResources().isEmpty()) {
                        form.error("An extensional collection requires at "
                                + "least one resource.");
                    }
                }
            });
        }

        @Override
        public boolean isLastStep() {
            return true;
        }

        @Override
        public IDynamicWizardStep next() {
            return null;
        }

        @SuppressWarnings("unchecked")
        private List<IColumn<Resource, String>>createColumns() {
            final List<IColumn<Resource, String>>columns = new ArrayList<>();
            columns.add(
                new AbstractColumn<Resource, String>(new Model<String>("Type")) {
                    @Override
                    public void populateItem(
                            Item<ICellPopulator<Resource>> item,
                            String componentId, IModel<Resource> model) {
                        switch (model.getObject().getType()) {
                            case METADATA:
                                item.add(new Label(componentId, "Metadata"));
                                break;
                            case RESOURCE:
                                item.add(new Label(componentId, "Resource"));
                                break;
                        }
                    }

                    @Override
                    public String getCssClass() {
                        return "type";
                    }
                });
            columns.add(
                new PropertyColumn<Resource, String>(
                new Model<String>("Reference"), "ref") {
                    @Override
                    public void populateItem(Item<ICellPopulator<Resource>> item, String componentId, IModel<Resource> rowModel) {
                        item.add(new ReferenceLinkPanel(componentId, rowModel));
                    }

                });
            columns.add(
                new HeaderlessColumn<Resource, String>() {
                    @Override
                    public void populateItem(
                            Item<ICellPopulator<Resource>> item,
                            String compontentId, IModel<Resource> model) {
                        item.add(new ActionsPanel(compontentId, model));
                    }

                    @Override
                    public String getCssClass() {
                        return "action";
                    }
                });
            columns.add(
                new HeaderlessColumn<Resource, String>() {
                    @Override
                    public void populateItem(Item<ICellPopulator<Resource>> item, String componentId, IModel<Resource> model) {
                        item.add(new MoveItemPanel(componentId, model));
                    }

                    @Override
                    public String getCssClass() {
                        return "move";
                    }
                });
            return columns;
        }
    } // class CreateVirtualCollectionWizard.ResourcesStep

    private final class GeneratedByStep extends DynamicWizardStep {

        public GeneratedByStep(IDynamicWizardStep previousStep) {
            super(previousStep, "Intensional Collection Query", null, vc);
            setDefaultModel(new CompoundPropertyModel<VirtualCollection>(vc));
            final TextArea<String> descriptionArea
                    = new TextArea<String>("generatedBy.description");
            descriptionArea.setRequired(true);
            add(descriptionArea);
            final TextField<String> uriField
                    = new TextField<String>("generatedBy.uri");
            uriField.add(Application.MAX_LENGTH_VALIDATOR);
            uriField.add(new UrlValidator(UrlValidator.NO_FRAGMENTS));
            add(uriField);
            final TextField<String> queryProfileField
                    = new TextField<String>("generatedBy.query.profile");
            add(queryProfileField);
            final TextArea<String> queryValueArea
                    = new TextArea<String>("generatedBy.query.value");
            add(queryValueArea);

            add(new AbstractFormValidator() {
                @Override
                public FormComponent<?>[] getDependentFormComponents() {
                    return new FormComponent[]{queryProfileField,
                        queryValueArea};
                }

                @Override
                public void validate(Form<?> form) {
                    final String profile = queryProfileField.getInput();
                    final String value = queryValueArea.getInput();
                    if (profile.isEmpty() && !value.isEmpty()) {
                        form.error("profile is required with value");
                        queryProfileField.invalid();
                    }
                    if (!profile.isEmpty() && value.isEmpty()) {
                        form.error("value is required with profile");
                        queryValueArea.invalid();
                    }
                    if (profile.isEmpty() && value.isEmpty()) {
                        /*
                         * XXX: clear Query object as side-effect. This is
                         * more a hack than a clean solution.
                         */
                        if (vc.getObject().getGeneratedBy() != null) {
                            vc.getObject().getGeneratedBy().setQuery(null);
                        }
                    }
                }
            });
        }

        @Override
        public boolean isLastStep() {
            return true;
        }

        @Override
        public IDynamicWizardStep next() {
            return null;
        }
    } // class CreateVirtualCollectionWizard.GeneratedByStep

    private final IModel<VirtualCollection> vc;

    public CreateVirtualCollectionWizard(String id, IModel<VirtualCollection> vc) {
        super(id);
        if (vc == null) {
            throw new IllegalArgumentException("vc == null");
        }
        this.vc = vc;
        setDefaultModel(new CompoundPropertyModel<>(vc));//this));
        init(new DynamicWizardModel(new GeneralStep()));
    }

    public IModel<VirtualCollection> getVirtualCollectionModel() {
        return vc;
    }

    @Override
    public final void onCancel() {
        onCancelWizard();
    }

    @Override
    public final void onFinish() {
        onFinishWizard(vc);
    }

    @Override
    public void detachModels() {
        super.detachModels();
        vc.detach();
    }

    protected abstract void onCancelWizard();

    protected abstract void onFinishWizard(IModel<VirtualCollection> vc);

} // class CreateVirtualCollectionWizard
