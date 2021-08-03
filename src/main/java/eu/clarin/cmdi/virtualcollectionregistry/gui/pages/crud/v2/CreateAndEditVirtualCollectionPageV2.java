package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BasePage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BrowsePrivateCollectionsPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BrowsePublicCollectionsPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v1.CreateAndEditVirtualCollectionPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.CollectionListPanel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.CreateAndEditPanel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmAction;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.dialogs.ModalConfirmDialog;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Event;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.EventType;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.Listener;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission.SubmissionUtils;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.wicket.components.panel.EmptyPanel;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.List;

@AuthorizeInstantiation(Roles.USER)
public class CreateAndEditVirtualCollectionPageV2 extends BasePage {

    private static Logger logger = LoggerFactory.getLogger(CreateAndEditVirtualCollectionPageV2.class);

    public final static String PARAM_VC_ID = "collection-id";

    @SpringBean
    private VirtualCollectionRegistry vcr;

    private final ModalConfirmDialog modal;

//    private final PrivateCollectionsManager provider;

    private VirtualCollection vc = null;;

    /**
     * Create a new virtual collection
     *
     * @throws VirtualCollectionRegistryException
     */
    public CreateAndEditVirtualCollectionPageV2() throws VirtualCollectionRegistryException {
        this(null, null);
    }

    /**
     * Edit an existing virtual collection
     *
     * @param params
     * @throws VirtualCollectionRegistryException
     */
    public CreateAndEditVirtualCollectionPageV2(PageParameters params) throws VirtualCollectionRegistryException {
        this(params.get(PARAM_VC_ID).toLong(), null);
    }

    private Long originalCollectionId;


    /**
     * Based on the supplied id either create a new collection (id = null or id does not return a collection)
     * or edit an existing collection.
     *
     * @param id
     * @param previousPage
     * @throws VirtualCollectionRegistryException
     */
    public CreateAndEditVirtualCollectionPageV2(Long id, final Page previousPage) throws VirtualCollectionRegistryException {
        this.originalCollectionId = id; //if null we are creating a new collection, otherwise an existing collection is being updated
        logger.info("Collection id = {}", id);
        if(id != null) {
            vc = vcr.retrieveVirtualCollection(id);
            if (vc != null) {
                this.checkAccess(vc);
            }
        }

        if(vc == null) {
            VirtualCollection submitted_vc = SubmissionUtils.retrieveCollection(getSession());
            Long mergeWithCollectionId = SubmissionUtils.retrieveCollectionMergeId(getSession());
            if(submitted_vc != null) {
                logger.info("Processing cached collection. id="+submitted_vc.getId());
                if(mergeWithCollectionId == null) {
                    logger.info("New collection");
                    vc = submitted_vc;
                    //Check if any of the properties require updating
                    if(vc.getOwner() == null) {
                        Principal p = getUser();
                        vc.setOwner(new User(p.getName()));
                        vc.getCreators().add(new Creator(p.getName(), ""));
                    }
                } else {
                    logger.info("Merge with collection id =" +mergeWithCollectionId);
                    vc = vcr.retrieveVirtualCollection(mergeWithCollectionId);
                    vc.merge(submitted_vc);
                    originalCollectionId = vc.getId();
                }

                //this.submissionMode = true;
            }
        }

        modal = new ModalConfirmDialog("modal");
        modal.addListener(new Listener() {
            @Override
            public void handleEvent(final Event event) {
                switch(event.getType()) {
                    case OK:
                        logger.trace("Default confirm");
                        event.updateTarget();
                        break;
                    case CONFIRMED_DELETE:
                        if(event.getData() == null) {
                            logger.trace("No collection found for removal");
                        } else {
                            removeCollection((VirtualCollection)event.getData());
                        }
                        event.updateTarget();
                        break;
                    case CANCEL:
                        event.updateTarget();
                        break;
                }
            }
        });
        add(modal);

        final CreateAndEditPanel crud = new CreateAndEditPanel(vcr, "create_and_edit_panel", modal);
        crud.addListener(new Listener<VirtualCollection>() {
            @Override
            public void handleEvent(Event<VirtualCollection> event) {
                switch(event.getType()) {
                    case SAVE:
                        logger.trace("Saving collection, mode="+originalCollectionId==null ? "create" : "update");
                        try {
                            if(originalCollectionId == null) {
                                Application
                                    .get()
                                    .getRegistry()
                                    .createVirtualCollection(event.getPrincipal(), event.getData());
                            } else {
                                Application
                                    .get()
                                    .getRegistry()
                                    .updateVirtualCollection(event.getPrincipal(), originalCollectionId, event.getData());
                            }
                        } catch(VirtualCollectionRegistryException ex) {
                            logger.error("Failed to persist collect. Error: {}", ex.toString());
                            throw new RuntimeException("Failed to persist collection", ex);
                        }
                        throw new RestartResponseException(BrowsePrivateCollectionsPage.class);
                    case CANCEL:
                        SubmissionUtils.clearCollectionFromSession(getSession());
                        throw new RestartResponseException(BrowsePrivateCollectionsPage.class);
                    default:
                        throw new RuntimeException("Unhandled event. type = "+event.getType().toString());
                }
            }
        });
        add(crud);
        
        if(vc != null) {
            crud.editCollection(vc);
        }
    }

    private void removeCollection(VirtualCollection c) {
        logger.trace("Removing collection with id = {} not implemented", originalCollectionId);
    }
}
