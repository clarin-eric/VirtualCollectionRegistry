package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.VolatileEntityModel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ConfirmationDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.menu.AjaxLinkMenuItem;
import eu.clarin.cmdi.virtualcollectionregistry.gui.menu.AjaxPopupMenu;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.CollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.VirtualCollectionTable;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.security.Principal;
import java.util.Collections;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Panel that shows a table with view, edit, publish, delete options for the
 * collections from an injected provided
 *
 * @author twagoo
 */
public class BrowseEditableCollectionsPanel extends Panel {

    @SpringBean
    private VirtualCollectionRegistry vcr;

    private class ActionsColumn extends Panel {

        public ActionsColumn(String id, IModel<VirtualCollection> model) {
            super(id, model);
            setRenderBodyOnly(true);

            final AjaxPopupMenu menu
                    = new AjaxPopupMenu("menu", new Model<String>("[actions]"));

            final AjaxLinkMenuItem<VirtualCollection> publishItem
                    = new AjaxLinkMenuItem<VirtualCollection>(
                            new Model<String>("Publish"), model, "publish") {
                                @Override
                                protected void onClick(AjaxRequestTarget target,
                                        IModel<VirtualCollection> model) {
                                    doPublish(target, model.getObject());
                                }
                            };
                    menu.add(publishItem);

                    final AjaxLinkMenuItem<VirtualCollection> editItem
                            = new AjaxLinkMenuItem<VirtualCollection>(
                                    new Model<String>("Edit"), model, "edit") {
                                        @Override
                                        protected void onClick(AjaxRequestTarget target,
                                                IModel<VirtualCollection> model) {
                                            doEdit(target, model.getObject());
                                        }
                                    };
                            menu.add(editItem);

                            final AjaxLinkMenuItem<VirtualCollection> deleteItem
                                    = new AjaxLinkMenuItem<VirtualCollection>(
                                            new Model<String>("Delete"), model, "delete") {
                                                @Override
                                                protected void onClick(AjaxRequestTarget target,
                                                        IModel<VirtualCollection> model) {
                                                    doDelete(target, model.getObject());
                                                }
                                            };
                                    menu.add(deleteItem);

                                    final AjaxLinkMenuItem<VirtualCollection> detailsItem
                                            = new AjaxLinkMenuItem<VirtualCollection>(
                                                    new Model<String>("Details"), model, "details") {
                                                        @Override
                                                        protected void onClick(AjaxRequestTarget target,
                                                                IModel<VirtualCollection> model) {
                                                            doDetails(target, model);
                                                        }
                                                    };
                                            menu.add(detailsItem);
                                            add(menu);

                                            final VirtualCollection vc = model.getObject();
                                            if (vc.isDeleted()) {
                                                detailsItem.setVisible(false).setEnabled(false);
                                            }
                                            if (!vc.isPrivate()) {
                                                editItem.setVisible(false).setEnabled(false);
                                                publishItem.setVisible(false).setEnabled(false);
                                                deleteItem.setVisible(false).setEnabled(false);
                                            }
        }
    }

    private class ActionsPanel extends Panel {

        public ActionsPanel(String id, IModel<VirtualCollection> model) {
            super(id, model);
            setRenderBodyOnly(true);

            final AjaxLink<VirtualCollection> publishLink
                    = new AjaxLink<VirtualCollection>("publish", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            doPublish(target, getModelObject());
                        }
                    };
            add(publishLink);

            final AjaxLink<VirtualCollection> editLink
                    = new AjaxLink<VirtualCollection>("edit", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            doEdit(target, getModelObject());
                        }
                    };
            add(editLink);

            final AjaxLink<VirtualCollection> deleteLink
                    = new AjaxLink<VirtualCollection>("delete", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            doDelete(target, getModelObject());
                        }
                    };
            add(deleteLink);

            final AjaxLink<VirtualCollection> detailsLink
                    = new AjaxLink<VirtualCollection>("details", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            doDetails(target, getModel());
                        }
                    };
            add(detailsLink);

            final VirtualCollection vc = model.getObject();
            if (vc.isDeleted()) {
                detailsLink.setVisible(false).setEnabled(false);
            }
            if (!vc.isPrivate()) {
                editLink.setVisible(false).setEnabled(false);
                publishLink.setVisible(false).setEnabled(false);
                deleteLink.setVisible(false).setEnabled(false);
            }
            boolean isVisible = detailsLink.isVisible()
                    || editLink.isVisible() || publishLink.isVisible()
                    || deleteLink.isVisible();
            setVisible(isVisible);
        }
    } // class BrowsePrivateCollectionsPage.ActionsPanel

    private final class PublishCollectionDialog extends ConfirmationDialog {

        private long vcId;

        public PublishCollectionDialog(String id,
                final Component updateComponenet) {
            super(id, updateComponenet);
            setInitialWidth(400);
        }

        @Override
        public void onConfirm(AjaxRequestTarget target) {
            try {
                vcr.setVirtualCollectionState(getUser(), vcId,
                        VirtualCollection.State.PUBLIC_PENDING);
            } catch (VirtualCollectionRegistryException e) {
                e.printStackTrace();
            }
        }

        public void show(AjaxRequestTarget target, VirtualCollection vc) {
            this.vcId = vc.getId();
            super.show(target,
                    new StringResourceModel("collections.publishconfirm",
                            new VolatileEntityModel<VirtualCollection>(vc)));
        }
    } // class BrowsePrivateCollectionsPage.PublishCollectionDialog

    private final class DeleteCollectionDialog extends ConfirmationDialog {

        private long vcId;

        public DeleteCollectionDialog(String id,
                final Component updateComponenet) {
            super(id, updateComponenet);
            setInitialWidth(400);
        }

        @Override
        public void onConfirm(AjaxRequestTarget target) {
            try {
                vcr.deleteVirtualCollection(getUser(), vcId);
            } catch (VirtualCollectionRegistryException e) {
                e.printStackTrace();
            }
        }

        public void show(AjaxRequestTarget target, VirtualCollection vc) {
            this.vcId = vc.getId();
            super.show(target,
                    new StringResourceModel("collections.deleteconfirm",
                            new VolatileEntityModel<VirtualCollection>(vc)));
        }
    } // class BrowsePrivateCollectionsPage.PublishCollectionDialog

    private final PublishCollectionDialog publishDialog;
    private final DeleteCollectionDialog deleteDialog;

    /**
     * 
     * @param id panel id
     * @param provider provider for collections that should be shown
     */
    public BrowseEditableCollectionsPanel(String id, CollectionsProvider provider) {
        super(id);
        final VirtualCollectionTable table
                = new VirtualCollectionTable("collectionsTable", provider, true) {
                    @Override
                    protected Panel createActionColumn(String componentId,
                            IModel<VirtualCollection> model) {
                        return new ActionsColumn(componentId, model);
                    }

                    @Override
                    protected Panel createActionPanel(String componentId,
                            IModel<VirtualCollection> model) {
                        return new ActionsPanel(componentId, model);
                    }
                };
        add(table);

        publishDialog = new PublishCollectionDialog("publishCollectionDialog",
                table);
        add(publishDialog);
        deleteDialog = new DeleteCollectionDialog("deleteCollectionDialog",
                table);
        add(deleteDialog);
    }

    private void doEdit(AjaxRequestTarget target,
            VirtualCollection vc) {
        setResponsePage(EditVirtualCollectionPage.class, new PageParameters(Collections.singletonMap("id", vc.getId())));
    }

    private void doPublish(AjaxRequestTarget target,
            VirtualCollection vc) {
        publishDialog.show(target, vc);
    }

    private void doDelete(AjaxRequestTarget target,
            VirtualCollection vc) {
        deleteDialog.show(target, vc);
    }

    private void doDetails(AjaxRequestTarget target, IModel<VirtualCollection> vc) {
        setResponsePage(VirtualCollectionDetailsPage.class, VirtualCollectionDetailsPage.createPageParameters(vc.getObject(), getPage().getPageReference()));
    }

    private Principal getUser() {
        return ((BasePage) getPage()).getUser();
    }
}