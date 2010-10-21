package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ConfirmationDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.HomePage;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource.Type;

@SuppressWarnings("serial")
public class CreateVirtualCollectionWizard extends Wizard {
    private final class GeneralStep extends WizardStep {
        public GeneralStep() {
            super("General", "Yada yada yada ...");
            add(new RequiredTextField<String>("vc.name"));
            final DropDownChoice<VirtualCollection.Type> typeChoice =
                new DropDownChoice<VirtualCollection.Type>("vc.type",
                        Arrays.asList(VirtualCollection.Type.values()),
                        new EnumChoiceRenderer<VirtualCollection.Type>(this));
            typeChoice.setRequired(true);
            add(typeChoice);
            add(new TextArea<String>("vc.description"));
        }
    } // class CreateVirtualCollectionWizard.GeneralStep

    private final class CreatorsStep extends WizardStep {
        private final class ActionsPanel extends Panel {
            public ActionsPanel(String id, final IModel<Creator> model) {
                super(id, model);
                final AjaxLink<Creator> editLink =
                    new AjaxLink<Creator>("edit") {
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
                final AjaxLink<Creator> removeLink =
                    new AjaxLink<Creator>("remove") {
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

        private final class DeleteCreatorDialog extends
                ConfirmationDialog {
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
        } // class CreateVirtualCollectionWizard.CreatorsStep.DeleteConfirmDialog
        
        private final EditCreatorDialog editCreatorDialog;
        private final DeleteCreatorDialog deleteCreatorDialog;

        public CreatorsStep() {
            super("Creators", "Yada yada yada ...");
            final DataTable<Creator> creatorsTable =
                new AjaxFallbackDefaultDataTable<Creator>("creatorsTable",
                        createColumns(),
                        new SortableDataProvider<Creator>() {
                            @Override
                            public Iterator<? extends Creator> iterator(
                                    int first, int count) {
                                // XXX: hack
                                Iterator<Creator> i =
                                    vc.getCreators().iterator();
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
                        },                   
                        8);
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

            deleteCreatorDialog =
                new DeleteCreatorDialog("deleteCreatorDialog",
                        creatorsTable);
            add(deleteCreatorDialog);

            add(new AjaxLink<Object>("add") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    editCreatorDialog.setCreator(new Creator());
                    editCreatorDialog.show(target);
                }
            });
        }

        @SuppressWarnings("unchecked")
        private IColumn<Creator>[] createColumns() {
            final IColumn<?>[] columns = new IColumn<?>[] {
                    new PropertyColumn<Creator>(
                            new Model<String>("Name"), "name"),
                    new PropertyColumn<Creator>(
                            new Model<String>("EMail"), "email"),
                    new PropertyColumn<Creator>(
                            new Model<String>("Organisation"), "organisation"),
                    new HeaderlessColumn<Creator>() {
                        @Override
                        public void populateItem(
                                Item<ICellPopulator<Creator>> cellItem,
                                String compontentId,
                                IModel<Creator> model) {
                            cellItem.add(new ActionsPanel(compontentId, model));
                        }
                    }
            };
            return (IColumn<Creator>[]) columns;
        }
    } // class CreateVirtualCollectionWizard.CreatorsStep

    private final class ResourcesStep extends WizardStep {
        private final class ActionsPanel extends Panel {
            public ActionsPanel(String id, final IModel<Resource> model) {
                super(id, model);
                final AjaxLink<Resource> editLink =
                    new AjaxLink<Resource>("edit") {
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
                final AjaxLink<Resource> removeLink =
                    new AjaxLink<Resource>("remove") {
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
        } // class CreateVirtualCollectionWizard.CreatorsStep.DeleteConfirmDialog

        private final EditResourceDialog editResourceDialog;
        private final DeleteResourceDialog deleteResourceDialog;
        
        public ResourcesStep() {
            super("Resources", "Yada yada yada ...");
            final DataTable<Resource> resourcesTable =
                new AjaxFallbackDefaultDataTable<Resource>("resourcesTable",
                        createColumns(),
                        new SortableDataProvider<Resource>() {
                            @Override
                            public Iterator<? extends Resource> iterator(
                                    int first, int count) {
                                // XXX: hack
                                Iterator<Resource> i =
                                    vc.getResources().iterator();
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
                        },                   
                        8);
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

            deleteResourceDialog =
                new DeleteResourceDialog("deleteResourceDialog",
                        resourcesTable);
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
                    editResourceDialog.setResource(new Resource());
                    editResourceDialog.show(target);
                }
            });

            add(new AjaxLink<Object>("addMore") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    addResourcesDialog.show(target);
                }
            });
        }

        @SuppressWarnings("unchecked")
        private IColumn<Resource>[] createColumns() {
            final IColumn<?>[] columns = new IColumn<?>[] {
                    new PropertyColumn<Resource>(
                            new Model<String>("Type"), "type"),
                    new PropertyColumn<Resource>(
                            new Model<String>("Reference"), "ref"),
                    new HeaderlessColumn<Resource>() {
                        @Override
                        public void populateItem(
                                Item<ICellPopulator<Resource>> cellItem,
                                String compontentId,
                                IModel<Resource> model) {
                            cellItem.add(new ActionsPanel(compontentId, model));
                        }
                    }
            };
            return (IColumn<Resource>[]) columns;
        }
    } // class CreateVirtualCollectionWizard.CreatorsStep

    private final VirtualCollection vc = new VirtualCollection();

    public CreateVirtualCollectionWizard(String id) {
        super(id);
        vc.getResources().add(new Resource(Type.METADATA, "http://some/where1"));
        vc.getResources().add(new Resource(Type.METADATA, "http://some/where2"));
        vc.getResources().add(new Resource(Type.METADATA, "http://some/where3"));
        vc.getResources().add(new Resource(Type.METADATA, "http://some/where4"));
        setDefaultModel(new CompoundPropertyModel<VirtualCollection>(this));
        WizardModel model = new WizardModel();
        model.add(new GeneralStep());
        model.add(new CreatorsStep());
        model.add(new ResourcesStep());
        init(model);
    }

    @Override
    public void onCancel() {
        setResponsePage(HomePage.class);
    }

    @Override
    public void onFinish() {
        System.err.println("Name: " + vc.getName());
        System.err.println("Type: " + vc.getType());
        System.err.println("Desc: " + vc.getDescription());
        for (Creator c : vc.getCreators()) {
            System.err.println("C: " + c.getName() + ", " + c.getEMail());
        }
        for (Resource r : vc.getResources()) {
            System.err.println("R: " + r.getType() + ", " + r.getRef());
        }
    }

} // class CreateVirtualCollectionWizard
