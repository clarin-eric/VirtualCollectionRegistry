package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import com.google.common.collect.Lists;
import eu.clarin.cmdi.virtualcollectionregistry.config.VcrConfigImpl;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.DateConverter;
import eu.clarin.cmdi.virtualcollectionregistry.gui.DetachableVirtualCollectionModel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.VolatileEntityModel;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Type;
import eu.clarin.cmdi.wicket.components.citation.CitationPanelFactory;
import eu.clarin.cmdi.wicket.components.panel.BootstrapDropdown;
import eu.clarin.cmdi.wicket.components.panel.BootstrapDropdown.DropdownMenuItem;
import eu.clarin.cmdi.wicket.components.panel.BootstrapPanelBuilder;
import eu.clarin.cmdi.wicket.components.pid.PersistentIdentifieable;
import eu.clarin.cmdi.wicket.components.pid.PidPanel;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.Component;
import org.apache.wicket.PageReference;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.UnauthorizedActionException;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.ComponentTag;
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

@SuppressWarnings("serial")
public class VirtualCollectionDetailsPage extends BasePage {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionDetailsPage.class);
    
    public static final String PARAM_VC_ID = "id";
    public static final String PARAM_BACK_PAGE = "backPage";
    //private static final String CSS_CLASS = "collectionDetails";
    private static final IConverter convDate = new DateConverter();
    private final HideIfEmptyBehavior hideIfEmpty = new HideIfEmptyBehavior();

    private final PageParameters params;
    
    @SpringBean
    private VcrConfigImpl vcrConfig;
    
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
    
    public VirtualCollectionDetailsPage(PageParameters params) {
        this(getVirtualCollectionModel(params), params);
    }

    public VirtualCollectionDetailsPage(final IModel<VirtualCollection> model, final PageParameters params) {
        super(new CompoundPropertyModel<VirtualCollection>(model));
        //setPageStateless(true);
        this.params = params;
        
        //Redirect to homepage if the model is not set
        if (model == null) {
            setResponsePage(Application.get().getHomePage());
            return;
        }
        
        //Will throw and exception and abort flow if authorization fails
        checkAccess(model.getObject());

        final Link<Void> backLink = new Link<Void>("back") {
            @Override
            public void onClick() {
                final PageReference previousPage = getPreviousPageReferenceFromSession();
                setResponsePage(getBackPageFromReference(previousPage, params));
            }
        };
        
        add(backLink);
        add(new HeaderPanel("vc_header", model));
        add(BootstrapPanelBuilder
                .createCollapsiblePanel("general")
                .setTitle("General")
                .setBody(new GeneralPanel("body", model))
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
    }
    
    private  class HeaderPanel extends Panel {
        public HeaderPanel(String id, final IModel<VirtualCollection> model) {
            super(id, new CompoundPropertyModel<VirtualCollection>(model));
            add(new Label("name"));
            add(CitationPanelFactory.getCitationPanel("citation", model));
        }    
    }
    
    private class BasicTextPanel extends Panel {
        public BasicTextPanel(String id, String label, IModel value) {
            this(id, label, value, false);
        }
        
        public BasicTextPanel(String id, String label, IModel value, boolean multiline) {
            super(id);            
            add(new Label("label", label));
            if(!multiline) {
                add(new CustomLabel("value", value));
            } else {
                add(new MultiLineLabel("value", value));
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
    
    private class BasicLinkPanel extends Panel {
        public BasicLinkPanel(String id, String label, IModel<String> model) {
            super(id);
            add(new Label("label", label));
            add(new ExternalLink("value",  model, model)
                    .setPopupSettings(new PopupSettings())
                    .add(hideIfEmpty));            
        }
    }
    
    private class GeneralPanel extends Panel {
        public GeneralPanel(String id, final IModel<VirtualCollection> model) {
            super(id);
            add(new BasicTextPanel("name", "Name", new Model(model.getObject().getName())));
            add(new BasicTextPanel("type", "Type", new Model(model.getObject().getType())));            
            add(new BasicTextPanel("creationDate", "Creation date", new Model(model.getObject().getCreationDate())));
            add(new BasicTextPanel("description", "Description", new Model(model.getObject().getDescription())).add(hideIfEmpty));
            add(new BasicTextPanel("purpose", "Purpose", new Model(model.getObject().getPurpose())).add(hideIfEmpty));
            add(new BasicTextPanel("reproducibility", "Reproducibility", new Model(model.getObject().getReproducibility())).add(hideIfEmpty));
            add(new BasicTextPanel("reproducibilityNotice", "Reproducibility notice", new Model(model.getObject().getReproducibilityNotice())).add(hideIfEmpty));
            add(new BasicPidPanel("pid", "Persistent identifier", new Model(model.getObject())));
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
                    item.add(new BasicTextPanel("website", "Website", new Model(creator.getWebsite())).add(hideIfEmpty));
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
            cols.add(new PropertyColumn<Resource, String>(
                    Model.of("Type"), "type") {
                        @Override
                        public void populateItem(Item<ICellPopulator<Resource>> item,
                                String componentId, IModel<Resource> model) {
                            final Resource.Type type = model.getObject().getType();
                            item.add(new Label(componentId,
                                            convEnum.convertToString(type, getLocale())));
                        }

                        @Override
                        public String getCssClass() {
                            return "type";
                        }
                    });
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
            if (vcrConfig.isSwitchboardEnabledForResources()) {     
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
         List options = 
            Lists.newArrayList(new DropdownMenuItem("Process with Language Resource Switchboard", "glyphicon glyphicon-open-file") {
                @Override
                protected AbstractLink getLink(String id) {
                    AbstractLink link = UIUtils.getLrsRedirectAjaxLinkForResource(id, model, vcrConfig.getSwitchboardEndpoint());
                    return link;
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
           if(params.get(VirtualCollectionDetailsPage.PARAM_BACK_PAGE) != null) {
                switch(BackPage.fromInt(params.get(VirtualCollectionDetailsPage.PARAM_BACK_PAGE).toInt())) {
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
        final PageParameters params = new PageParameters();
        params.set(VirtualCollectionDetailsPage.PARAM_VC_ID, vc.getId());
        params.set(VirtualCollectionDetailsPage.PARAM_BACK_PAGE, backPage.intValue());
        if (pageReference != null) {
            Session.get().setAttribute("reference", pageReference);
        }
        return params;
    }

    /**
     *
     * @param vc collection to check for
     * @throws UnauthorizedActionException if the VC is private and the current
     * user is not the owner
     */
    private void checkAccess(final VirtualCollection vc) throws UnauthorizedActionException {
        if (vc.isPrivate()
                && !isUserAdmin()
                && !getSession().isCurrentUser(vc.getOwner())) {
            // user trying to access other user's collection
            throw new UnauthorizedActionException(this, Component.RENDER);
        }
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
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
