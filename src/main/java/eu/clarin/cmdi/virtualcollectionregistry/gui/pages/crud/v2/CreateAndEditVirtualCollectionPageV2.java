package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException;
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

    private final PrivateCollectionsManager provider;

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

    /**
     * Based on the supplied id either create a new collection (id = null or id does not return a collection)
     * or edit an existing collection.
     *
     * @param id
     * @param previousPage
     * @throws VirtualCollectionRegistryException
     */
    public CreateAndEditVirtualCollectionPageV2(Long id, final Page previousPage) throws VirtualCollectionRegistryException {
        logger.info("Collection id = {}", id);
        if(id != null) {
            vc = vcr.retrieveVirtualCollection(id);
            if (vc != null) {
                this.checkAccess(vc);
            }
        }


        if(vc == null) {
            VirtualCollection submitted_vc = SubmissionUtils.retrieveCollection(getSession());
            if(submitted_vc != null) {
                logger.info("Processing submitted collection. id="+submitted_vc.getId());
                vc = submitted_vc;
                //Check if any of the properties require updating
                if(vc.getOwner() == null) {
                    Principal p = getUser();
                    vc.setOwner(new User(p.getName()));
                    vc.getCreators().add(new Creator(p.getName(), ""));
                }

                //this.submissionMode = true;
            }
        }

        this.provider = new PrivateCollectionsManager();

        final WebMarkupContainer ajaxWrapper = new WebMarkupContainer("ajaxwrapper");
        final Label labelNoCollections = new Label("lbl_no_collections", "No collections");
        ajaxWrapper.setOutputMarkupId(true);

        modal = new ModalConfirmDialog("modal");
        modal.addListener(new Listener() {
            @Override
            public void handleEvent(final Event event) {
                switch(event.getType()) {
                    case OK:
                        logger.info("Default confirm");
                        event.updateTarget(ajaxWrapper);
                        break;
                    case CONFIRMED_DELETE:
                        if(event.getData() == null) {
                            logger.info("No collection found for removal");
                        } else {
                            removeCollection((VirtualCollection)event.getData());
                            labelNoCollections.setVisible(provider.isEmpty());
                        }
                        event.updateTarget(ajaxWrapper);
                        break;
                    case CANCEL:
                        event.updateTarget();
                        break;
                }
            }
        });
        add(modal);

        final CreateAndEditPanel crud = new CreateAndEditPanel("create_and_edit_panel", modal);

        crud.addListener(new Listener<VirtualCollection>() {
            @Override
            public void handleEvent(Event<VirtualCollection> event) {
                switch(event.getType()) {
                    case SAVE:
                        logger.trace("Saving collection");
                        //Search or exising collection
                        int idx = -1;
                        Long id = event.getData().getId();
                        if(id != null) {
                            for (int i = 0; i < provider.size(); i++) {
                                Long listId = provider.get(i).getId();
                                if (listId.longValue() == id.longValue()) {
                                    idx = i;
                                }
                            }
                        }
                        //Update or insert
                        try {
                            if (idx >= 0) {
                                provider.set(idx, event.getData(), event.getPrincipal()); //Update collection
                            } else {
                                provider.add(event.getData(), event.getPrincipal()); //New collection
                            }
                        } catch(VirtualCollectionRegistryException ex) {
                            logger.error("Failed to persist collect. Error: {}", ex.toString());
                        }
                        labelNoCollections.setVisible(provider.isEmpty());

                        throw new RestartResponseException(BrowsePrivateCollectionsPage.class);
                    case CANCEL:
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

        ListView listview = new ListView("listview", provider.getList()) {
            @Override
            protected void populateItem(ListItem item) {
                final EmptyPanel pnl = new EmptyPanel("pnl_collection");
                item.add(pnl);
            }
        };
        ajaxWrapper.add(labelNoCollections);
        labelNoCollections.setVisible(provider.isEmpty());
        ajaxWrapper.add(listview);

        final WebMarkupContainer wrapper = new WebMarkupContainer("wrapper");
        wrapper.add(ajaxWrapper);
        wrapper.setVisible(false);
        add(wrapper);
    }

    private void removeCollection(VirtualCollection c) {
        //Search index for collection to remove
        int idxToRemove = -1;
        Long id = c.getId();
        logger.trace("Removing collection with id = {}",id);
        for(int i = 0; i < provider.size(); i++) {
            Long listId = provider.get(i).getId();
            if(listId == id) {
                idxToRemove = i;
            }
        }
        //Remove collection
        if(idxToRemove >= 0) {
            provider.remove(idxToRemove);
        } else {
            logger.warn("Tried to remove but could not find collection with id={}", id);
        }
    }

    private void checkAccess(final VirtualCollection vc) throws VirtualCollectionRegistryPermissionException {
        // do not allow editing of VC's that are non-private or owned
        // by someone else! (except for admin)
        if (!isUserAdmin()
                && ( //only allow editing of private & public
                !(vc.getState() == VirtualCollection.State.PRIVATE || vc.getState() == VirtualCollection.State.PUBLIC)
                        // only allow editing by the owner
                        || vc.getOwner() == null || !(vc.getOwner().equalsPrincipal(getUser()) || vc.getOwner().getName().equalsIgnoreCase("anonymous")) )) {
            logger.warn("User {} attempts to edit virtual collection {} with state {} owned by {}", new Object[]{getUser().getName(), vc.getId(), vc.getState(), vc.getOwner().getName()});
            throw new UnauthorizedInstantiationException(CreateAndEditVirtualCollectionPage.class);
        }
    }
}
