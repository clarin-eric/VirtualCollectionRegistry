package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import java.security.Principal;
import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ConfirmationDialog;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.State;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollectionList;

@AuthorizeInstantiation(Roles.USER)
@SuppressWarnings("serial")
public class BrowsePrivateCollectionsPage extends BasePage {

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
            protected int getCollectionsCount() {
                try {
                    final VirtualCollectionRegistry vcr =
                        VirtualCollectionRegistry.instance();
                    VirtualCollectionList results =
                        vcr.getVirtualCollections(getUser(), null, -1, 0);
                    return results.getTotalCount();
                } catch (VirtualCollectionRegistryException e) {
                    throw new WicketRuntimeException(e);
                }
            }

            @Override
            protected Iterator<VirtualCollection> getCollections(int first,
                    int count) {
                try {
                    final VirtualCollectionRegistry vcr =
                        VirtualCollectionRegistry.instance();
                    VirtualCollectionList results =
                        vcr.getVirtualCollections(getUser(), null,
                                first, count);
                    return results.getItems().iterator();
                } catch (VirtualCollectionRegistryException e) {
                    throw new WicketRuntimeException(e);
                }
            }

            @Override
            protected void doEdit(AjaxRequestTarget target,
                    VirtualCollection vc) {
                setResponsePage(new CreateVirtualCollectionPage(vc, getPage()));
            };

            @Override
            protected void doPublish(AjaxRequestTarget target,
                    VirtualCollection vc) {
                publishDialog.show(target, vc);
            }

            @Override
            protected void doDelete(AjaxRequestTarget target,
                    VirtualCollection vc) {
                deleteDialog.show(target, vc);
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

    private Principal getUser() {
        ApplicationSession session = (ApplicationSession) getSession();
        Principal principal = session.getPrincipal();
        if (principal == null) {
            throw new WicketRuntimeException("principal == null");
        }
        return principal;
    }

} // class BrowsePrivateCollectionsPage
