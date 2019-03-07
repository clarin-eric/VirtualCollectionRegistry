package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionValidationException;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.CreatorValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.KeywordValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.NameValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.QueryValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.ReproducibilityNoticeValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.ResourceValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.feedback.TypeValidationFailedMessage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.ApplicationSession;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.AuthorsInput;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.CheckboxInput;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.CheckboxInputChangeListener;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.CollectionQuery;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.KeywordInput;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.QueryInput;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.forms.ResourceInput;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission.SubmissionUtils;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Purpose;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Reproducibility;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Type;
import eu.clarin.cmdi.virtualcollectionregistry.service.CreatorProvider;
import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxPreventSubmitBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.flow.RedirectToUrlException;
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
    
    public final static String DEFAULT_TOOLTIP_DATA_PLACEMENT = "bottom";
    
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
        
    private final IModel<String> nameModel = Model.of("");
    private final IModel<Type> typeModel = new Model(Type.EXTENSIONAL);
    private final IModel<String> descriptionModel = Model.of("");
    private final IModel<Purpose> purposeModel = new Model(Purpose.REFERENCE);
    private final IModel<Reproducibility> reproducibilityModel = new Model(Reproducibility.INTENDED);      
    private final Model<String> reproducibilityNoticeModel = Model.of("");
    private final IModel<List<String>> keywordsModel = new ListModel<>(new ArrayList<>());
    private final IModel<List<Creator>> authorsModel = new ListModel<>(new ArrayList<>());
    private final IModel<List<Resource>> resourceModel = new ListModel<>(new ArrayList<>()); 
    private final IModel<CollectionQuery> queryModel = new Model<>(new CollectionQuery());
    
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
        if(vc == null) {
            VirtualCollection submitted_vc = SubmissionUtils.retrieveCollection(getSession());
            if(submitted_vc != null) {
                logger.info("Processing submitted collection");
                vc = submitted_vc;
            }
        }
        initializeWithCollection(vc);
    }
    
    private void initializeWithCollection(VirtualCollection vc) throws VirtualCollectionRegistryPermissionException {
        /*
        Principal userPrincipal = getUser();        
        Set<Creator> creators = vcr.getCreatorService().getCreators(userPrincipal.getName());
        
        logger.info("Creators:");
        for(Creator c : creators) {
            logger.info("\t{}, {}", c.getPerson(), c.getEMail());
        }
        */        
        if(vc != null) {
            checkAccess(vc);
            this.vc = vc;        
            this.editMode = true;
        } else {
            this.editMode = false;
            //this.authorsModel.getObject().add(new Creator("test"));
        }
        
    }
    
    private class FormBuilder implements Serializable {
        private final Form form;
        
        private ResourceInput resourceInput;
        private QueryInput queryInput;
                
        public FormBuilder(String id) {
            this.form = new Form("form") {    
                @Override
                protected void onSubmit() {
                    super.onSubmit();
                    logger.debug("Virtual collection form submit!");
                    persist();                
                }

                @Override
                protected void onError() {
                    logger.info("Virtual collection form failed to validate!");
                }

                @Override
                protected boolean wantSubmitOnNestedFormSubmit() {
                    return false; //Make parent form submit if a nested form is submitted
                }
            };
            this.form.setOutputMarkupId(true);
            this.form.add(new AjaxPreventSubmitBehavior());
        }
        
        protected FormBuilder addComponent(String id, IModel model) {
            return this;
        }
        
        protected FormBuilder addCheckboxInputGroup(String id, IModel model, List values, String label, String tooltip ) {
            return addCheckboxInputGroup(id, model, values, label, tooltip, null);
        }
        
        protected FormBuilder addCheckboxInputGroup(String id, IModel model, List values, String label, String tooltip, IFeedbackMessageFilter filter) {
            CheckboxInput input = new CheckboxInput<>(id, model, values, label, tooltip, "accordion", DEFAULT_TOOLTIP_DATA_PLACEMENT);
            if(id.equalsIgnoreCase("type")) {
                input.setCheckboxInputChangeListener(new CheckboxInputChangeListener() {
                    @Override
                    public void handleEvent(AjaxRequestTarget art, IModel model) {
                        String val = model.getObject().toString();
                        resourceInput.setVisible(val.equalsIgnoreCase("extensional"));
                        queryInput.setVisible(val.equalsIgnoreCase("intensional"));
                        art.add(resourceInput);
                        art.add(queryInput);
                    }
                }); 
            }
            this.form.add(input);
            if(id.equalsIgnoreCase("type")) {
                FeedbackPanel feedback = new FeedbackPanel("feedback_"+id);
                feedback.setFilter(filter);
                this.form.add(feedback);
            }
            return this;
        }
        
        protected FormBuilder addTextinput(IModel model, String name, String label, String tooltipText, boolean required, boolean multiline, IFeedbackMessageFilter filter) {
            final WebMarkupContainer tooltipComponent = new WebMarkupContainer("tt_"+name);
            UIUtils.addTooltip(tooltipComponent, tooltipText, "accordion", DEFAULT_TOOLTIP_DATA_PLACEMENT);       
            if( multiline ) {
                tooltipComponent.add(new TextArea(name, model).add(StringValidator.minimumLength(1)));
            } else if (!multiline && required ) {           
                tooltipComponent.add(new RequiredTextField(name, model).add(StringValidator.minimumLength(1)));
            } else {
                tooltipComponent.add(new TextField(name, model).add(StringValidator.minimumLength(1)));
            }
        
            this.form.add(getLabel("lbl_"+name, label, required));
            this.form.add(tooltipComponent);
            
            FeedbackPanel feedback = new FeedbackPanel("feedback_"+name);
            feedback.setFilter(filter);
            this.form.add(feedback);
            
            return this;
        }
        
        protected FormBuilder addSubmitButton(String id, IModel model) {
            Button submitButton = new Button(id, model);
            this.form.add(submitButton);
            this.form.setDefaultButton(submitButton);
            return this;
        }
        
        protected FormBuilder addKeywordInput(String id, IModel model) {
            KeywordInput input = new KeywordInput(id, model);
            this.form.add(input);
            FeedbackPanel feedback = new FeedbackPanel("feedback_"+id);
            feedback.setFilter((FeedbackMessage fm) -> fm.getMessage() instanceof KeywordValidationFailedMessage);
            this.form.add(feedback);
            return this;
        }
        
        protected FormBuilder addAuthorsInput(String id, IModel model) {
            this.form.add(new AuthorsInput(id, model));
            FeedbackPanel feedback = new FeedbackPanel("feedback_"+id);
            feedback.setFilter((FeedbackMessage fm) -> fm.getMessage() instanceof CreatorValidationFailedMessage);
            this.form.add(feedback);
            return this;
        }
        
        protected FormBuilder addResourceInput(String id, IModel model) {
            resourceInput = new ResourceInput(id, model);
            resourceInput.setOutputMarkupId(true);
            resourceInput.setOutputMarkupPlaceholderTag(true);
            this.form.add(resourceInput);
            FeedbackPanel feedback = new FeedbackPanel("feedback_"+id);
            feedback.setFilter((FeedbackMessage fm) -> fm.getMessage() instanceof ResourceValidationFailedMessage);
            this.form.add(feedback);
            return this;
        }
        
         protected FormBuilder addQueryInput(String id, IModel model) {
            queryInput = new QueryInput(id, model);
            queryInput.setVisible(false); //Todo: sync this type checkbox value
            queryInput.setOutputMarkupId(true);        
            queryInput.setOutputMarkupPlaceholderTag(true);
            this.form.add(queryInput);
            FeedbackPanel feedback = new FeedbackPanel("feedback_"+id);
            feedback.setFilter((FeedbackMessage fm) -> fm.getMessage() instanceof QueryValidationFailedMessage);
            this.form.add(feedback);
            return this;
        }
         
        protected Form build() {
            return this.form;
        }
        
        private Label getLabel(String id, String label, boolean required) {
            Label lbl = new Label(id, Model.of(label));
            lbl.add(new AttributeModifier("class", "required"));
            return lbl;
        }
    }
    
    protected void addComponents() {     
        //Add existing values to models if we are editing an existing collection
        if(vc != null && !vc.getName().isEmpty()) {
            nameModel.setObject(vc.getName());
            typeModel.setObject(vc.getType());
            descriptionModel.setObject(vc.getDescription());
            purposeModel.setObject(vc.getPurpose());
            reproducibilityModel.setObject(vc.getReproducibility());
            reproducibilityNoticeModel.setObject(vc.getReproducibilityNotice());
            keywordsModel.setObject(vc.getKeywords());
            authorsModel.setObject(vc.getCreators());
            if(vc.getResources() != null) {
                resourceModel.setObject(vc.getResources());
            }
            if(vc.getGeneratedBy() != null) {
                queryModel.setObject(CollectionQuery.fromGeneratedBy(vc.getGeneratedBy()));
            }
        }
        
        Model submitModel = Model.of(this.editMode ? "Save virtual collection" : "Create virtual collection");
        Form form = 
            new FormBuilder("form")
                .addTextinput(nameModel, "name", "Name:", nameTooltip, true, false, (FeedbackMessage fm) -> fm.getMessage() instanceof NameValidationFailedMessage)
                .addCheckboxInputGroup("type", typeModel, Arrays.asList(Type.values()), "Type:", typeTooltip, (FeedbackMessage fm) -> fm.getMessage() instanceof TypeValidationFailedMessage)
                .addTextinput(descriptionModel, "description", "Description:", descriptionTooltip, true, true, (FeedbackMessage fm) -> false)
                .addCheckboxInputGroup("purpose", purposeModel, Arrays.asList(Purpose.values()), "Purpose:", purposeTooltip)
                .addCheckboxInputGroup("reproducibility", reproducibilityModel, Arrays.asList(Reproducibility.values()), "Reproducibility:", reproducibilityTooltip)
                .addTextinput(reproducibilityNoticeModel, "reproducibility_notice", "Reproducibility Notice:", reproducibilityNoticeTooltip, false, true, (FeedbackMessage fm) -> fm.getMessage() instanceof ReproducibilityNoticeValidationFailedMessage)
                .addKeywordInput("keywords", keywordsModel)
                .addAuthorsInput("authors", authorsModel)
                .addResourceInput("resources", resourceModel)
                .addQueryInput("query", queryModel)
                .addSubmitButton("submit", submitModel)
                .build();
        
        add(form);
    }
    
    private void persist() {
        logger.debug("Persist");
        
        String name = nameModel.getObject();        
        String description = descriptionModel.getObject();

        Purpose purpose = null;
        if(purposeModel.getObject() != null) {
            purpose = purposeModel.getObject();
        }
        Reproducibility reproducibility = null;
        if(reproducibilityModel.getObject() != null) {
            reproducibility = reproducibilityModel.getObject();
        }
        String repoducibilityNotice = reproducibilityNoticeModel.getObject();
        List<String> keywords =  (ArrayList)(new ArrayList(keywordsModel.getObject()).clone());
        List<Creator> creators =  (ArrayList)(new ArrayList(authorsModel.getObject()).clone());
        List<Resource> resources = (ArrayList)(new ArrayList(resourceModel.getObject()).clone());
        
        VirtualCollection new_vc = new VirtualCollection();
        if(this.vc != null) {
            new_vc = this.vc;
        }
        new_vc.setName(name);
        new_vc.setDescription(description);                
        new_vc.setPurpose(purpose);
        new_vc.setReproducibility(reproducibility);
        new_vc.setReproducibilityNotice(repoducibilityNotice);
        
        if(editMode) {
            new_vc.getKeywords().clear();
            new_vc.getCreators().clear();
            new_vc.getResources().clear();
        }
        
        new_vc.getKeywords().addAll(keywords);                
        new_vc.getCreators().addAll(creators);                
        
        Type type = null;
        if( typeModel.getObject() != null) {
            type = typeModel.getObject();
        }                
        new_vc.setType(type);
        //Set extensional or intensional values based on collection type
        if (type == Type.EXTENSIONAL) {            
            new_vc.getResources().addAll(resources);
            new_vc.setGeneratedBy(null);
        } else if( type == Type.INTENSIONAL) {
            new_vc.setGeneratedBy(queryModel.getObject().convertToGeneratedBy());
            //new_vc.getResources().clear(); //TODO: why is this not checked in the validator
        }

        try {
            ApplicationSession session = (ApplicationSession) getSession();
            Principal principal = session.getPrincipal();
            if (principal == null) {                
                throw new WicketRuntimeException("principal == null"); // XXX: security issue?
            }
            
            if (new_vc.getId() == null) {
                logger.debug("Creating new virtual collection");                
                new_vc.setCreationDate(new Date()); // FIXME: get date from GUI?
                vcr.createVirtualCollection(principal, new_vc);
            } else {
                logger.debug("Updating existing virtual collection with id: {}", new_vc.getId());
                vcr.updateVirtualCollection(principal, new_vc.getId(), new_vc);
            }
            
            //TODO: dynamically fetch context path
            throw new RedirectToUrlException("/app/private");
        } catch (VirtualCollectionValidationException e) {
            e.addAllErrorsToSession(getSession());
        } catch(VirtualCollectionRegistryException e) {
            getSession().error(e.getMessage());
        }
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
                || !(vc.getOwner().equalsPrincipal(getUser()) || vc.getOwner().getName().equalsIgnoreCase("anonymous")) )) {
            logger.warn("User {} attempts to edit virtual collection {} with state {} owned by {}", new Object[]{getUser().getName(), vc.getId(), vc.getState(), vc.getOwner().getName()});
            throw new UnauthorizedInstantiationException(CreateAndEditVirtualCollectionPage.class);
        }
    }    
} // class CreateVirtualCollecionPage
