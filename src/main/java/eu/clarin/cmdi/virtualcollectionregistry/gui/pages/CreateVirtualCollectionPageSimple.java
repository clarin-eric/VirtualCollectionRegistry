package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.service.CreatorProvider;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

@AuthorizeInstantiation(Roles.USER)
@SuppressWarnings("serial")
public class CreateVirtualCollectionPageSimple extends BasePage {

    //private final static ResourceReference TOOLTIP_ACTIVATE_JAVASCRIPT_REFERENCE = 
     //       new PackageResourceReference(CreateVirtualCollectionPage.class, "wizardhelp.js");
    
    @SpringBean
    private VirtualCollectionRegistry vcr;
    
    @SpringBean
    private CreatorProvider creatorProvider;
    
    /**
     * Used by extenstions.
     */
    public CreateVirtualCollectionPageSimple() {
        this(null, null);
    }

    /**
     * used when page constructed by framework
     * @param params
     */
    public CreateVirtualCollectionPageSimple(PageParameters params) {
        this(null, null);
    } 
   
    
    /**
     * 
     * @param vc
     * @param previousPage 
     */
    public CreateVirtualCollectionPageSimple(VirtualCollection vc, final Page previousPage) {
        //String hint = "A short but descriptive name of the virtual collection for listings and views";
        //add(new CustomFormTextComponent("text_name", "Name", "", hint));
        add(new Label("title", "Create virtual collection"));
    }
/*    
     @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));  
        response.render(JavaScriptHeaderItem.forReference(TOOLTIP_ACTIVATE_JAVASCRIPT_REFERENCE));
    }
  */  
} // class CreateVirtualCollecionPage
