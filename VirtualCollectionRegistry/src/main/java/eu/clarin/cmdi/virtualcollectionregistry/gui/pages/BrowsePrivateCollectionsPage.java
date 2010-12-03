package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import java.security.Principal;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ConfirmationDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.VirtualCollectionTable;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.State;

@AuthorizeInstantiation(Roles.USER)
@SuppressWarnings("serial")
public class BrowsePrivateCollectionsPage extends BasePage {
    private class ActionsPanel extends Panel {
        public ActionsPanel(String id, IModel<VirtualCollection> model) {
            super(id, model);
            setRenderBodyOnly(true);

            final AjaxLink<VirtualCollection> publishLink =
                new AjaxLink<VirtualCollection>("publish", model) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    doPublish(target, getModelObject());
                }
            };
            add(publishLink);

            final AjaxLink<VirtualCollection> editLink =
                new AjaxLink<VirtualCollection>("edit", model) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    doEdit(target, getModelObject());
                }
            };
            add(editLink);

            final AjaxLink<VirtualCollection> deleteLink =
                new AjaxLink<VirtualCollection>("delete", model) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        doDelete(target, getModelObject());
                    }
            };
            add(deleteLink);

            final AjaxLink<VirtualCollection> detailsLink =
                new AjaxLink<VirtualCollection>("details", model) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    doDetails(target, getModelObject());
                }
            };
            add(detailsLink);

            VirtualCollection vc = model.getObject();
            if (vc.isDeleted()) {
                detailsLink.setVisible(false).setEnabled(false);
            }
            if (!vc.isPrivate()) {
                editLink.setVisible(false).setEnabled(false);
                publishLink.setVisible(false).setEnabled(false);
                deleteLink.setVisible(false).setEnabled(false);
            }
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
                final VirtualCollectionRegistry vcr =
                    VirtualCollectionRegistry.instance();
                vcr.setVirtualCollectionState(getUser(), vcId,
                        State.PUBLIC_PENDING);
            } catch (VirtualCollectionRegistryException e) {
                e.printStackTrace();
            }
        }

        public void show(AjaxRequestTarget target, VirtualCollection vc) {
            this.vcId = vc.getId();
            super.show(target,
                    new StringResourceModel("collections.publishconfirm",
                            new Model<VirtualCollection>(vc)));
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
                final VirtualCollectionRegistry vcr =
                    VirtualCollectionRegistry.instance();
                vcr.deleteVirtualCollection(getUser(), vcId);
            } catch (VirtualCollectionRegistryException e) {
                e.printStackTrace();
            }
        }

        public void show(AjaxRequestTarget target, VirtualCollection vc) {
            this.vcId = vc.getId();
            super.show(target,
                    new StringResourceModel("collections.deleteconfirm",
                            new Model<VirtualCollection>(vc)));
        }
    } // class BrowsePrivateCollectionsPage.PublishCollectionDialog

    private final PublishCollectionDialog publishDialog;
    private final DeleteCollectionDialog deleteDialog;

    public BrowsePrivateCollectionsPage() {
        super();
        final VirtualCollectionTable table =
            new VirtualCollectionTable("collectionsTable", true) {
                @Override
                protected Panel createActionPanel(String componentId,
                        IModel<VirtualCollection> model) {
                    return new ActionsPanel(componentId, model);
                }
        };
        add(table);

        publishDialog = new PublishCollectionDialog("publishCollectionDialog",
                table.getTable());
        add(publishDialog);
        deleteDialog = new DeleteCollectionDialog("deleteCollectionDialog",
                table.getTable());
        add(deleteDialog);
    }

    private void doEdit(AjaxRequestTarget target,
            VirtualCollection vc) {
        setResponsePage(new CreateVirtualCollectionPage(vc, getPage()));
    };

    private void doPublish(AjaxRequestTarget target,
            VirtualCollection vc) {
        publishDialog.show(target, vc);
    }

    private void doDelete(AjaxRequestTarget target,
            VirtualCollection vc) {
        deleteDialog.show(target, vc);
    }
    private void doDetails(AjaxRequestTarget target, VirtualCollection vc) {
        setResponsePage(new VirtualCollectionDetailsPage(vc, getPage()));
    }

    private Principal getUser() {
        ApplicationSession session = (ApplicationSession) getSession();
        Principal principal = session.getPrincipal();
        if (principal == null) {
            throw new WicketRuntimeException("principal == null");
        }
        return principal;
    }

} // class BrowsePrivateCollectionsPage