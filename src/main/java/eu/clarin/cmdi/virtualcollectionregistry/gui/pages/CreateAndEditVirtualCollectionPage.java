package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.AuthorsInput;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.CheckboxInput;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.CheckboxInputModel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.KeywordInput;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.ResourceInput;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Purpose;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Reproducibility;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Type;
import eu.clarin.cmdi.virtualcollectionregistry.service.CreatorProvider;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxPreventSubmitBehavior;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * References:
 *  Forms: https://ci.apache.org/projects/wicket/guide/6.x/guide/forms2.html#forms2_6
 * Nested forms: https://cwiki.apache.org/confluence/display/WICKET/Nested+Forms
 * @author wilelb
 */
@AuthorizeInstantiation(Roles.USER)
@SuppressWarnings("serial")
public class CreateAndEditVirtualCollectionPage extends BasePage {

    private static Logger logger = LoggerFactory.getLogger(CreateAndEditVirtualCollectionPage.class);
    
    protected final String DEFAULT_TOOLTIP_DATA_PLACEMENT = "right";
    
    protected VirtualCollection vc;
    protected boolean renderStateValid = false;
    protected boolean editMode = false;
    
    @SpringBean
    private VirtualCollectionRegistry vcr;
    
    @SpringBean
    private CreatorProvider creatorProvider;
    
    /**
     * Used by extenstions.
     * @throws eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException
     */
    public CreateAndEditVirtualCollectionPage() throws VirtualCollectionRegistryPermissionException {
        this(null, null);
    }

    /**
     * used when page constructed by framework
     * @param params
     * @throws eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException
     */
    public CreateAndEditVirtualCollectionPage(PageParameters params) throws VirtualCollectionRegistryException {
        final Long id = params.get("id").toLong();
        initializeWithCollection(vcr.retrieveVirtualCollection(id));        
    } 
   
    /**
     * 
     * @param vc
     * @param previousPage 
     * @throws eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException 
     */
    public CreateAndEditVirtualCollectionPage(VirtualCollection vc, final Page previousPage) throws VirtualCollectionRegistryPermissionException {
        initializeWithCollection(vc);
    }
    
    private void initializeWithCollection(VirtualCollection vc) throws VirtualCollectionRegistryPermissionException {
        if(vc != null) {
            checkAccess(vc);
            this.vc = vc;        
            this.editMode = true;
        } else {
            this.editMode = false;
        }
    }
    
    protected void addComponents() {
        final IModel<String> nameModel = vc == null || vc.getName().isEmpty() ? Model.of("") : Model.of(vc.getName());
        final IModel<CheckboxInputModel<Type>> typeModel = new Model(new CheckboxInputModel<>(Type.INTENSIONAL));
        final IModel<String> descriptionModel = Model.of("x");
        final IModel<CheckboxInputModel<Purpose>> purposeModel = new Model(new CheckboxInputModel(Purpose.REFERENCE));
        final IModel<CheckboxInputModel<Reproducibility>> reproducibilityModel = new Model(new CheckboxInputModel(Reproducibility.INTENDED));
        final Model<String> reproducibilityNoticeModel = Model.of("");
        final IModel<List<String>> keywordsModel = new ListModel<>(new ArrayList<>());
        final IModel<List<Creator>> authorsModel = new ListModel<>(new ArrayList<>());
        final IModel<List<Resource>> resourceModel = new ListModel<>(new ArrayList<>());
        
        String nameTooltip = "A short but descriptive name of the virtual collection for listings and views";
        String descriptionTooltip = "A prose description of this virtual collection";
        String typeTooltip = 
            "Type of Virtual Collection, either intensional or extensional<br /><br /> \n" +
            "<b>Extensional</b>: Type of virtual collection that explicitly enumerates the references to resources or metadata documents that make up the collection<br /><br /> \n" +
            "<b>Intensional</b>: Type of virtual collection that defines a query by which the items of the collection can be retrieved ad-hoc from a secondary service <em>(experimental, use at own risk!)</em>";
        String purposeTooltip = 
            "An indication of the intended usage of the present virtual collection<br /><br /> \n" +
            "<b>Research</b>: The virtual collection bundles resources that are relevant to a specific research (question).<br /><br /> \n" +
            "<b>Reference</b>: The virtual collection bundles resources, that are to be cited in a publication.<br /><br /> \n" +
            "<b>Sample</b>: This virtual collection bundles is intended to serve as an sample for research data<br /><br /> \n" +
            "<b>Future-use</b>: The purpose of this virtual collection is not specified yet. Used in published collection is advised against.";
        String reproducibilityTooltip = 
                "An indication of the degree to which results obtained from processing of the present collection can be expected to be stable<br /><br />\n" +
                "<b>Intended</b>: Processing results can be expected to remain stable<br /><br />\n" +
                "<b>Fluctuating</b>: Processing results may vary<br /><br />\n" +
                "<b>Untended</b>: No claims with respect to the stability of the processing results are made";
        String reproducibilityNoticeTooltip = "Optional note describing the expected reproducibility of processing results in more detail";
        String keywordsTooltip = "A set of words or short phrases, each signifying a salient facet of the present virtual collection";
        String authorsTooltip = "Add a new creator";
        String resourcesTooltip = "Add a single new resource or metadata reference with an optional label and/or description";
        
        
        Form form = new Form("form") {    
            @Override
            protected void onSubmit() {
                super.onSubmit();
                logger.info("Form successfully submitted!");
                String name = nameModel.getObject();
                Type type = null;
                if( typeModel.getObject() != null) {
                    type = typeModel.getObject().getObject();
                }                
                String description = descriptionModel.getObject();
                
                Purpose purpose = null;
                if(purposeModel.getObject() != null) {
                    purpose = purposeModel.getObject().getObject();
                }
                Reproducibility reproducibility = null;
                if(reproducibilityModel.getObject() != null) {
                    reproducibility = reproducibilityModel.getObject().getObject();
                }
                String repoducibilityNotice = reproducibilityNoticeModel.getObject();
                List<String> keywords = keywordsModel.getObject();
                List<Creator> creators = authorsModel.getObject();
                List<Resource> resources = resourceModel.getObject();
                
                final VirtualCollection vc = new VirtualCollection();
                vc.setName(name);
                vc.setType(type);
                vc.setDescription(description);                
                vc.setPurpose(purpose);
                vc.setReproducibility(reproducibility);
                vc.setReproducibilityNotice(repoducibilityNotice);
                if(editMode) {
                    vc.getKeywords().clear();
                    vc.getCreators().clear();
                    vc.getResources().clear();
                }
                vc.getKeywords().addAll(keywords);                
                vc.getCreators().addAll(creators);                
                vc.getResources().addAll(resources);
                
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
                
            }
            
            @Override
            protected void onError() {
                logger.info("Form failed to validate!");
            }
        };

        form.setOutputMarkupId(true);
        
        addTextinput(form, nameModel, "name", "Name", nameTooltip, true, false);

        final List<CheckboxInputModel<Type>> typeList = new ArrayList<>();
        for(Type t : Type.values()) {
            typeList.add(new CheckboxInputModel(t));
        }
        form.add(
            new CheckboxInput<>("type", typeModel, typeList, "Type", typeTooltip)
            .setRequired(true)            
        );
       
        addTextinput(form, descriptionModel, "description", "Description", descriptionTooltip, false, true);
        
        final List<CheckboxInputModel<Purpose>> purposeList = new ArrayList<>();
        for(Purpose t : Purpose.values()) {
            purposeList.add(new CheckboxInputModel(t));
        }
        form.add(
            new CheckboxInput<>("purpose", purposeModel, purposeList, "Purpose", purposeTooltip)
            .setRequired(false)

        );
        
        final List<CheckboxInputModel<Reproducibility>> reproducibilityeList = new ArrayList<>();
        for(Reproducibility t : Reproducibility.values()) {
            reproducibilityeList.add(new CheckboxInputModel(t));
        }
        form.add(
            new CheckboxInput<CheckboxInputModel<Reproducibility>>("reproducibility", reproducibilityModel, reproducibilityeList, "Reproducibility", reproducibilityTooltip)
            .setRequired(false)
        );
        addTextinput(form, reproducibilityNoticeModel, "reproducibility_notice", "Reproducibility Notice", reproducibilityNoticeTooltip, false, true);

        if (vc != null) {
            keywordsModel.getObject().addAll(vc.getKeywords());            
        }
        form.add(new KeywordInput("keywords", keywordsModel));//.setRequired(true));
       
        if(vc != null) {
            authorsModel.getObject().addAll(vc.getCreators());
        }        
        form.add(new AuthorsInput("authors", authorsModel));//.setRequired(true));
        
        if(vc != null) {
            resourceModel.getObject().addAll(vc.getResources());
        }  
        form.add(new ResourceInput("resources", resourceModel));//.setRequired(true));
        
        form.add(new AjaxPreventSubmitBehavior());
        
        Button submitButton = new Button("submit", Model.of("Create virtual collection"));
        if(this.editMode) {
            submitButton = new Button("submit", Model.of("Save virtual collection"));
        }
        form.add(submitButton);
        form.setDefaultButton(submitButton);
        
        add(form);
    }
    
    private Label getLabel(String id, String label, boolean required) {
        Label lbl = new Label(id, Model.of(label));
        lbl.add(new AttributeModifier("class", "required"));
        return lbl;
    }
    
    private void addTextinput(Form form, IModel model, String name, String label, String tooltipText, boolean required, boolean multiline) {
        final WebMarkupContainer tooltipComponent = new WebMarkupContainer("tt_"+name);
        tooltipComponent.add(new AttributeAppender("data-toggle", Model.of("tooltip")));
        tooltipComponent.add(new AttributeAppender("data-placement", Model.of(DEFAULT_TOOLTIP_DATA_PLACEMENT)));
        tooltipComponent.add(new AttributeAppender("data-html", Model.of("true")));
        tooltipComponent.add(new AttributeAppender("data-trigger", Model.of("focus")));
        tooltipComponent.add(new AttributeAppender("title", Model.of(tooltipText)));
        if( multiline ) {
            tooltipComponent.add(new TextArea(name, model).add(StringValidator.minimumLength(1)));
        } else if (!multiline && required ) {           
            tooltipComponent.add(new RequiredTextField(name, model).add(StringValidator.minimumLength(1)));
        } else {
            tooltipComponent.add(new TextField(name, model).add(StringValidator.minimumLength(1)));
        }
        
        form.add(getLabel("lbl_"+name, label, required));
        form.add(tooltipComponent);
        
    }
  
    @Override
    public void onBeforeRender() {
        super.onBeforeRender();
        if(!renderStateValid) {
            addComponents();
            renderStateValid = true;
        }
    }
    
    public void updateWithCollection(VirtualCollection vc) {
        if(vc != null) {
            this.vc = vc;
            this.editMode = true;
            this.renderStateValid = false;
            this.removeAll();
        }
    } 
    
    private void checkAccess(final VirtualCollection vc) throws VirtualCollectionRegistryPermissionException {
        // do not allow editing of VC's that are non-private or owned
        // by someone else! (except for admin)
        if (!isUserAdmin()
                && ( //only allow editing of private & public
                !(vc.getState() == VirtualCollection.State.PRIVATE || vc.getState() == VirtualCollection.State.PUBLIC)
                // only allow editing by the owner
                || !vc.getOwner().equalsPrincipal(getUser()))) {
            logger.warn("User {} attempts to edit virtual collection {} with state {} owned by {}", new Object[]{getUser().getName(), vc.getId(), vc.getState(), vc.getOwner().getName()});
            throw new UnauthorizedInstantiationException(CreateAndEditVirtualCollectionPage.class);
        }
    }    
} // class CreateVirtualCollecionPage
