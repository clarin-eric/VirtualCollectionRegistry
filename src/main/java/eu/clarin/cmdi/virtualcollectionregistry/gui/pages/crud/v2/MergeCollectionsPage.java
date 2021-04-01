package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BasePage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v1.CreateAndEditVirtualCollectionPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission.SubmissionUtils;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.CollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.PrivateCollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.markup.html.WebMarkupContainer;
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
        //this.provider = new PrivateCollectionsManager();

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
        }

        this.provider = new PrivateCollectionsProvider();

        add(new WebMarkupContainer("btn_add_new"));
        add(new WebMarkupContainer("btn_merge"));
    }
}

