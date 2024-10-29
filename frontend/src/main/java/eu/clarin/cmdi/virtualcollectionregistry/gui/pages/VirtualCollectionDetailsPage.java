package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.*;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin.AdminPage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.CreateAndEditVirtualCollectionPageV2;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission.SubmissionUtils;
import eu.clarin.cmdi.virtualcollectionregistry.gui.panels.versions.VersionsDropDownPanel;
import eu.clarin.cmdi.virtualcollectionregistry.wicket.DetailsStructuredMeatadataHeaderBehavior;
import eu.clarin.cmdi.wicket.components.CMDIExplorerLink;
import eu.clarin.cmdi.wicket.components.LanguageResourceSwitchboardLink;
import eu.clarin.cmdi.wicket.components.citation.CitationPanelFactory;
import eu.clarin.cmdi.wicket.components.panel.BootstrapDropdown;
import eu.clarin.cmdi.wicket.components.panel.BootstrapDropdown.DropdownMenuItem;
import eu.clarin.cmdi.wicket.components.panel.BootstrapPanelBuilder;
import eu.clarin.cmdi.wicket.components.pid.PidPanel;
import java.io.Serializable;
import java.sql.Date;
import java.util.*;
import org.apache.wicket.Component;
import org.apache.wicket.PageReference;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.authorization.UnauthorizedActionException;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.OddEvenListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.INamedParameters.NamedPair;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionFactory;
import eu.clarin.cmdi.virtualcollectionregistry.rest.utils.RestUtils;
import eu.clarin.cmdi.virtualcollectionregistry.model.api.exception.VirtualCollectionRegistryPermissionException;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection.Type;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfigImpl;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifieable;
import eu.clarin.cmdi.virtualcollectionregistry.model.pid.PersistentIdentifier;
import jakarta.servlet.http.HttpServletRequest;

@SuppressWarnings("serial")
public class VirtualCollectionDetailsPage extends BasePage {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionDetailsPage.class);
    
    public static final String PARAM_VC_ID = "collection-id";
    public static final String PARAM_BACK_PAGE = "backPage";
    //private static final String CSS_CLASS = "collectionDetails";
    private static final IConverter convDate = new DateConverter();
    private final HideIfEmptyBehavior hideIfEmpty = new HideIfEmptyBehavior();
    private final HideIfShowAdvancedDisabled hideIfShowAdvancedDisabled = new HideIfShowAdvancedDisabled();

    private final PageParameters params;

    private IModel<Boolean> showAdvancedFields = Model.of(false);

    @SpringBean
    private VcrConfigImpl vcrConfig;

    private User owner = null;

    private static final IConverter convEnum = new IConverter() {
        @Override
        public String convertToString(Object o, Locale locale) {
            final Enum<?> enumObj = (Enum<?>) o;
            final String key
                    = enumObj.getDeclaringClass().getSimpleName() + "."
                    + enumObj.name();
            final String value = Application.get().getResourceSettings()
                    .getLocalizer().getString(key, null);
            return Strings.escapeMarkup(value).toString();
        }

        @Override
        public Object convertToObject(String s, Locale locale) {
            return null;
        }
    };

    /*
     * Actually, we really want the behavior to hide the component on
     * the beforeRender() call, but we always get an exception from Wicket
     * that we are not supposed to change the page hierarchy anymore. This
     * class is a hack to avoid this exception.
     */
    private static final class HideIfEmptyBehavior extends Behavior {

        private final List<Component> components = new LinkedList<Component>();

        @Override
        public void bind(Component component) {
            super.bind(component);
            components.add(component);
        }

        public void hideEmptyComponents() {
            for (Component component : components) {
                Object obj = component.getDefaultModelObject();
                if (obj == null) {
                    component.setVisible(false);
                } else {
                    if (obj instanceof Collection<?>) {
                        if (((Collection<?>) obj).isEmpty()) {
                            component.setVisible(false);
                        }
                    }
                }
            }
        }
    } // class VirtualCollectionDetailsPage.HideIfEmptyBehavior

    private static final class HideIfShowAdvancedDisabled extends Behavior {

        private final List<Component> components = new LinkedList<Component>();

        @Override
        public void bind(Component component) {
            super.bind(component);
            components.add(component);
        }

        public void hideIfShowAdvancedDisabled(boolean showAdvanced) {
            for (Component component : components) {
                component.setVisible(showAdvanced);
            }
        }
    }

    private static class CustomLabel<C> extends Label {

        public CustomLabel(String id) {
            super(id);
        }

        public CustomLabel(String id, IModel model) {
            super(id, model);
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public <C> IConverter<C> getConverter(Class<C> type) {
            if (VirtualCollection.Type.class.isAssignableFrom(type)
                    || VirtualCollection.Purpose.class.isAssignableFrom(type)
                    || VirtualCollection.Reproducibility.class.isAssignableFrom(type)) {
                return convEnum;
            }
            if (Date.class.isAssignableFrom(type)) {
                return convDate;
            }
            return super.getConverter(type);
        }
    } // class VirtualCollectionDetailsPage.TypeLabel
    
    public VirtualCollectionDetailsPage(PageParameters params) throws VirtualCollectionRegistryPermissionException {
        this(getVirtualCollectionModel(params), params);
    }

    public VirtualCollectionDetailsPage(final IModel<VirtualCollection> model, final PageParameters params) throws VirtualCollectionRegistryPermissionException {
        super(new CompoundPropertyModel<VirtualCollection>(model));
        //setPageStateless(true);
        this.params = params;
        setOutputMarkupId(true);

        //Enable content negotiation based redirect
        String redirectLocation = "/service/v1/collections/" + model.getObject().getId();
        RestUtils.checkRestApiRedirection((HttpServletRequest)getRequest().getContainerRequest(), redirectLocation);

        //Redirect to homepage if the model is not set
        if (model == null) {
            setResponsePage(Application.get().getHomePage());
            return;
        }
        
        //Will throw an exception and abort flow if authorization fails
        checkReadAccess(model.getObject());

        final Link<Void> backLink = new Link<Void>("back") {
            @Override
            public void onClick() {
                final PageReference previousPage = getPreviousPageReferenceFromSession();
                setResponsePage(getBackPageFromReference(previousPage, params));
            }
        };
        
        add(backLink);
        add(new HeaderPanel("vc_header", model, getPageReference(), this));
        add(BootstrapPanelBuilder
                .createCollapsiblePanel("general")
                .setTitle("General")
                .setBody(new GeneralPanel("body", model, getPageReference()))
                .build());
        
        add(BootstrapPanelBuilder
                .createCollapsiblePanel("creators")
                .setTitle("Creators")
                .setBody(new CreatorsPanel("body", model))
                .build());

        add(BootstrapPanelBuilder
                .createCollapsiblePanel("resources")
                .setTitle("Resources")
                .setBody(new ResourcesPanel("body", model), false)
                .setVisible(model.getObject().getType() == Type.EXTENSIONAL)
                .build());
        
        add(BootstrapPanelBuilder
                .createCollapsiblePanel("generatedBy")
                .setTitle("Generated By")
                .setBody(new GeneratedByPanel("body", model))
                .setVisible(model.getObject().getType() == Type.INTENSIONAL)
                .build());

        add(new DetailsStructuredMeatadataHeaderBehavior(model));
    }

    private  class HeaderPanel extends Panel {
        public HeaderPanel(String id, final IModel<VirtualCollection> model, final PageReference reference, Component componentToUpdate) {
            super(id, new CompoundPropertyModel<VirtualCollection>(model));
            add(new Label("name"));

            WebMarkupContainer icon = new WebMarkupContainer("name_icon");
            icon.add(new AttributeAppender("class", "fa fa-code-fork"));
            add(icon);

            ExternalLink forkedLink = new ExternalLink("forked_link", "#");

            VirtualCollection forkedFrom = model.getObject().getForkedFrom();
            IModel mdlForkedFrom = Model.of("");
            if(forkedFrom != null) {
                mdlForkedFrom.setObject(forkedFrom.getName());
                String url = Application.get().getPermaLinkService().getCollectionDetailsUrl(forkedFrom);
                forkedLink = new ExternalLink("forked_link", url);
            }
            forkedLink.add(new Label("forked_lbl_value", mdlForkedFrom));

            WebMarkupContainer forked = new WebMarkupContainer("forked_container");
            forked.add(new Label("forked_lbl","Forked from " ));
            forked.add(forkedLink);
            forked.setVisible(forkedFrom != null);
            add(forked);
            icon.setVisible(forkedFrom != null);

            //add(new VersionsPanel("versions", model, reference));
            add(new VersionsDropDownPanel("versions", model, reference));

            //Toggle editor mode checkbox
            add(new AjaxCheckBox("btn_editor_mode", showAdvancedFields) {
                @Override
                public void onUpdate(AjaxRequestTarget target) {
                    if (target != null) {
                        target.add(componentToUpdate);
                    }
                }
            });
            add(new Label("btn_editor_mode_label", Model.of("Show advanced fields")));


            try {
                owner = new User(getUser());
            } catch(Exception ex) {}

            add(LanguageResourceSwitchboardLink.forCollection("process", model.getObject(), vcrConfig));
            add(CMDIExplorerLink.forCollection("download", model.getObject(), vcrConfig));

            add(CitationPanelFactory.getCitationPanel("citation", model));
            AjaxLink btnFork = new AjaxLink("btn_fork", new Model<String>("Cite")) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    VirtualCollection forkedCollection =
                        VirtualCollectionFactory.createFork(model.getObject(), owner).getCollection();
                    //VirtualCollection forkedCollection = model.getObject().fork(owner);
                    SubmissionUtils.storeCollection((ApplicationSession)getSession(), forkedCollection);
                    setResponsePage(CreateAndEditVirtualCollectionPageV2.class);
                }
            } ;
            btnFork.setVisible(Application.get().getConfig().isForkingEnabled() && owner != null);
            btnFork.setEnabled(Application.get().getConfig().isForkingEnabled() && owner != null);
            UIUtils.addTooltip(btnFork, "Fork this collection");
            add(btnFork);
        }

        protected PageParameters buildParamsFromMap(Map<String, Long> map) {
            PageParameters params = new PageParameters();
            for(String key : map.keySet()) {
                params.add(key, map.get(key));
            }
            return params;
        }
    }
    
    private class BasicTextPanel extends Panel {
        public BasicTextPanel(String id, String label, IModel value) {
            this(id, label, value, false);
        }

        public BasicTextPanel(String id, String label, IModel value, boolean multiline) {
            this(id, label, value, false, false);
        }

        public BasicTextPanel(String id, String label, IModel value, boolean multiline, boolean withMarkdown) {
            super(id);            
            add(new Label("label", label));

            String htmlValue = "";
            if(value.getObject() != null) {
                MutableDataSet options = new MutableDataSet();
                Parser parser = Parser.builder(options).build();
                HtmlRenderer renderer = HtmlRenderer.builder(options).build();
                Node document = parser.parse(value.getObject().toString());
                htmlValue = renderer.render(document);
            }

            if(!multiline) {
                CustomLabel lbl = new CustomLabel("value", new Model(htmlValue));
                lbl.setEscapeModelStrings(false);
                add(lbl);
            } else {
                MultiLineLabel lbl = new MultiLineLabel("value", htmlValue);
                lbl.setEscapeModelStrings(false);
                add(lbl);
            }
        }
    }
    
    private class BasicListPanel extends Panel {
        public BasicListPanel(String id, String label, List listOfValues) {
            super(id);
            add(new Label("label", label));
            final ListView<String> list = new ListView<String>("values", listOfValues) {
                @Override
                protected void populateItem(ListItem<String> item) {
                    item.add(new Label("value", item.getModelObject()));
                }
            };
            add(list);
        }        
    }
    
    private class BasicPidPanel extends Panel {
        public BasicPidPanel(String id, String label, IModel<PersistentIdentifieable> model) {
            super(id);
            add(new Label("label", label));
            add(new PidPanel("value",  model, "collection"));
        }
    }
    
    private class BasicLinkPanelPopup extends Panel {
        public BasicLinkPanelPopup(String id, String label, IModel<String> model) {
            super(id);
            add(new Label("label", label));
            add(new ExternalLink("value",  model, model)
                    .setPopupSettings(new PopupSettings())
                    .add(hideIfEmpty));            
        }
    }

    private class BasicLinkPanel extends Panel {
        public BasicLinkPanel(String id, String label, IModel<String> model) {
            super(id);
            add(new Label("label", label));
            add(new ExternalLink("value",  model, model)
                    .add(hideIfEmpty));
        }
    }
    
    private class GeneralPanel extends Panel {
        public GeneralPanel(String id, final IModel<VirtualCollection> model, final PageReference reference) {
            super(id);

            add(new BasicTextPanel("name", "Name", new Model(model.getObject().getName())));
            add(new BasicTextPanel("type", "Type", new Model(model.getObject().getType())).add(hideIfShowAdvancedDisabled));
            add(new BasicTextPanel("creationDate", "Creation date", new Model(model.getObject().getCreationDate())));
            add(new BasicTextPanel("description", "Description", new Model(model.getObject().getDescription()), true, true).add(hideIfEmpty));
            add(new BasicTextPanel("purpose", "Purpose", new Model(model.getObject().getPurpose())).add(hideIfEmpty, hideIfShowAdvancedDisabled));
            add(new BasicTextPanel("reproducibility", "Reproducibility", new Model(model.getObject().getReproducibility())).add(hideIfEmpty, hideIfShowAdvancedDisabled));
            add(new BasicTextPanel("reproducibilityNotice", "Reproducibility notice", new Model(model.getObject().getReproducibilityNotice()), true, true).add(hideIfEmpty, hideIfShowAdvancedDisabled));

            List<PersistentIdentifier> latestPidList = new LinkedList<>();
            for(PersistentIdentifier pid : model.getObject().getLatestIdentifiers()) {
                latestPidList.add(pid);
            }
            ListView latestPidsListView = new ListView("latest_pids", latestPidList) {
                @Override
                protected void populateItem(ListItem item) {
                    PersistentIdentifier p = (PersistentIdentifier)item.getModel().getObject();
                    item.add(new BasicPidPanel("latest_pid", "Latest Persistent identifier", new Model(p)));
                }
            };
            latestPidsListView.setVisible(!latestPidList.isEmpty());
            add(latestPidsListView);

            List<PersistentIdentifier> pidList = new LinkedList<>();
            for(PersistentIdentifier pid : model.getObject().getIdentifiers()) {
                pidList.add(pid);
            }
            ListView pidsListView = new ListView("pids", pidList) {
                @Override
                protected void populateItem(ListItem item) {
                    PersistentIdentifier p = (PersistentIdentifier)item.getModel().getObject();
                    item.add(new BasicPidPanel("pid", "Persistent identifier", new Model(p)));
                }
            };
            add(pidsListView);

            add(new BasicListPanel("keywords", "Keywords", model.getObject().getKeywords()).add(hideIfEmpty));
        }
    }

    private class CreatorsPanel extends Panel {
        public CreatorsPanel(String id, final IModel<VirtualCollection> model) {
            super(id);
            add(new ListView<Creator>("creators") {
                @Override
                protected void populateItem(ListItem<Creator> item) {
                    Creator creator = item.getModel().getObject();
                    item.add(new BasicTextPanel("person", "Person", new Model(creator.getPerson())));
                    item.add(new BasicTextPanel("address", "Address", new Model(creator.getAddress())).add(hideIfEmpty)); 
                    item.add(new BasicTextPanel("organisation", "Organisation", new Model(creator.getOrganisation())).add(hideIfEmpty)); 
                    item.add(new BasicTextPanel("email", "Email", new Model(creator.getEMail())).add(hideIfEmpty));
                    item.add(new BasicTextPanel("telephone", "Telephone", new Model(creator.getTelephone())).add(hideIfEmpty));
                    //item.add(new BasicTextPanel("website", "Website", new Model(creator.getWebsite())).add(hideIfEmpty));
                    item.add(new BasicLinkPanel("website", "Website", new Model(creator.getWebsite())).add(hideIfEmpty));
                    item.add(new BasicTextPanel("role", "Role", new Model(creator.getRole())).add(hideIfEmpty));
                }

                @Override
                protected IModel<Creator> getListItemModel(
                        IModel<? extends List<Creator>> listViewModel, int index) {
                    final List<Creator> creators = listViewModel.getObject();
                    return new CompoundPropertyModel<Creator>(
                            new VolatileEntityModel<>(creators.get(index)));
                }

                @Override
                protected ListItem<Creator> newItem(int index,  IModel<Creator> model) {
                    return new OddEvenListItem<Creator>(index, model) {
                        @Override
                        protected void onComponentTag(ComponentTag tag) {
                            super.onComponentTag(tag);
                            if (getIndex() == 0) {
                                tag.append("class", "first", " ");
                            }
                        }
                    };
                }
            });
        }
    }

    private class ResourcesPanel extends Panel {
        public ResourcesPanel(String id, final IModel<VirtualCollection> model) {
            super(id);            
            add(buildResourcesTable("resourcesTable", model));
        }
    }
    
    private DataTable<Resource, String> buildResourcesTable(String id, final IModel<VirtualCollection> model) {
        @SuppressWarnings("rawtypes")
            final List<IColumn<Resource, String>> cols = new ArrayList<>();
            cols.add(new AbstractColumn<Resource, String>(Model.of("Reference")) {
                @Override
                public void populateItem(Item<ICellPopulator<Resource>> item, String componentId, IModel<Resource> rowModel) {                
                    item.add(new ReferenceLinkPanel(componentId, rowModel));
                }

                @Override
                public String getCssClass() {
                    return "reference";
                }
            });

            //Make sure to check all possible actions. Only add action column if there
            //is more than one action enabled.
            if (vcrConfig.isProcessEnabledForResources()) {
                cols.add(new AbstractColumn<Resource, String>(Model.of("Actions")) {
                    @Override
                    public void populateItem(Item<ICellPopulator<Resource>> item, String componentId, IModel<Resource> rowModel) {
                        item.add(getDropdown(componentId, rowModel));
                    }

                    @Override
                    public String getCssClass() {
                        return "actions";
                    }
                });
            }

            final SortableDataProvider<Resource, String> resourcesProvider = new SortableDataProvider<Resource, String>() {
                @Override
                public Iterator<? extends Resource> iterator(long first, long count) {
                    return model.getObject().getResources().listIterator((int)first);
                }

                @Override
                public IModel<Resource> model(Resource resource) {
                    return new VolatileEntityModel<>(resource);
                }

                @Override
                public long size() {
                    return model.getObject().getResources().size();
                }
            };

            final DataTable<Resource, String> resourcesTable
                    = new AjaxFallbackDefaultDataTable<>(id,
                            cols, resourcesProvider, 64);
            return resourcesTable;
    }
    
    private BootstrapDropdown getDropdown(String id, IModel<Resource> model) {
        List options = new ArrayList();
        options.add(new DropdownMenuItem("Process with Language Resource Switchboard", "glyphicon glyphicon-open-file") {
                @Override
                protected AbstractLink getLink(String id) {
                    return (AbstractLink)LanguageResourceSwitchboardLink.forResource("link", model.getObject());
                }
            });                
         
        BootstrapDropdown dropDown = new BootstrapDropdown(id, new ListModel(options)) {
            @Override
            protected Serializable getButtonClass() {
                return null; //render as link, not button
            }

            @Override
            protected Serializable getButtonIconClass() {
                return "glyphicon glyphicon-option-horizontal";
            }

            @Override
            protected boolean showCaret() {
                return false;
            }
        };
                
        return dropDown;
    }
    
    private class GeneratedByPanel extends Panel {
        public GeneratedByPanel(String id, final IModel<VirtualCollection> model) {
            super(id);
            GeneratedBy gb = model.getObject().getGeneratedBy();
            add(new BasicTextPanel("description", "Name", new Model(gb == null ? "" : gb.getDescription())));
            add(new BasicTextPanel("uri", "URI", new Model(gb == null ? "" : gb.getURI())));
            add(new BasicTextPanel("query.profile", "Query profile", new Model(gb == null ? "" : gb.getQuery().getProfile())));
            add(new BasicTextPanel("query.value", "Query value", new Model(gb == null ? "" : gb.getQuery().getValue())));
        }
    }
    
    private static IModel<VirtualCollection> getVirtualCollectionModel(PageParameters params) {
        Long collectionId = null;
        
        StringValue id = params.get(PARAM_VC_ID);
        if (id == null) {
            Session.get().error("Collection could not be retrieved, id not provided");
            return null;
        }
        try {
            collectionId = id.toLong();
        } catch(Exception ex) {
            Session.get().error("Collection could not be retrieved, id ("+id+") not a valid number.");
            return null;
        }
        /*
        if (collectionId == null) {
            Session.get().error("Collection could not be retrieved, id ("+id+") is invalid");
            return null;
        }
        */
        return new DetachableVirtualCollectionModel(collectionId);
    }

    private Class getBackPageFromReference(PageReference reference, PageParameters params) {
        if(reference != null) {
            return reference.getPage().getPageClass();
        } else {
           StringValue backPageParam = params.get(VirtualCollectionDetailsPage.PARAM_BACK_PAGE);
           if(backPageParam != null & !backPageParam.isEmpty()) {
                switch(BackPage.fromInt(backPageParam.toInt())) {
                    case PUBLIC_LISTING: return BrowsePublicCollectionsPage.class;
                    case PRIVATE_LISTING: return BrowsePrivateCollectionsPage.class;
                    case ADMIN_LISTING: return AdminPage.class;
                }
           }
        }
        return getApplication().getHomePage();
    }
    
    private static PageReference getPreviousPageReferenceFromSession() {
        if(Session.exists()) {
            Object pageReference = Session.get().getAttribute("reference");
            if(pageReference != null) {
                return (PageReference)pageReference;
            }
        }
        return null;
        
    }

    public static PageParameters createPageParameters(VirtualCollection vc, PageReference pageReference, BackPage backPage) {
        return createPageParameters(vc.getId(), pageReference, backPage);
    }

    public static PageParameters createPageParameters(Long vcId, PageReference pageReference, BackPage backPage) {
        final PageParameters params = new PageParameters();
        params.set(VirtualCollectionDetailsPage.PARAM_VC_ID, vcId);
        params.set(VirtualCollectionDetailsPage.PARAM_BACK_PAGE, backPage.intValue());
        if (pageReference != null) {
            Session.get().setAttribute("reference", pageReference);
        }
        return params;
    }

    /**
     *
     * @throws UnauthorizedActionException if the VC is private and the current
     * user is not the owner
     */
    /*
    private void checkAccess(final VirtualCollection vc) throws UnauthorizedActionException {
        if (vc.isPrivate()
                && !isUserAdmin()
                && !getSession().isCurrentUser(vc.getOwner())) {
            // user trying to access other user's collection
            throw new UnauthorizedActionException(this, Component.RENDER);
        }
    }
*/
    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        //Order is important
        hideIfShowAdvancedDisabled.hideIfShowAdvancedDisabled(showAdvancedFields.getObject());
        hideIfEmpty.hideEmptyComponents();
    }
    
    public static enum BackPage {
        PUBLIC_LISTING(0), PRIVATE_LISTING(1), ADMIN_LISTING(2);
        
        private final int value;
        
        private BackPage(int value) {
            this.value = value;
        }
        
        public int intValue() {
            return this.value;
        }
        
        public static BackPage fromInt(int value) {
            switch(value) {
                case 0: return BackPage.PUBLIC_LISTING;
                case 1: return BackPage.PRIVATE_LISTING;
                case 2: return BackPage.ADMIN_LISTING;
                default:
                    return BackPage.PUBLIC_LISTING;
            }
        }
    }

    @Override
    public IModel<String> getCanonicalUrlModel() {
        //Ignore non canonical parameters
        final PageParameters _params = new PageParameters();
        for(NamedPair pair : params.getAllNamed()) {
            if(!pair.getKey().equalsIgnoreCase(VirtualCollectionDetailsPage.PARAM_BACK_PAGE)) {
                _params.add(pair.getKey(), pair.getValue());
            }
         }
        //Build absolute url
        final CharSequence url = RequestCycle.get().urlFor(getClass(), _params);
        final String absoluteUrl = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
        return new Model(absoluteUrl);
    }
    
} // class VirtualCollectionDetailsPage
