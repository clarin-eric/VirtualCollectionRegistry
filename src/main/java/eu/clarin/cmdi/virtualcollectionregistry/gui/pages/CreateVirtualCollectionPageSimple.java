package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.AuthorsInput;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.CheckboxInput;
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
public class CreateVirtualCollectionPageSimple extends BasePage {

    private static Logger logger = LoggerFactory.getLogger(CreateVirtualCollectionPageSimple.class);
    
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
        this.vc = vc;
        if(this.vc != null) {
            this.editMode = true;
        }
    }
    
    protected void addComponents() {
        final IModel<String> nameModel = vc == null || vc.getName().isEmpty() ? Model.of("") : Model.of(vc.getName());
        final IModel<Type> typeModel = new Model(Type.INTENSIONAL);        
        final IModel<String> descriptionModel = Model.of("x");
        final IModel<Purpose> purposeModel = new Model(Purpose.REFERENCE);
        final IModel<Reproducibility> reproducibilityModel = new Model(Reproducibility.INTENDED);        
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
                Type type = typeModel.getObject();
                String description = descriptionModel.getObject();
                Purpose purpose = purposeModel.getObject();
                Reproducibility reproducibility = reproducibilityModel.getObject();              
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

        form.add(
            new CheckboxInput<>("type", typeModel, Arrays.asList(Type.values()), "Type", typeTooltip)
            .setRequired(true)            
        );
       
        addTextinput(form, descriptionModel, "description", "Description", descriptionTooltip, false, true);
        form.add(
            new CheckboxInput<>("purpose", purposeModel, Arrays.asList(Purpose.values()), "Purpose", purposeTooltip)
            .setRequired(false)

        );
        form.add(
            new CheckboxInput<>("reproducibility", reproducibilityModel, Arrays.asList(Reproducibility.values()), "Reproducibility", reproducibilityTooltip)
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
        if(this.vc != null) {
            this.vc = vc;
            this.editMode = true;
            this.renderStateValid = false;
            this.removeAll();
        }
    } 
    
} // class CreateVirtualCollecionPage
