package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.DateConverter;
import eu.clarin.cmdi.virtualcollectionregistry.gui.DetachableVirtualCollectionModel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.VolatileEntityModel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.border.AjaxToggleBorder;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Type;
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
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.OddEvenListItem;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.ComponentPropertyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.Strings;

@SuppressWarnings("serial")
public class VirtualCollectionDetailsPage extends BasePage {

    public static final String PARAM_VC_ID = "id";
    public static final String PARAM_BACK_PAGE_ID = "backPage";
    public static final String PARAM_BACK_PAGE_VERSION = "backPageVersion";
    public static final String PARAM_BACK_PAGE_PAGEMAP_NAME = "backPageMapName";
    private static final String CSS_CLASS = "collectionDetails";
    private static final IConverter convDate = new DateConverter();
    private final HideIfEmptyBehavior hideIfEmpty = new HideIfEmptyBehavior();

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

        
       // @Override
        //public void cleanup() {
        //    super.cleanup();
        //    components.clear();
        //}
    } // class VirtualCollectionDetailsPage.HideIfEmptyBehavior

    private static class CustomLabel<C> extends Label {

        public CustomLabel(String id) {
            super(id);
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
        this(getVirtualCollectionModel(params), getPageReference(params));
    }

    public VirtualCollectionDetailsPage(final IModel<VirtualCollection> model, final PageReference previousPage) {
        super(new CompoundPropertyModel<VirtualCollection>(model));
        if (model == null) {
            setResponsePage(Application.get().getHomePage());
        } else {
            checkAccess(model.getObject());
        }

        final Link<Void> backLink = new Link<Void>("back") {
            @Override
            public void onClick() {
                if (previousPage == null) {
                    setResponsePage(getApplication().getHomePage());
                } else {
                    setResponsePage(previousPage.getPage());
                }
            }
        };
        add(backLink);
        add(new HeaderPanel("headerPanel", model));
        addGeneralProperties(model);
        addCreators();
        addResources(model);
        addGeneratedBy(model);
    }

    private void addGeneralProperties(final IModel<VirtualCollection> model) {
        final Border general = new AjaxToggleBorder("generalBorder",
                new Model<String>("General"), CSS_CLASS);
        add(general);
        general.add(new Label("name"));
        general.add(new CustomLabel("type"));
        general.add(new CustomLabel("creationDate"));
        general.add(new MultiLineLabel("description").add(hideIfEmpty));
        general.add(new CustomLabel("purpose").add(hideIfEmpty));
        general.add(new CustomLabel("reproducibility").add(hideIfEmpty));
        general.add(new Label("reproducibilityNotice").add(hideIfEmpty));

        final ExternalLink pidLink = new ExternalLink("pidLink", new PropertyModel<String>(model, "persistentIdentifier.actionableURI"));
        pidLink.add(new Label("persistentIdentifier.URI"));
        pidLink.add(hideIfEmpty);
        general.add(pidLink);

        addKeywords(general);
    }

    private void addKeywords(final Border general) {
        final ListView<String> keywords = new ListView<String>("keywords") {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("keyword", item.getModelObject()));
            }
        };
        keywords.add(hideIfEmpty);
        general.add(keywords);
    }

    private void addCreators() {
        final Border creators = new AjaxToggleBorder("creatorsBorder",
                new Model<String>("Creators"), CSS_CLASS);
        add(creators);
        creators.add(new ListView<Creator>("creators") {
            @Override
            protected void populateItem(ListItem<Creator> item) {
                item.add(new Label("person"));
                item.add(new MultiLineLabel("address").add(hideIfEmpty));
                item.add(new Label("organisation").add(hideIfEmpty));
                item.add(new Label("email").add(hideIfEmpty));
                item.add(new Label("telephone").add(hideIfEmpty));
                final IModel<String> siteModel
                        = new ComponentPropertyModel<String>("website");
                item.add(new ExternalLink("website", siteModel, siteModel)
                        .setPopupSettings(new PopupSettings())
                        .add(hideIfEmpty));
                item.add(new Label("role").add(hideIfEmpty));
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
                //final IModel<Creator> model
                //        = getListItemModel(getModel(), index);
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

    private void addResources(final IModel<VirtualCollection> model) {
        final Border resources = new AjaxToggleBorder("resourcesBorder",
                new Model<String>("Resources"), CSS_CLASS + " resources");
        add(resources);

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

        final SortableDataProvider<Resource, String> resourcesProvider = new SortableDataProvider<Resource, String>() {
            @Override
            public Iterator<? extends Resource> iterator(long first, long count) {
                return model.getObject().getResources().listIterator((int)first);
            }

            @Override
            public IModel<Resource> model(Resource resource) {
                return new VolatileEntityModel<Resource>(resource);
            }

            @Override
            public long size() {
                return model.getObject().getResources().size();
            }
        };

        final DataTable<Resource, String> resourcesTable
                = new AjaxFallbackDefaultDataTable<>("resourcesTable",
                        cols, resourcesProvider, 64);
        resources.add(resourcesTable);
        resources.setVisible(model.getObject().getType() == Type.EXTENSIONAL);
    }

    private void addGeneratedBy(final IModel<VirtualCollection> model) {
        final Border generated = new AjaxToggleBorder("generatedByBorder",
                new Model<String>("Intensional Collection Query"), CSS_CLASS);
        add(generated);
        generated.add(new Label("generatedBy.description"));
        generated.add(new Label("generatedBy.uri").add(hideIfEmpty));
        generated.add(new Label("generatedBy.query.profile").add(hideIfEmpty));
        generated.add(new Label("generatedBy.query.value").add(hideIfEmpty));
        generated.setVisible(model.getObject().getType() == Type.INTENSIONAL);
    }

    private static IModel<VirtualCollection> getVirtualCollectionModel(PageParameters params) {
        final Long collectionId = params.get(PARAM_VC_ID).toLong();
        if (collectionId == null) {
            Session.get().error("Collection could not be retrieved, id not provided");
            return null;
        }
        return new DetachableVirtualCollectionModel(collectionId);
    }

    private static PageReference getPageReference(PageParameters params) {
        final Integer pageId = params.get(PARAM_BACK_PAGE_ID).toInt();
        //final Integer pageVersion = params.get(PARAM_BACK_PAGE_VERSION).toInt();
        
        
        //final String pageMap = params.get(PARAM_BACK_PAGE_PAGEMAP_NAME).toString();
        
        //TODO: fix in wicket 1.5
        /*
        if (pageId != null && pageVersion != null) {
            for (IPageMap map : Session.get().getPageMaps()) {
                if (pageMap == null && map.getName() == null || pageMap != null && pageMap.equals(map.getName())) {
                    final Page page = map.get(pageId, pageVersion);
                    if (page != null) {
                        return page.getPageReference();
                    }
                }
            }
        }
        */
        return null;
    }

    public static PageParameters createPageParameters(VirtualCollection vc, PageReference pageReference) {
        final PageParameters params = new PageParameters();
        params.set(VirtualCollectionDetailsPage.PARAM_VC_ID, vc.getId());

        if (pageReference != null) {
            params.set(VirtualCollectionDetailsPage.PARAM_BACK_PAGE_ID, pageReference.getPageId());
            //TODO: fix in wicket 1.5
            /*
            params.set(VirtualCollectionDetailsPage.PARAM_BACK_PAGE_VERSION, pageReference.getPage().getVersion());
            if (pageReference.getPageMapName() != null) {
                params.set(VirtualCollectionDetailsPage.PARAM_BACK_PAGE_PAGEMAP_NAME, pageReference.getPageMapName());
            }
            */
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

} // class VirtualCollectionDetailsPage
