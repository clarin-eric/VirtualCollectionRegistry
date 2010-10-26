package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardModel;
import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardStep;
import org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ConfirmationDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.HomePage;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@SuppressWarnings("serial")
public class CreateVirtualCollectionWizard extends Wizard {
    private final class GeneralStep extends DynamicWizardStep {
        public GeneralStep() {
            super(null, "General", "Yada yada yada ...");
            add(new RequiredTextField<String>("vc.name"));
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
            add(new TextArea<String>("vc.reproducibilityNotice"));
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
                final AjaxLink<Creator> editLink = new AjaxLink<Creator>("edit") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        final Creator creator = model.getObject();
                        if (creator != null) {
                            editCreatorDialog.setCreator(creator);
                            editCreatorDialog.show(target);
                        }
                    }
                };
                add(editLink);
                final AjaxLink<Creator> removeLink = new AjaxLink<Creator>(
                        "remove") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        final Creator creator = model.getObject();
                        if (creator != null) {
                            deleteCreatorDialog.setCreator(creator);
                            deleteCreatorDialog.show(target);
                        }
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
                super(id, new Model<String>(""));
                this.creatorsTable = creatorsTable;
            }

            public void setCreator(Creator creator) {
                this.creator = creator;
                setMessage(new StringResourceModel("creators.deleteconfirm",
                        new Model<Creator>(creator)));
            }

            @Override
            public void onConfirm(AjaxRequestTarget target) {
                if (creator != null) {
                    vc.getCreators().remove(creator);
                    target.addComponent(creatorsTable);
                }
            }

            @Override
            public void onCancel(AjaxRequestTarget target) {
            }
        } // class
          // CreateVirtualCollectionWizard.CreatorsStep.DeleteCreatorDialog

        private final EditCreatorDialog editCreatorDialog;
        private final DeleteCreatorDialog deleteCreatorDialog;

        public CreatorsStep(IDynamicWizardStep previousStep) {
            super(previousStep, "Creators", "Yada yada yada ...");
            final DataTable<Creator> creatorsTable = new AjaxFallbackDefaultDataTable<Creator>(
                    "creatorsTable", createColumns(),
                    new SortableDataProvider<Creator>() {
                        @Override
                        public Iterator<? extends Creator> iterator(int first,
                                int count) {
                            // XXX: hack
                            Iterator<Creator> i = vc.getCreators().iterator();
                            while (first-- > 0) {
                                i.next();
                            }
                            return i;
                        }

                        @Override
                        public IModel<Creator> model(Creator creator) {
                            return new Model<Creator>(creator);
                        }

                        @Override
                        public int size() {
                            return vc.getCreators().size();
                        }
                    }, 8);
            add(creatorsTable);

            editCreatorDialog = new EditCreatorDialog("editCreatorDialog") {
                @Override
                public void onSubmit(AjaxRequestTarget target, Creator creator) {
                    if (creator != null) {
                        if (!vc.getCreators().contains(creator)) {
                            vc.getCreators().add(creator);
                        }
                        target.addComponent(creatorsTable);
                    }
                }
            };
            add(editCreatorDialog);

            deleteCreatorDialog = new DeleteCreatorDialog(
                    "deleteCreatorDialog", creatorsTable);
            add(deleteCreatorDialog);

            add(new AjaxLink<Object>("add") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    editCreatorDialog.setCreator(new Creator());
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
                    } };
            return (IColumn<Creator>[]) columns;
        }
    } // class CreateVirtualCollectionWizard.CreatorsStep

    private final class ResourcesStep extends DynamicWizardStep {
        private final class ActionsPanel extends Panel {
            public ActionsPanel(String id, final IModel<Resource> model) {
                super(id, model);
                final AjaxLink<Resource> editLink = new AjaxLink<Resource>(
                        "edit") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        final Resource resource = model.getObject();
                        if (resource != null) {
                            editResourceDialog.setResource(resource);
                            editResourceDialog.show(target);
                        }
                    }
                };
                add(editLink);
                final AjaxLink<Resource> removeLink = new AjaxLink<Resource>(
                        "remove") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        final Resource resource = model.getObject();
                        if (resource != null) {
                            deleteResourceDialog.setResource(resource);
                            deleteResourceDialog.show(target);
                        }
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
                super(id, new Model<String>(""));
                this.resourcesTable = resourcesTable;
            }

            public void setResource(Resource resource) {
                this.resource = resource;
                setMessage(new StringResourceModel("resources.deleteconfirm",
                        new Model<Resource>(resource)));
            }

            @Override
            public void onConfirm(AjaxRequestTarget target) {
                if (resource != null) {
                    vc.getResources().remove(resource);
                    target.addComponent(resourcesTable);
                }
            }

            @Override
            public void onCancel(AjaxRequestTarget target) {
            }
        } // class
          // CreateVirtualCollectionWizard.CreatorsStep.DeleteResourceDialog

        private final EditResourceDialog editResourceDialog;
        private final DeleteResourceDialog deleteResourceDialog;

        public ResourcesStep(IDynamicWizardStep previousStep) {
            super(previousStep, "Resources", "Yada yada yada ...");
            final DataTable<Resource> resourcesTable = new AjaxFallbackDefaultDataTable<Resource>(
                    "resourcesTable", createColumns(),
                    new SortableDataProvider<Resource>() {
                        @Override
                        public Iterator<? extends Resource> iterator(int first,
                                int count) {
                            // XXX: hack
                            Iterator<Resource> i = vc.getResources().iterator();
                            while (first-- > 0) {
                                i.next();
                            }
                            return i;
                        }

                        @Override
                        public IModel<Resource> model(Resource resource) {
                            return new Model<Resource>(resource);
                        }

                        @Override
                        public int size() {
                            return vc.getResources().size();
                        }
                    }, 8);
            add(resourcesTable);

            editResourceDialog = new EditResourceDialog("editResourceDialog") {
                @Override
                public void onSubmit(AjaxRequestTarget target, Resource resource) {
                    if (resource != null) {
                        if (!vc.getResources().contains(resource)) {
                            vc.getResources().add(resource);
                        }
                        target.addComponent(resourcesTable);
                    }
                }
            };
            add(editResourceDialog);

            deleteResourceDialog = new DeleteResourceDialog(
                    "deleteResourceDialog", resourcesTable);
            add(deleteResourceDialog);

            final AddResourcesDialog addResourcesDialog = new AddResourcesDialog(
                    "addResourcesDialog") {
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
                    editResourceDialog.setResource(new Resource());
                    editResourceDialog.show(target);
                }
            });

            add(new AjaxLink<Object>("addMore") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    addResourcesDialog.clearForm();
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
                    } };
            return (IColumn<Resource>[]) columns;
        }
    } // class CreateVirtualCollectionWizard.ResourcesStep

    private final class GeneratedByStep extends DynamicWizardStep {
        private final GeneratedBy generatedBy = new GeneratedBy();

        public GeneratedByStep(IDynamicWizardStep previousStep) {
            super(previousStep, "GeneratedBy", "Yada yada yada ...");
            final Form<GeneratedBy> form = new Form<GeneratedBy>(
                    "generatedByForm", new CompoundPropertyModel<GeneratedBy>(
                            generatedBy));
            final TextArea<String> descriptionArea = new TextArea<String>(
                    "description");
            descriptionArea.setRequired(true);
            form.add(descriptionArea);
            form.add(new TextField<String>("uri"));
            add(form);
        }

        @Override
        public boolean isLastStep() {
            return true;
        }

        @Override
        public IDynamicWizardStep next() {
            return null;
        }

        @Override
        public void applyState() {
            vc.setGeneratedBy(generatedBy);
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
        for (Creator c : vc.getCreators()) {
            System.err.println("C: " + c.getName() + ", " + c.getEMail());
        }
        for (Resource r : vc.getResources()) {
            System.err.println("R: " + r.getType() + ", " + r.getRef());
        }
        if (vc.getGeneratedBy() != null) {
            final GeneratedBy gb = vc.getGeneratedBy();
            System.err.println("GB.DESC: " + gb.getDescription());
            System.err.println("GB.UR: " + gb.getURI());
        }
    }

} // class CreateVirtualCollectionWizard
