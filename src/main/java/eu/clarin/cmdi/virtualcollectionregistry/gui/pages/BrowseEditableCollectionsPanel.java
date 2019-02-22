package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.AdminUsersService;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionValidationException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.VolatileEntityModel;
import eu.clarin.cmdi.wicket.components.citation.CitationPanelFactory;
import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.Dialogs;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.CollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.VirtualCollectionTable;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.State;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionValidator;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.PageReference;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.migrate.StringResourceModelMigration;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
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
    }
    
    private class EmptyPanel extends PanelWithUserInformation {
        public EmptyPanel(String id, IModel<VirtualCollection> model) {
            super(id, model);
            setRenderBodyOnly(true);
            add(new Label("lbl", new Model<>("")));
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
                            confirmPublish(target, getModel());
                        }
                    };
            UIUtils.addTooltip(publishLink, "Publish this collection");
            add(publishLink);

            final AjaxLink<VirtualCollection> editLink
                    = new AjaxLink<VirtualCollection>("edit", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            doEdit(target, getModelObject());
                        }
                    };
            UIUtils.addTooltip(editLink, "Edit this collection");
            add(editLink);

            final AjaxLink<VirtualCollection> deleteLink
                    = new AjaxLink<VirtualCollection>("delete", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            doDelete(target, getModelObject());
                        }
                    };
            UIUtils.addTooltip(deleteLink, "Delete this collection");
            add(deleteLink);

            final AjaxLink<VirtualCollection> detailsLink
                    = new AjaxLink<VirtualCollection>("details", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            doDetails(target, getModel());
                        }
                    };
            UIUtils.addTooltip(detailsLink, "View collection details");
            add(detailsLink);
            /*
            final CitationDialog citationDialog = new CitationDialog("citationDialog", model);
            add(citationDialog);
            
            final AjaxLink<VirtualCollection> citeLink
                    = new AjaxLink<VirtualCollection>("cite", model) {
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            citationDialog.show(target);
                        }
                    };
            UIUtils.addTooltip(citeLink, "Cite this collection");
            citeLink.setEnabled(model.getObject().isCiteable());
            add(citeLink);
            */
            add(CitationPanelFactory.getCitationPanel("cite", model, true));
            
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
            if(vc.getState() == VirtualCollection.State.PUBLIC_FROZEN && !isUserAdmin()) {
                editLink.setVisible(false).setEnabled(false);
            }
            boolean isVisible = detailsLink.isVisible()
                    || editLink.isVisible() || publishLink.isVisible()
                    || deleteLink.isVisible();
            setVisible(isVisible);
        }
    } // class BrowsePrivateCollectionsPage.ActionsPanel

    private final eu.clarin.cmdi.wicket.components.ConfirmationDialog publishDialog;
    private IModel<String> confirmPublishCollectionModel = new Model<>("Not initialized");
    private final eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler<VirtualCollection> confirmHandler;
    private final eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler<VirtualCollection> confirmFrozenHandler;
    
    private final eu.clarin.cmdi.wicket.components.ConfirmationDialog deleteDialog;
    private IModel<String>  confirmDeleteCollectionModel = new Model<>("Not initialized");
    private final eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler<VirtualCollection> confirmDeleteHandler;
            
    private final eu.clarin.cmdi.wicket.components.ConfirmationDialog editPublishedDialog;
    private IModel<String> confirmEditCollectionModel = new Model<>("Not initialized");
    private final eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler<VirtualCollection> confirmEditHandler;
    
    private IModel<String> publishCollectionModel = new Model<>("");
    private final eu.clarin.cmdi.wicket.components.ConfirmationDialog confirmPublishCollectionWithWarningsDialog;
    private final eu.clarin.cmdi.wicket.components.ConfirmationDialog.PublishHandler<VirtualCollection> confirmPublishCollectionWithWarningsHandler;
    
    @SpringBean(name = "publication-soft")
    private VirtualCollectionValidator prePublicationValidator;

    /**
     * 
     * @param id panel id
     * @param provider provider for collections that should be shown
     */
    public BrowseEditableCollectionsPanel(String id, CollectionsProvider provider, final PageReference reference) {
        this(id, provider, false, reference);
    }
    
    /**
     *
     * @param id panel id.
     * @param provider provider for collections that should be shown.
     * @param isAdmin enable (true) or disable (false) the admin options.
     */
    public BrowseEditableCollectionsPanel(String id, CollectionsProvider provider, final boolean isAdmin, final PageReference reference) {
        super(id);
        this.setOutputMarkupId(true);       
        final VirtualCollectionTable table
                = new VirtualCollectionTable("collectionsTable", provider, true, isAdmin) {
                    @Override
                    protected Panel createActionColumn(String componentId,
                            IModel<VirtualCollection> model) {
                        State state = model.getObject().getState();
                        if(state == State.PUBLIC_FROZEN || state == State.PUBLIC || state == State.PRIVATE || isAdmin) {
                            //return new ActionsColumn(componentId, model);
                            return new ActionsPanel(componentId, model);
                        } else {
                            return new EmptyPanel(componentId, model);
                        }
                    }

                    @Override
                    protected Panel createActionPanel(String componentId,
                            IModel<VirtualCollection> model) {
                        State state = model.getObject().getState();
                        if(state == State.PUBLIC_FROZEN || state == State.PUBLIC || state == State.PRIVATE || isAdmin) {
                            return new ActionsPanel(componentId, model);
                        } else {
                            return new EmptyPanel(componentId, model);
                        }
                    }
                    
                    @Override
                    protected PageReference getPageReference() {
                        return reference;
                    }
                };
        add(table);
  
        confirmHandler = new eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler<VirtualCollection>() {
            private IModel<VirtualCollection> model;
            
            @Override
            public void handle(AjaxRequestTarget target) {
                prePublishCheck(target, this.model, false);
                /*
                VirtualCollection vc = this.model.getObject();
                if (vc != null) {
                    try {
                        try {
                            prePublicationValidator.validate(vc);                    
                            doPublish(vc.getId(), false, target); //TODO: howto force publish of a frozen collection?
                        } catch (VirtualCollectionValidationException ex) {
                            logger.info("Confirm publishing of collection with errors");                           
                            confirmPublishWithWarnings(target, model, ex.getAllErrorsAsList());
                        }
                    } catch (VirtualCollectionRegistryException ex) {
                        logger.error("Could not publish collection {}, id {}", vc.getName(), vc.getId(), ex);
                        Session.get().error(ex.getMessage());
                        throw new RuntimeException();
                    }
                } else {
                    logger.info("Failed to validate null virtual collection");
                    throw new RuntimeException();
                }
                */
            }            

            @Override
            public void setObject(IModel<VirtualCollection> model) {
                this.model = model;
            }
        };
        
        confirmFrozenHandler = new eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler<VirtualCollection>() {
            private IModel<VirtualCollection> model;
            
            @Override
            public void handle(AjaxRequestTarget target) {
                prePublishCheck(target, this.model, true);
            }            

            @Override
            public void setObject(IModel<VirtualCollection> model) {
                this.model = model;
            }
        };
        
        confirmPublishCollectionWithWarningsHandler = new eu.clarin.cmdi.wicket.components.ConfirmationDialog.PublishHandler<VirtualCollection>() {
            private IModel<VirtualCollection> model;
            private boolean frozen;
            
            @Override
            public void handle(AjaxRequestTarget target) {
               VirtualCollection vc = this.model.getObject();
                try {
                    doPublish(vc.getId(), this.frozen, target);
                 } catch (VirtualCollectionRegistryException ex) {
                    logger.error("Could not publish collection {}, id {}", vc.getName(), vc.getId(), ex);
                    Session.get().error(ex.getMessage());
                    throw new RuntimeException();
                }
            }            

            @Override
            public void setObject(IModel<VirtualCollection> model) {
                this.model = model;
            }
            
            @Override
            public void setFrozen(boolean frozen) {
                this.frozen = frozen;
            }
        };
        
        confirmEditHandler  = new eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler<VirtualCollection>() {
            private IModel<VirtualCollection> model;
            
            @Override
            public void handle(AjaxRequestTarget target) {
                if(model.getObject() != null) {
                    setResponsePage(CreateAndEditVirtualCollectionPage.class, 
                        buildParamsFromMap(Collections.singletonMap("id", model.getObject().getId())));
                }
            }            

            @Override
            public void setObject(IModel<VirtualCollection> model) {
                this.model = model;
            }
        };
        
        confirmDeleteHandler  = new eu.clarin.cmdi.wicket.components.ConfirmationDialog.Handler<VirtualCollection>() {
            private IModel<VirtualCollection> model;
            
            @Override
            public void handle(AjaxRequestTarget target) {
                try {
                   vcr.deleteVirtualCollection(getUser(), model.getObject().getId());
                } catch (VirtualCollectionRegistryException e) {
                    logger.error("Failed to delete virtual collection", e);
                }
            }            

            @Override
            public void setObject(IModel<VirtualCollection> model) {
                this.model = model;
            }
        };
        
        publishDialog = Dialogs.createConfirmPublishCollectionDialog("confirmPublishCollectionDialog", publishCollectionModel, confirmHandler, confirmFrozenHandler);
        add(publishDialog);

        confirmPublishCollectionWithWarningsDialog = Dialogs.createConfirmPublishCollectionWithWarningsDialog("publishCollectionDialog", confirmPublishCollectionModel, confirmPublishCollectionWithWarningsHandler);
        add(confirmPublishCollectionWithWarningsDialog);
        
        deleteDialog = Dialogs.createDeleteEditCollectionDialog("deleteCollectionDialog", confirmDeleteCollectionModel, confirmDeleteHandler);
        add(deleteDialog);

        editPublishedDialog = Dialogs.createConfirmEditCollectionDialog("editPublishedCollectionDialog", confirmEditCollectionModel, confirmEditHandler);
        add(editPublishedDialog);
    }

    private void prePublishCheck(AjaxRequestTarget target, IModel<VirtualCollection> model, boolean frozen) {
        VirtualCollection vc = model.getObject();
        if (vc != null) {
            try {
                try {
                    prePublicationValidator.validate(vc);                    
                    doPublish(vc.getId(), frozen, target); 
                } catch (VirtualCollectionValidationException ex) {
                    logger.info("Confirm publishing of collection with errors");                           
                    confirmPublishWithWarnings(target, model, ex.getAllErrorsAsList(), frozen);
                }
            } catch (VirtualCollectionRegistryException ex) {
                logger.error("Could not publish collection {}, id {}", vc.getName(), vc.getId(), ex);
                Session.get().error(ex.getMessage());
                throw new RuntimeException();
            }
        } else {
            logger.info("Failed to validate null virtual collection");
            throw new RuntimeException();
        }
    }
    
    private void doEdit(AjaxRequestTarget target, VirtualCollection vc) {
        if(!vc.isPublicFrozen() || isUserAdmin()) {
            if (vc.isPublic()) {
                // ask for confirmation when trying to edit a published collection                
                confirmEditCollectionModel.setObject(new StringResourceModel("collections.editpublishedconfirm", new VolatileEntityModel<>(vc)).getObject());
                confirmEditHandler.setObject(new VolatileEntityModel<>(vc));
                editPublishedDialog.show(target);
            } else if (vc.isPublicFrozen()) {
                // ask for confirmation when trying to edit a published collection
                // todo: custom message for editing of frozen collections
                confirmEditCollectionModel.setObject(new StringResourceModel("collections.editpublishedfrozenconfirm", new VolatileEntityModel<>(vc)).getObject());
                confirmEditHandler.setObject(new VolatileEntityModel<>(vc));
                editPublishedDialog.show(target);
            } else {
                setResponsePage(CreateAndEditVirtualCollectionPage.class, 
                        buildParamsFromMap(Collections.singletonMap("id", vc.getId())));
            }
        }
    }

    private void confirmPublish(AjaxRequestTarget target, IModel<VirtualCollection> model) {
        publishCollectionModel.setObject(new StringResourceModel("collections.publishconfirm", model).getObject());
        confirmHandler.setObject(model);
        confirmFrozenHandler.setObject(model);
        publishDialog.show(target);
    }

    private void confirmPublishWithWarnings(AjaxRequestTarget target, IModel<VirtualCollection> model, List<String> errors, boolean frozen) {
        StringBuilder sb = new StringBuilder();
        for (String warning : errors) {
            sb.append(" -").append(warning).append("\n");
        }

        StringResourceModel stringResourceModel = 
            StringResourceModelMigration.of("collections.publishwarningsconfirm", model, new Object[]{sb});
                            
        publishDialog.close(target);
        confirmPublishCollectionModel.setObject(stringResourceModel.getObject());
        confirmPublishCollectionWithWarningsHandler.setObject(model);
        confirmPublishCollectionWithWarningsHandler.setFrozen(frozen);
        confirmPublishCollectionWithWarningsDialog.show(target);
    }
    
    private void doDelete(AjaxRequestTarget target,
            VirtualCollection vc) {
        confirmDeleteCollectionModel.setObject(new StringResourceModel("collections.deleteconfirm",new VolatileEntityModel<VirtualCollection>(vc)).getObject());
        confirmDeleteHandler.setObject(new VolatileEntityModel<>(vc));
        deleteDialog.show(target);
    }

    private void doDetails(AjaxRequestTarget target, IModel<VirtualCollection> vc) {
        //TODO: handle admin page
        setResponsePage(VirtualCollectionDetailsPage.class,
            VirtualCollectionDetailsPage.createPageParameters(
                vc.getObject(), getPage().getPageReference(),
                VirtualCollectionDetailsPage.BackPage.PRIVATE_LISTING));
    }

    private void doPublish(long vcId, boolean frozen, AjaxRequestTarget target) throws VirtualCollectionRegistryException {
        logger.info("Publishing, frozen = {}", frozen);

        //Publish if resources are valid
        VirtualCollection.State newState = VirtualCollection.State.PUBLIC_PENDING;
        if(frozen) {
            newState = VirtualCollection.State.PUBLIC_FROZEN_PENDING;
        }        
        vcr.setVirtualCollectionState(getUser(), vcId, newState);
        
        if(publishDialog.isVisible()) {
            publishDialog.close(target);
        }
        if(confirmPublishCollectionWithWarningsDialog.isVisible()) {
            confirmPublishCollectionWithWarningsDialog.close(target);
        }
    }
    
    private Principal getUser() {
        return ((BasePage) getPage()).getUser();
    }
    
    protected boolean isUserAdmin() {
        final String userName = getUser().getName();
        final boolean admin = userName != null && adminUsersService.isAdmin(userName);
        return admin;
    }
    
    protected PageParameters buildParamsFromMap(Map<String, Long> map) {
        PageParameters params = new PageParameters();
        for(String key : map.keySet()) {
            params.add(key, map.get(key));
        }
        return params;
    }
}
