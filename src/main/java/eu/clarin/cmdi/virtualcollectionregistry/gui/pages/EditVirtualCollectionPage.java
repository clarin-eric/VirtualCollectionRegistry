package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.wizard.CreateVirtualCollectionWizard;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.State;
import org.apache.wicket.Page;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation(Roles.USER)
public class EditVirtualCollectionPage extends CreateVirtualCollectionPage {

    @SpringBean
    private VirtualCollectionRegistry vcr;

    private final static Logger logger = LoggerFactory.getLogger(EditVirtualCollectionPage.class);

    public EditVirtualCollectionPage(VirtualCollection vc, Page page) {
        super(vc, page);
    }
    
    public EditVirtualCollectionPage(PageParameters params) throws VirtualCollectionRegistryException {
        super(params);
        final Long id = params.get("id").toLong();
        final VirtualCollection vc = vcr.retrieveVirtualCollection(id);
        checkAccess(vc);
        super.updateWizardModelWithCollection(vc); 
    }

    private void checkAccess(final VirtualCollection vc) throws VirtualCollectionRegistryPermissionException {
        // do not allow editing of VC's that are non-private or owned
        // by someone else! (except for admin)
        if (!isUserAdmin()
                && ( //only allow editing of private & public
                !(vc.getState() == State.PRIVATE || vc.getState() == State.PUBLIC)
                // only allow editing by the owner
                || !vc.getOwner().equalsPrincipal(getUser()))) {
            logger.warn("User {} attempts to edit virtual collection {} with state {} owned by {}", new Object[]{getUser().getName(), vc.getId(), vc.getState(), vc.getOwner().getName()});
            throw new UnauthorizedInstantiationException(EditVirtualCollectionPage.class);
        }
    }

    
}
