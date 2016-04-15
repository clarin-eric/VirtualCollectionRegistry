package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.AdminUsersService;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.VolatileEntityModel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ConfirmationDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.PublishConfirmationDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.menu.AjaxLinkMenuItem;
import eu.clarin.cmdi.virtualcollectionregistry.gui.menu.AjaxPopupMenu;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.CollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.VirtualCollectionTable;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidator;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel that shows a table with view, edit, publish, delete options for the
 * collections from an injected provided
 *
 * 
 * @author twagoo
 */
@SuppressWarnings("serial")
public class BrowseEditableCollectionsPanel extends Panel {

    private final static Logger logger = LoggerFactory.getLogger(BrowseEditableCollectionsPanel.class);

    @SpringBean
    private VirtualCollectionRegistry vcr;

    @SpringBean
    private AdminUsersService adminUsersService;
    
    private abstract class PanelWithUserInformation extends Panel {
        
        public PanelWithUserInformation(String id, IModel<VirtualCollection> model) {
            super(id, model);            
        }
        
        protected Principal getUser() {
            ApplicationSession session = (ApplicationSession) getSession();
            Principal principal = session.getPrincipal();
            if (principal == null) {
                throw new WicketRuntimeException("principal == null");
            }
            return principal;
        }

        protected boolean isUserAdmin() {
            final String userName = getUser().getName();
            final boolean admin = userName != null && adminUsersService.isAdmin(userName);
            return admin;
        }
    }
    
    private class ActionsColumn extends PanelWithUserInformation {

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
                                    doPublish(target, model);
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
                editItem.setVisible(false).setEnabled(false);
            }
            
            if (!vc.isPrivate()) {
                publishItem.setVisible(false).setEnabled(false);
                if(!isUserAdmin()) {
                    deleteItem.setVisible(false).setEnabled(false);
                }
            }
            
            if(model.getObject().getState() == VirtualCollection.State.PUBLIC_FROZEN) {
                editItem.setVisible(false).setEnabled(false);
            }
        }
    }

    private class ActionsPanel extends PanelWithUserInformation {

        public ActionsPanel(String id, IModel<VirtualCollection> model) {
            super(id, model);
            setRenderBodyOnly(true);

            final AjaxLink<VirtualCollection> publishLink
                    = new AjaxLink<VirtualCollection>("publish", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            doPublish(target, getModel());
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
                editLink.setVisible(false).setEnabled(false);
            }
            if (!vc.isPrivate()) {
                publishLink.setVisible(false).setEnabled(false);
                if(!isUserAdmin()) {
                    deleteLink.setVisible(false).setEnabled(false);
                }
            }
            if(vc.getState() == VirtualCollection.State.PUBLIC_FROZEN) {
                editLink.setVisible(false).setEnabled(false);
            }
            boolean isVisible = detailsLink.isVisible()
                    || editLink.isVisible() || publishLink.isVisible()
                    || deleteLink.isVisible();
            setVisible(isVisible);
        }
    } // class BrowsePrivateCollectionsPage.ActionsPanel

    private final class PublishCollectionDialog extends PublishConfirmationDialog {

        private IModel<VirtualCollection> vcModel;

        public PublishCollectionDialog(String id,
                final Component updateComponenet) {
            super(id, updateComponenet);
            setInitialWidth(400);
        }

        @Override
        public void onConfirm(AjaxRequestTarget target) {
            try {
                try {
                    prePublicationValidator.validate(vcModel.getObject());                    
                    doPublish(vcModel.getObject().getId(), isFrozen());
                } catch (VirtualCollectionRegistryUsageException ex) {
                    confirmPublishCollectionDialog.showDialogue(target, vcModel, ex.getValidationErrors(), isFrozen());
                }
            } catch (VirtualCollectionRegistryException ex) {
                logger.error("Could not publish collection {}, id {}", vcModel.getObject().getName(), vcModel.getObject().getId(), ex);
                Session.get().error(ex.getMessage());
            }
        }

        public void showDialogue(AjaxRequestTarget target, IModel<VirtualCollection> vc) {
            this.vcModel = vc;
            super.show(target,
                    new StringResourceModel("collections.publishconfirm", vc));
        }
    } // class BrowsePrivateCollectionsPage.PublishCollectionDialog

    private void doPublish(long vcId) throws VirtualCollectionRegistryException {
        doPublish(vcId, false);
    }

    private void doPublish(long vcId, boolean frozen) throws VirtualCollectionRegistryException {
        logger.info("Publishing, frozen = {}", frozen);
        VirtualCollection.State newState = VirtualCollection.State.PUBLIC_PENDING;
        if(frozen) {
            newState = VirtualCollection.State.PUBLIC_FROZEN_PENDING;
        }        
        vcr.setVirtualCollectionState(getUser(), vcId, newState);
    }
    
    private final class ConfirmPublishCollectionDialog extends ConfirmationDialog {

        private long vcId;
        private boolean frozen;

        public ConfirmPublishCollectionDialog(String id,
                final Component updateComponenet) {
            super(id, updateComponenet);
            setInitialWidth(400);
            frozen = false; //default
        }

        @Override
        public void onConfirm(AjaxRequestTarget target) {
            try {
                doPublish(vcId, frozen);
            } catch (VirtualCollectionRegistryException ex) {
                logger.error("Could not publish collection with id {}", vcId, ex);
                Session.get().error(ex.getMessage());
            }
        }

        public void showDialogue(AjaxRequestTarget target, IModel<VirtualCollection> vc, List<String> warnings, boolean frozen) {
            this.vcId = vc.getObject().getId();
            this.frozen = frozen;
            StringBuilder sb = new StringBuilder();
            for (String warning : warnings) {
                sb.append(" -").append(warning).append("\n");
            }
            super.show(target,
                    new StringResourceModel("collections.publishwarningsconfirm", vc, new Object[]{sb}));
        }

        @Override
        protected Model<String> getCssClass() {
            return Model.of("longConfirmationDialog");
        }

    }

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

    private final class EditPublishedCollectionDialog extends ConfirmationDialog {

        private long vcId;

        public EditPublishedCollectionDialog(String id,
                final Component updateComponenet) {
            super(id, updateComponenet);
            setInitialWidth(400);
        }

        @Override
        public void onConfirm(AjaxRequestTarget target) {
            setResponsePage(EditVirtualCollectionPage.class, new PageParameters(Collections.singletonMap("id", vcId)));
        }

        public void showDialogue(AjaxRequestTarget target, VirtualCollection vc) {
            this.vcId = vc.getId();
            super.show(target,
                    new StringResourceModel("collections.editpublishedconfirm",
                            new VolatileEntityModel<>(vc)));
        }
    }

    private final PublishCollectionDialog publishDialog;
    private final DeleteCollectionDialog deleteDialog;
    private final EditPublishedCollectionDialog editPublishedDialog;
    private final ConfirmPublishCollectionDialog confirmPublishCollectionDialog;

    @SpringBean(name = "publication-soft")
    private VirtualCollectionValidator prePublicationValidator;

    /**
     * 
     * @param id panel id
     * @param provider provider for collections that should be shown
     */
    public BrowseEditableCollectionsPanel(String id, CollectionsProvider provider) {
        this(id, provider, false);
    }
    
    /**
     *
     * @param id panel id.
     * @param provider provider for collections that should be shown.
     * @param isAdmin enable (true) or disable (false) the admin options.
     */
    public BrowseEditableCollectionsPanel(String id, CollectionsProvider provider, final boolean isAdmin) {
        super(id);
        final VirtualCollectionTable table
                = new VirtualCollectionTable("collectionsTable", provider, true, isAdmin) {
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

        publishDialog = new PublishCollectionDialog("publishCollectionDialog", table);
        add(publishDialog);

        deleteDialog = new DeleteCollectionDialog("deleteCollectionDialog", table);
        add(deleteDialog);

        editPublishedDialog = new EditPublishedCollectionDialog("editPublishedCollectionDialog", table);
        add(editPublishedDialog);

        confirmPublishCollectionDialog = new ConfirmPublishCollectionDialog("confirmPublishCollectionDialog", table);
        add(confirmPublishCollectionDialog);
    }

    private void doEdit(AjaxRequestTarget target, VirtualCollection vc) {
        if(!vc.isPublicFrozen()) {
            if (vc.isPublic()) {
                // ask for confirmation when trying to edit a published collection
                editPublishedDialog.showDialogue(target, vc);
            } else {
                setResponsePage(EditVirtualCollectionPage.class, new PageParameters(Collections.singletonMap("id", vc.getId())));
            }
        }
    }

    private void doPublish(AjaxRequestTarget target,
            IModel<VirtualCollection> vc) {
        publishDialog.showDialogue(target, vc);
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
