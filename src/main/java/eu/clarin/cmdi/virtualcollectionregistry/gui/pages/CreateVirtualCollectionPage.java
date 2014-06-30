package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import java.security.Principal;
import java.util.Date;

import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.wizard.CreateVirtualCollectionWizard;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

@AuthorizeInstantiation(Roles.USER)
@SuppressWarnings("serial")
public class CreateVirtualCollectionPage extends BasePage {

    @SpringBean
    private VirtualCollectionRegistry vcr;

    // only for extensions
    protected CreateVirtualCollectionPage() {
    }

    // used when page constructed by framework
    public CreateVirtualCollectionPage(PageParameters params) {
        //ignore any params
        this(new VirtualCollection(), null);
    }

    public CreateVirtualCollectionPage(VirtualCollection vc, final Page previousPage) {
        final CreateVirtualCollectionWizard wizard = createWizard(vc, previousPage);
        add(wizard);
    }

    protected final CreateVirtualCollectionWizard createWizard(VirtualCollection vc, final Page previousPage) {
        return new CreateVirtualCollectionWizard("wizard", vc) {

            @Override
            protected void onCancelWizard() {
                // proceed to response page
                if (previousPage != null) {
                    setResponsePage(previousPage);
                } else {
                    setResponsePage(BrowsePrivateCollectionsPage.class);
                }
            }

            @Override
            protected void onFinishWizard(VirtualCollection vc) {
                try {
                    ApplicationSession session
                            = (ApplicationSession) getSession();
                    Principal principal = session.getPrincipal();
                    if (principal == null) {
                        // XXX: security issue?
                        throw new WicketRuntimeException("principal == null");

                    }
                    // FIXME: get date from GUI?
                    if (vc.getId() == null) {
                        vc.setCreationDate(new Date());
                        vcr.createVirtualCollection(principal, vc);
                    } else {
                        vcr.updateVirtualCollection(principal, vc.getId(), vc);
                    }
                } catch (VirtualCollectionRegistryException e) {
                    // FIXME: handle error
                    e.printStackTrace();
                }

                // proceed to response page
                if (previousPage != null) {
                    setResponsePage(previousPage);
                } else {
                    setResponsePage(BrowsePrivateCollectionsPage.class);
                }
            }
        };
    }

} // class CreateVirtualCollecionPage
