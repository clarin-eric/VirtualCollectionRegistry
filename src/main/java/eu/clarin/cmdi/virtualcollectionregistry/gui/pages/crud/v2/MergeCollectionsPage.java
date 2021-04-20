package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BasePage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BrowsePublicCollectionsPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.ErrorPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v1.CreateAndEditVirtualCollectionPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission.SubmissionUtils;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.CollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.PrivateCollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
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

public class MergeCollectionsPage extends BasePage {

    private static Logger logger = LoggerFactory.getLogger(MergeCollectionsPage.class);

    @SpringBean
    private VirtualCollectionRegistry vcr;

    private VirtualCollection vc = null;
    private CollectionsProvider provider;

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
        ListView listview = new ListView("collections_list", this.provider.getList()) {
            long visibleCount = 0;

            protected void populateItem(ListItem item) {
                final VirtualCollection collection = (VirtualCollection) item.getModel().getObject();
                item.add(new Label("collection_title", collection.getTitle()));
                item.add(new Label("collection_created", collection.getDateCreated()));
                item.add(new Label("collection_modified", collection.getDateModified()));
                item.add(new Label("collection_resources_count", collection.getResources().size()));
                item.add(new Label("collection_type", collection.getType()));
                item.add(new Label("collection_state", collection.getState()));

                IModel<String> msgModel = Model.of("Cannot merge collection");
                Label msg = new Label("collection_message", msgModel);
                msg.setVisible(!collection.canMerge());
                item.add(msg);

                AjaxFallbackLink btnMerge = new AjaxFallbackLink("collection_btn_merge") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        //Store the collecion id of the collection the merge with in the session and redirect to the
                        //edit page
                        logger.debug("Storing merge collection id="+collection.getId()+" (title="+collection.getTitle()+")");
                        SubmissionUtils.storeCollectionMergeId(session, collection.getId());
                        throw new RestartResponseException(CreateAndEditVirtualCollectionPageV2.class);
                    }
                };


                btnMerge.setEnabled(collection.canMerge());
                item.add(btnMerge);

                boolean matchedTypes = collection.getType() == submitted_vc.getType();
                if(!matchedTypes) {
                    logger.info("Hiding item, types dont match");
                }
                item.setVisible(matchedTypes);
            }
        };
        listview.setVisible(visibleCollectionCount > 0);
        add(listview);

        add(new AjaxFallbackLink("btn_add_new") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                throw new RestartResponseException(CreateAndEditVirtualCollectionPageV2.class);
            }
        });

        add(new AjaxFallbackLink("btn_cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                SubmissionUtils.clearCollectionFromSession(session);
                throw new RestartResponseException(BrowsePublicCollectionsPage.class);
            }
        });
    }
}

