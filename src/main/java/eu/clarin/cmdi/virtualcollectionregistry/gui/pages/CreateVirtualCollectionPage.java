package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.VolatileEntityModel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.wizard.CreateVirtualCollectionWizard;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.CreatorProvider;
import java.security.Principal;
import java.util.Date;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

@AuthorizeInstantiation(Roles.USER)
@SuppressWarnings("serial")
public class CreateVirtualCollectionPage extends BasePage {

    @SpringBean
    private VirtualCollectionRegistry vcr;
    
    @SpringBean
    private CreatorProvider creatorProvider;

    // only for extensions
    //protected CreateVirtualCollectionPage() {
    //}

    // used when page constructed by framework
    public CreateVirtualCollectionPage(PageParameters params) {
        //ignore any params
        this(null, null);
    }

    public CreateVirtualCollectionPage(VirtualCollection vc, final Page previousPage) {
        if(vc == null) {
            VirtualCollection defaultVc = new VirtualCollection();
            defaultVc.setType(VirtualCollection.Type.EXTENSIONAL);
            defaultVc.setPurpose(VirtualCollection.Purpose.RESEARCH);
            defaultVc.setReproducibility(VirtualCollection.Reproducibility.INTENDED);
            
            final Creator creator = creatorProvider.getCreator(ApplicationSession.get().getPrincipal());
            if (creator.getPerson() != null) {
                defaultVc.getCreators().add(creator);
            }
            vc = defaultVc;
        }
        final CreateVirtualCollectionWizard wizard = createWizard(vc, previousPage);
        add(wizard);
    }

    protected final CreateVirtualCollectionWizard createWizard(VirtualCollection vc, final Page previousPage) {
        return new CreateVirtualCollectionWizard("wizard", new VolatileEntityModel(vc)) {

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
            protected void onFinishWizard(IModel<VirtualCollection> vcModel) {
                final VirtualCollection vc = vcModel.getObject();
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
                    getSession().error(e.getMessage());
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
