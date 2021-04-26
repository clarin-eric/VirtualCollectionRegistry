package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BasePage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BrowsePublicCollectionsPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.ErrorPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.CreateAndEditVirtualCollectionPageV2;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.CollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.PrivateCollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.List;

public class MergeCollectionsPage extends BasePage {

    private final static String MESSAGE_MERGE_NO_COLLECTION = "Please select a collection first.";
    private final static String MESSAGE_MERGE_WITH_COLLECTION = "Merging with collection: %s";

    private static Logger logger = LoggerFactory.getLogger(MergeCollectionsPage.class);

    @SpringBean
    private VirtualCollectionRegistry vcr;

    private VirtualCollection vc = null;
    private CollectionsProvider provider;

    private VirtualCollection selectedCollection = null;

    private final AjaxFallbackLink btnMergeWith;
    private final Label btnMergeWithMessage;
    private final Model btnMergeWithMessageModel;

    public MergeCollectionsPage(PageParameters params) throws VirtualCollectionRegistryException {
        this.setOutputMarkupId(true);
        final Component componentToUpdate = this;

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
        } else {
            logger.warn("No collection found in session");
            getSession().error(new ErrorPage.Error("Collection submission error", "No collection found in session"));
            throw new RestartResponseException(ErrorPage.class);
        }

        this.provider = new PrivateCollectionsProvider();

        //Submission summary
        add(new Label("submitted_title", submitted_vc.getTitle()));
        add(new Label("submitted_description", submitted_vc.getDescription()));
        add(new Label("submitted_resources", submitted_vc.getResources().size()));

        long collectionCount = 0;
        long visibleCollectionCount = 0;
        for(VirtualCollection c : this.provider.getList()) {
            collectionCount++;
            if(c.getType() == submitted_vc.getType()) {
                visibleCollectionCount++;
            }
        }

        final IModel lblModel = Model.of("No collections available.");
        if(visibleCollectionCount > 0) {
            lblModel.setObject("Available collections:");
        }
        add(new Label("collections_list_label", lblModel));

        final ApplicationSession session = getSession();
        final List<VirtualCollection> collectionsList = this.provider.getList();
        ListView listview = new ListView("collections_list", collectionsList) {
            protected void populateItem(ListItem item) {
                final VirtualCollection collection = (VirtualCollection) item.getModel().getObject();

                final WebMarkupContainer div = new WebMarkupContainer("selectable-collection");
                div.setOutputMarkupId(true);

                if (selectedCollection != null && selectedCollection.getId() == collection.getId()) {
                    //div.add(new AttributeAppender("class", "selected"));
                    div.add(new AttributeAppender("style", " background: lightgrey;"));
                    logger.info("Updated styles for selected row");
                }

                div.add(new AjaxEventBehavior("click") {
                    protected void onEvent(AjaxRequestTarget target) {
                        logger.info("Selected collection id=" + collection.getId() + ", title=" + collection.getTitle());
                        if (selectedCollection != null && selectedCollection.getId() == collection.getId()) {
                            selectedCollection = null;
                            logger.info("Cleared selected collection");
                        } else {
                            selectedCollection = collection;
                            logger.info("Updated selected collection");
                        }

                        if (target != null) {
                            target.add(componentToUpdate);
                        }
                    }
                });

                div.add(new Label("collection_title", collection.getTitle()));
                div.add(new Label("collection_created", collection.getDateCreated()));
                div.add(new Label("collection_modified", collection.getDateModified()));
                div.add(new Label("collection_resources_count", collection.getResources().size()));
                div.add(new Label("collection_type", collection.getType()));
                div.add(new Label("collection_state", collection.getState()));

                boolean matchedTypes = collection.getType() == submitted_vc.getType();
                if(!matchedTypes || !collection.canMerge()) {
                    logger.info("Hiding item, types dont match or cannot merge");
                }
                div.setVisible(matchedTypes);

                item.add(div);
            }
        };
        listview.setVisible(visibleCollectionCount > 0);
        add(listview);

        AjaxFallbackLink btnAddNew = new AjaxFallbackLink("btn_add_new") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                doAddNewCollection();
            }
        };
        btnAddNew.add(new Label("btn_add_new_label", Model.of("Add new collection")));
        add(btnAddNew);

        btnMergeWith = new AjaxFallbackLink("btn_merge_with") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                doMergeCollection(session);
            }
        };
        btnMergeWith.add(new Label("btn_merge_with_label", Model.of("Merge with selected collection")));
            add(btnMergeWith);

        btnMergeWithMessageModel = Model.of(MESSAGE_MERGE_NO_COLLECTION);
        btnMergeWithMessage = new Label("btn_merge_with_label_message", btnMergeWithMessageModel);
        add(btnMergeWithMessage);

        AjaxFallbackLink btnCancel = new AjaxFallbackLink("btn_cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                doCancel(session);
            }
        };
        btnCancel.add(new Label("btn_cancel_label", Model.of("Cancel submission")));
        add(btnCancel);
    }

    @Override
    protected void onBeforeRender() {
        btnMergeWith.setEnabled(selectedCollection != null);
        if(selectedCollection == null) {
            btnMergeWithMessageModel.setObject(MESSAGE_MERGE_NO_COLLECTION);
        } else {
            btnMergeWithMessageModel.setObject(String.format(MESSAGE_MERGE_WITH_COLLECTION, selectedCollection.getTitle()));
        }
        super.onBeforeRender();
    }

    private void doMergeCollection(ApplicationSession session) {
        if(selectedCollection == null) {
            logger.warn("Cannot merge collection: selectedCollection == null.");
            return;
        }

        logger.debug("Storing merge collection id="+selectedCollection.getId()+" (title="+selectedCollection.getTitle()+")");
        SubmissionUtils.storeCollectionMergeId(session, selectedCollection.getId());
        throw new RestartResponseException(CreateAndEditVirtualCollectionPageV2.class);
    }

    private void doAddNewCollection() {
        throw new RestartResponseException(CreateAndEditVirtualCollectionPageV2.class);
    }

    private void doCancel(ApplicationSession session) {
        SubmissionUtils.clearCollectionFromSession(session);
        throw new RestartResponseException(BrowsePublicCollectionsPage.class);
    }
}

