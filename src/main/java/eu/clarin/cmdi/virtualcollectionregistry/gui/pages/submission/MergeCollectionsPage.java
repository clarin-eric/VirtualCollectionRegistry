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
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebComponent;
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
import java.util.*;

public class MergeCollectionsPage extends BasePage {

    private final static String MESSAGE_MERGE_NO_COLLECTION = "Please select a collection first.";
    private final static String MESSAGE_MERGE_WITH_COLLECTION = "Merging with collection: %s";
    private final static String LABEL_NO_PRIVATE_COLLECTIONS_AVAILABLE = "No private collections available.";
    private final static String LABEL_AVAILABLE_COLLECTIONS = "Available collections:";


    private final Properties labels = new Properties();

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
        labels.put("heading_submitted_collection", "Submitted Collection");
        labels.put("heading_actions", "Actions");
        labels.put("heading_merge_collection", "Merge Collection");
        labels.put("lbl_submitted_title", "Title:");
        labels.put("lbl_submitted_desc", "Description:");
        labels.put("lbl_submitted_resources", "#Resources:");
        labels.put("lbl_collection_created", "Created at:");
        labels.put("lbl_collection_modified", "Modified at:");
        labels.put("lbl_collection_resources_count", "#Resources:");
        labels.put("lbl_collection_type", "Type:");
        labels.put("lbl_collection_state", "State:");
        labels.put("err_submission", "Collection submission error");
        labels.put("err_no_collection_in_session", "No collection found in session");
        labels.put("btn_add_new_label", "Add as new Collection");
        labels.put("btn_merge_with_label", "Merge with selected Collection");
        labels.put("btn_cancel_label", "Cancel submission");


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
            getSession().error(new ErrorPage.Error(labels.getProperty("err_submission"), labels.getProperty("err_no_collection_in_session")));
            throw new RestartResponseException(ErrorPage.class);
        }

        this.provider = new PrivateCollectionsProvider();

        addLabel(this, "heading_submitted_collection");
        addLabel(this, "heading_actions");
        addLabel(this, "heading_merge_collection");

        addLabel(this, "lbl_submitted_title");
        addLabel(this, "lbl_submitted_desc");
        addLabel(this, "lbl_submitted_resources");

        //Submission summary
        addField(this, "submitted_title", submitted_vc.getTitle());
        addField(this, "submitted_description", submitted_vc.getDescription());
        addField(this, "submitted_resources", submitted_vc.getResources().size());

        long collectionCount = 0;
        long visibleCollectionCount = 0;
        for(VirtualCollection c : this.provider.getList()) {
            collectionCount++;
            if(c.getType() == submitted_vc.getType()) {
                visibleCollectionCount++;
            }
        }

        final IModel lblModel = Model.of(LABEL_NO_PRIVATE_COLLECTIONS_AVAILABLE);
        if(visibleCollectionCount > 0) {
            lblModel.setObject(LABEL_AVAILABLE_COLLECTIONS);
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
                addFieldWithLabel(div, "collection_created",  collection.getDateCreated());
                addFieldWithLabel(div, "collection_modified", collection.getDateModified());
                addFieldWithLabel(div, "collection_resources_count", collection.getResources().size());
                addFieldWithLabel(div, "collection_type", collection.getType().toString());
                addFieldWithLabel(div, "collection_state", collection.getState().toString());

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
        addLabel(btnAddNew, "btn_add_new_label");
        add(btnAddNew);

        btnMergeWith = new AjaxFallbackLink("btn_merge_with") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                doMergeCollection(session);
            }
        };
        addLabel(btnMergeWith, "btn_merge_with_label");
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
        addLabel(btnCancel, "btn_cancel_label");
        add(btnCancel);
    }

    private void addFieldWithLabel(MarkupContainer c, String id, int value) {
        addFieldWithLabel(c, id, String.valueOf(value));
    }

    private void addFieldWithLabel(MarkupContainer c, String id, Date value) {
        addFieldWithLabel(c, id, value.toString());
    }

    private void addFieldWithLabel(MarkupContainer c, String id, String value) {
        String lbl = labels.getProperty("lbl_"+id);
        c.add(new Label("lbl_"+id, Model.of(lbl)));
        c.add(new Label(id, value));
    }

    private void addField(MarkupContainer c, String id, int value) {
        addField(c, id, String.valueOf(value));
    }

    private void addField(MarkupContainer c, String id, String value) {
        c.add(new Label(id, Model.of(value)));
    }

    private void addLabel(MarkupContainer c, String id) {
        String lbl = labels.getProperty(id);
        c.add(new Label(id, Model.of(lbl)));
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

