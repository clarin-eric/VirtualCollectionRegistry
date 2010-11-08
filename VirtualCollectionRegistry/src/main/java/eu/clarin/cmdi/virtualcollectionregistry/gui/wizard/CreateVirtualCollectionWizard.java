package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.UrlValidator;

import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ConfirmationDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.HomePage;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
public class CreateVirtualCollectionWizard extends WizardBase {
    private final class GeneralStep extends DynamicWizardStep {
        private final class DeleteKeywordDialog extends ConfirmationDialog {
            private transient KeywordsList keywordList;
            private String keyword;
            
            public DeleteKeywordDialog(String id, KeywordsList keywordList) {
                super(id);
                this.keywordList = keywordList;
            }

            @Override
            public void onConfirm(AjaxRequestTarget target) {
                vc.getKeywords().remove(keyword);
                target.addComponent(keywordList);
            }
            
            public void show(AjaxRequestTarget target, String keyword) {
                this.keyword = keyword;
                super.show(target, new StringResourceModel("keywords.deleteconfirm", null, new Object[] { keyword }));
            }
        } // class CreateVirtualCollectionWizard.GeneralStep.DeleteKeywordDialog

        private final class KeywordsList extends WebMarkupContainer {
            private final ListView<String> itemsView;

            public KeywordsList(String id, final List<String> items) {
                super(id);
                setOutputMarkupId(true);

                itemsView = new ListView<String>("keywords", items) {
                    @Override
                    protected void populateItem(final ListItem<String> item) {
                        final IModel<String> model = item.getModel();
                        item.add(new Label("itemText", model.getObject()));
                        item.add(new AjaxLink<String>("itemRemove",
                                new Model<String>("[remove]")) {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                deleteKeywordDialog.show(target,
                                        model.getObject());
                            }
                        });
                        item.add(new AttributeAppender("class",
                                new AbstractReadOnlyModel<String>() {
                                    public String getObject() {
                                        if (item.getIndex() == 0) {
                                            return "first odd";
                                        }
                                        return (item.getIndex() % 2 == 1) ?
                                                    "even" : "odd";
                                    }
                                }, " "));
                    }
                };
                add(itemsView);
            }
        } // class CreateVirtualCollectionWizard.GeneralStep.KeywordsList

        private final AddKeywordDialog addKeywordDialog;
        private final DeleteKeywordDialog deleteKeywordDialog;

        public GeneralStep() {
            super(null, "General", "Yada yada yada ...");
            final TextField<String> nameField =
                new RequiredTextField<String>("vc.name");
            nameField.add(new StringValidator.MaximumLengthValidator(255));
            add(nameField);
            final DropDownChoice<VirtualCollection.Type> typeChoice =
                new DropDownChoice<VirtualCollection.Type>("vc.type",
                        Arrays.asList(VirtualCollection.Type.values()),
                        new EnumChoiceRenderer<VirtualCollection.Type>(this));
            typeChoice.setRequired(true);
            add(typeChoice);
            add(new TextArea<String>("vc.description"));
            final DropDownChoice<VirtualCollection.Purpose> purposeChoice =
                new DropDownChoice<VirtualCollection.Purpose>("vc.purpose",
                        Arrays.asList(VirtualCollection.Purpose.values()),
                        new EnumChoiceRenderer<VirtualCollection.Purpose>(this));
            add(purposeChoice);
            final DropDownChoice<VirtualCollection.Reproducibility> reproducibilityChoice =
                new DropDownChoice<VirtualCollection.Reproducibility>("vc.reproducibility",
                        Arrays.asList(VirtualCollection.Reproducibility.values()),
                        new EnumChoiceRenderer<VirtualCollection.Reproducibility>(this));
            add(reproducibilityChoice);
            final TextArea<String> reproducibilityNoticeArea =
                new TextArea<String>("vc.reproducibilityNotice");
            add(reproducibilityNoticeArea);
            
            final KeywordsList keywordList =
                new KeywordsList("keywordsList", vc.getKeywords());
            add(keywordList);
            add(new AjaxLink<String>("keywordsAdd") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    addKeywordDialog.show(target);
                }
            });

            addKeywordDialog = new AddKeywordDialog("addKeywordDialog") {
                @Override
                public void onSubmit(AjaxRequestTarget target, String keyword) {
                    if (!vc.getKeywords().contains(keyword)) {
                        vc.getKeywords().add(keyword);
                    }
                    target.addComponent(keywordList);
                }
            };
            add(addKeywordDialog);

            deleteKeywordDialog =
                new DeleteKeywordDialog("deleteKeywordDialog", keywordList);
            add(deleteKeywordDialog);
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
            switch (vc.getType()) {
            case EXTENSIONAL:
                vc.setGeneratedBy(null);
                break;
            case INTENSIONAL:
                vc.getResources().clear();
                break;
            }
        }
    } // class CreateVirtualCollectionWizard.GeneralStep

    private final class CreatorsStep extends DynamicWizardStep {
        private final class ActionsPanel extends Panel {
            public ActionsPanel(String id, final IModel<Creator> model) {
                super(id, model);
                final AjaxLink<Creator> editLink =
                    new AjaxLink<Creator>("edit") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        final Creator creator = model.getObject();
                        editCreatorDialog.show(target, creator);
                    }
                };
                add(editLink);
                final AjaxLink<Creator> removeLink =
                    new AjaxLink<Creator>("remove") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        final Creator creator = model.getObject();
                        deleteCreatorDialog.show(target, creator);
                    }
                };
                add(removeLink);
            }
        } // class CreateVirtualCollectionWizard.CreatorsStep.ActionsPanel

        private final class DeleteCreatorDialog extends ConfirmationDialog {
            private final transient DataTable<Creator> creatorsTable;
            private Creator creator;

            public DeleteCreatorDialog(String id,
                    DataTable<Creator> creatorsTable) {
                super(id);
                setInitialWidth(400);
                this.creatorsTable = creatorsTable;
            }

            @Override
            public void onConfirm(AjaxRequestTarget target) {
                if (creator != null) {
                    vc.getCreators().remove(creator);
                    target.addComponent(creatorsTable);
                }
            }

            public void show(AjaxRequestTarget target, Creator creator) {
                this.creator = creator;
                super.show(target,
                        new StringResourceModel("creators.deleteconfirm",
                                new Model<Creator>(creator)));
            }
        } // class CreateVirtualCollectionWizard.CreatorsStep.DeleteCreatorDialog

        private final EditCreatorDialog editCreatorDialog;
        private final DeleteCreatorDialog deleteCreatorDialog;

        public CreatorsStep(IDynamicWizardStep previousStep) {
            super(previousStep, "Creators", "Yada yada yada ...");
            final DataTable<Creator> creatorsTable =
                new AjaxFallbackDefaultDataTable<Creator>("creatorsTable",
                        createColumns(),
                        new SortableDataProvider<Creator>() {
                            @Override
                            public Iterator<? extends Creator>
                                iterator(int first, int count) {
                                return vc.getCreators().listIterator(first);
                            }

                            @Override
                            public IModel<Creator> model(Creator creator) {
                                return new Model<Creator>(creator);
                            }
                            @Override
                            public int size() {
                                return vc.getCreators().size();
                            }
                        },
                        8);
            creatorsTable.setOutputMarkupId(true);
            add(creatorsTable);

            editCreatorDialog = new EditCreatorDialog("editCreatorDialog") {
                @Override
                public void onSubmit(AjaxRequestTarget target, Creator creator) {
                    if (!vc.getCreators().contains(creator)) {
                        vc.getCreators().add(creator);
                    }
                    target.addComponent(creatorsTable);
                }
            };
            add(editCreatorDialog);

            deleteCreatorDialog =
                new DeleteCreatorDialog("deleteCreatorDialog", creatorsTable);
            add(deleteCreatorDialog);

            add(new AjaxLink<Object>("add") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    editCreatorDialog.show(target);
                }
            });
        }

        @Override
        public boolean isLastStep() {
            return false;
        }

        @Override
        public IDynamicWizardStep next() {
            switch (vc.getType()) {
            case EXTENSIONAL:
                return new ResourcesStep(this);
            case INTENSIONAL:
                return new GeneratedByStep(this);
            default:
                throw new InternalError("bad vc type");
            } // switch
        }

        @SuppressWarnings("unchecked")
        private IColumn<Creator>[] createColumns() {
            final IColumn<?>[] columns = new IColumn<?>[] {
                    new PropertyColumn<Creator>(new Model<String>("Name"),
                            "name"),
                    new PropertyColumn<Creator>(new Model<String>("EMail"),
                            "email"),
                    new PropertyColumn<Creator>(new Model<String>(
                            "Organisation"), "organisation"),
                    new HeaderlessColumn<Creator>() {
                        @Override
                        public void populateItem(
                                Item<ICellPopulator<Creator>> item,
                                String compontentId, IModel<Creator> model) {
                            item.add(new ActionsPanel(compontentId, model));
                        }

                        @Override
                        public String getCssClass() {
                            return "action";
                        }
                    }
            };
            return (IColumn<Creator>[]) columns;
        }
    } // class CreateVirtualCollectionWizard.CreatorsStep

    private final class ResourcesStep extends DynamicWizardStep {
        private final class ActionsPanel extends Panel {
            public ActionsPanel(String id, final IModel<Resource> model) {
                super(id, model);
                final AjaxLink<Resource> editLink =
                    new AjaxLink<Resource>("edit") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        final Resource resource = model.getObject();
                        editResourceDialog.show(target, resource);
                    }
                };
                add(editLink);
                final AjaxLink<Resource> removeLink =
                    new AjaxLink<Resource>("remove") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        final Resource resource = model.getObject();
                        deleteResourceDialog.show(target, resource);
                    }
                };
                add(removeLink);
            }
        } // class CreateVirtualCollectionWizard.ResourcesStep.ActionsPanel

        private final class DeleteResourceDialog extends ConfirmationDialog {
            private final transient DataTable<Resource> resourcesTable;
            private Resource resource;

            public DeleteResourceDialog(String id,
                    DataTable<Resource> resourcesTable) {
                super(id);
                setInitialWidth(400);
                this.resourcesTable = resourcesTable;
            }

            @Override
            public void onConfirm(AjaxRequestTarget target) {
                if (resource != null) {
                    vc.getResources().remove(resource);
                    target.addComponent(resourcesTable);
                }
            }

            public void show(AjaxRequestTarget target, Resource resource) {
                this.resource = resource;
                super.show(target,
                        new StringResourceModel("resources.deleteconfirm",
                        new Model<Resource>(resource)));
            }
        } // class CreateVirtualCollectionWizard.CreatorsStep.DeleteResourceDialog

        private final EditResourceDialog editResourceDialog;
        private final DeleteResourceDialog deleteResourceDialog;

        public ResourcesStep(IDynamicWizardStep previousStep) {
            super(previousStep, "Resources", "Yada yada yada ...");
            final DataTable<Resource> resourcesTable =
                new AjaxFallbackDefaultDataTable<Resource>("resourcesTable",
                        createColumns(),
                        new SortableDataProvider<Resource>() {
                            @Override
                            public Iterator<? extends Resource>
                                iterator(int first, int count) {
                                return vc.getResources().listIterator(first);
                            }

                            @Override
                            public IModel<Resource> model(Resource resource) {
                                return new Model<Resource>(resource);
                            }

                            @Override
                            public int size() {
                                return vc.getResources().size();
                            }
                        },
                        64);
            resourcesTable.setOutputMarkupId(true);
            add(resourcesTable);

            editResourceDialog = new EditResourceDialog("editResourceDialog") {
                @Override
                public void onSubmit(AjaxRequestTarget target,
                        Resource resource) {
                    if (!vc.getResources().contains(resource)) {
                        vc.getResources().add(resource);
                    }
                    target.addComponent(resourcesTable);
                }
            };
            add(editResourceDialog);

            deleteResourceDialog = new DeleteResourceDialog(
                    "deleteResourceDialog", resourcesTable);
            add(deleteResourceDialog);

            final AddResourcesDialog addResourcesDialog =
                new AddResourcesDialog("addResourcesDialog") {
                @Override
                public void onSubmit(AjaxRequestTarget target,
                        Resource[] resources) {
                    for (Resource resource : resources) {
                        if (!vc.getResources().contains(resource)) {
                            vc.getResources().add(resource);
                        }
                    }
                    target.addComponent(resourcesTable);
                }

            };
            add(addResourcesDialog);

            add(new AjaxLink<Object>("add") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    editResourceDialog.show(target);
                }
            });

            add(new AjaxLink<Object>("addMore") {
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
                    if (vc.getResources().isEmpty()) {
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
        private IColumn<Resource>[] createColumns() {
            final IColumn<?>[] columns = new IColumn<?>[] {
                    new AbstractColumn<Resource>(new Model<String>("Type")) {
                        @Override
                        public void populateItem(
                                Item<ICellPopulator<Resource>> item,
                                String componentId, IModel<Resource> model) {
                            final Resource resource = model.getObject();
                            switch (resource.getType()) {
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
                    },
                    new PropertyColumn<Resource>(
                            new Model<String>("Reference"), "ref"),
                    new HeaderlessColumn<Resource>() {
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
                    }
            };
            return (IColumn<Resource>[]) columns;
        }
    } // class CreateVirtualCollectionWizard.ResourcesStep

    private final class GeneratedByStep extends DynamicWizardStep {
        public GeneratedByStep(IDynamicWizardStep previousStep) {
            super(previousStep, "GeneratedBy", "Yada yada yada ...");
            final TextArea<String> descriptionArea =
                new TextArea<String>("vc.generatedBy.description");
            descriptionArea.setRequired(true);
            add(descriptionArea);
            final TextField<String> uriField =
                new TextField<String>("vc.generatedBy.uri");
            uriField.add(new StringValidator.MaximumLengthValidator(255));
            uriField.add(new UrlValidator(UrlValidator.NO_FRAGMENTS));
            add(uriField);
            final TextField<String> queryProfileField =
                new TextField<String>("vc.generatedBy.query.profile");
            add(queryProfileField);
            final TextArea<String> queryValueArea =
                new TextArea<String>("vc.generatedBy.query.value");
            add(queryValueArea);
            
            add(new AbstractFormValidator() {
                @Override
                public FormComponent<?>[] getDependentFormComponents() {
                    return new FormComponent[] { queryProfileField,
                                                 queryValueArea };
                }

                @Override
                public void validate(Form<?> form) {
                    final String profile = queryProfileField.getInput();
                    final String value   = queryValueArea.getInput();
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
                        if (vc.getGeneratedBy() != null) {
                            vc.getGeneratedBy().setQuery(null);
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

    private static final VirtualCollection EMPTY_VC = new VirtualCollection();
    private final VirtualCollection vc = new VirtualCollection();

    public CreateVirtualCollectionWizard(String id) {
        super(id);
        setDefaultModel(new CompoundPropertyModel<VirtualCollection>(this));
        init(new DynamicWizardModel(new GeneralStep()));
    }

    @Override
    public void onCancel() {
        if (!EMPTY_VC.equals(vc)) {
            System.err.println("XXX: VC was modified!");
        }
        setResponsePage(HomePage.class);
    }

    @Override
    public void onFinish() {
        System.err.println("----------------------------------------------");
        System.err.println("Name: " + vc.getName());
        System.err.println("Type: " + vc.getType());
        System.err.println("Desc: " + vc.getDescription());
        System.err.println("creation: " + vc.getCreationDate());
        System.err.println("Purpose: " + vc.getPurpose());
        System.err.println("Rep: " + vc.getReproducibility());
        System.err.println("RepNot: " + vc.getReproducibilityNotice());
        for (String kw : vc.getKeywords()) {
            System.err.println("KW: " + kw);
        }
        for (Creator c : vc.getCreators()) {
            System.err.println("C: " + c.getName() + ", " + c.getEMail());
        }
        for (Resource r : vc.getResources()) {
            System.err.println("R: " + r.getType() + ", " + r.getRef());
        }
        if (vc.getGeneratedBy() != null) {
            final GeneratedBy gb = vc.getGeneratedBy();
            System.err.println("GB.Desc: " + gb.getDescription());
            System.err.println("GB.Uri: " + gb.getURI());
            final GeneratedBy.Query query = gb.getQuery();
            if (query != null) {
                System.err.println("GB.Query.Profile: " + query.getProfile());
                System.err.println("GB.Query.Value:" + query.getValue());
            }
        }
    }

} // class CreateVirtualCollectionWizard
