package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.DateConverter;
import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
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
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.Strings;

import eu.clarin.cmdi.virtualcollectionregistry.gui.border.AjaxToggleBorder;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection.Type;


@SuppressWarnings("serial")
public class VirtualCollectionDetailsPage extends BasePage {
    private static final IConverter convEnum = new IConverter() {
        @Override
        public String convertToString(Object o, Locale locale) {
            final Enum<?> enumObj = (Enum<?>) o;
            final String key =
                enumObj.getDeclaringClass().getSimpleName() + "." +
                enumObj.name();
            final String value = Application.get().getResourceSettings()
                    .getLocalizer().getString(key, null);
            return Strings.escapeMarkup(value).toString();
        }

        @Override
        public Object convertToObject(String s, Locale locale) {
            return null;
        }
    };
    private static final IConverter convDate = new DateConverter();

    private static final String CSS_CLASS = "collectionDetails";

    /*
     * Actually, we really want the behavior to hide the component on
     * the beforeRender() call, but we always get an exception from Wicket
     * that we are not supposed to change the page hierarchy anymore. This
     * class is a hack to avoid this exception.
     */
    private static final class HideIfEmptyBehavior extends AbstractBehavior {
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

        @Override
        public void cleanup() {
            super.cleanup();
            components.clear();
        }
    } // class VirtualCollectionDetailsPage.HideIfEmptyBehavior

    private static class CustomLabel extends Label {

        public CustomLabel(String id) {
            super(id);
        }

        @Override
        public IConverter getConverter(Class<?> type) {
            if (VirtualCollection.Type.class.isAssignableFrom(type) ||
                VirtualCollection.Purpose.class.isAssignableFrom(type) ||
                VirtualCollection.Reproducibility.class.isAssignableFrom(type)) {
                return convEnum;
            }
            if (Date.class.isAssignableFrom(type)) {
                return convDate;
            }
            return super.getConverter(type);
        }
    } // class VirtualCollectionDetailsPage.TypeLabel


    private final HideIfEmptyBehavior hideIfEmpty = new HideIfEmptyBehavior();


    public VirtualCollectionDetailsPage(final IModel<VirtualCollection> model,
            final Page previousPage) {
        super(new CompoundPropertyModel<VirtualCollection>(model.getObject()));
        if (previousPage == null) {
            throw new IllegalArgumentException("previousPage == null");
        }
        final Link<Void> backLink = new Link<Void>("back") {
            @Override
            public void onClick() {
                setResponsePage(previousPage);
            }
        };
        add(backLink);

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
        final ListView<String> keywords = new ListView<String>("keywords") {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("keyword", item.getModelObject()));
            }
        };
        keywords.add(hideIfEmpty);
        general.add(keywords);

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
                final IModel<String> siteModel =
                    new ComponentPropertyModel<String>("website");
                item.add(new ExternalLink("website", siteModel, siteModel)
                        .setPopupSettings(new PopupSettings())
                        .add(hideIfEmpty));
                item.add(new Label("role").add(hideIfEmpty));
            }

            @Override
            protected IModel<Creator> getListItemModel(
                    IModel<? extends List<Creator>> listViewModel, int index) {
                final List<Creator> creators = listViewModel.getObject();
                return new CompoundPropertyModel<Creator>(creators.get(index));
            }

            @Override
            protected ListItem<Creator> newItem(int index) {
                final IModel<Creator> model =
                    getListItemModel(getModel(), index);
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

        final Border resources = new AjaxToggleBorder("resourcesBorder",
                new Model<String>("Resources"), CSS_CLASS + " resources");
        add(resources);

        final VirtualCollection vc = model.getObject();
        @SuppressWarnings("rawtypes")
        final IColumn[] cols = new IColumn[2];
        cols[0] = new PropertyColumn<Resource>(
                new Model<String>("Type"), "type") {
            @Override
            public void populateItem(Item<ICellPopulator<Resource>> item,
                    String componentId, IModel<Resource> model) {
                final Resource.Type type = model.getObject().getType();
                item.add(new Label(componentId,
                        convEnum.convertToString(type, getLocale())));
            }
        };
        cols[1] = new PropertyColumn<Resource>(
                new Model<String>("Reference"), "ref");

        @SuppressWarnings("unchecked")
        final DataTable<Resource> resourcesTable =
            new AjaxFallbackDefaultDataTable<Resource>("resourcesTable",
                    cols,
                    new SortableDataProvider<Resource>() {
                        @Override
                        public Iterator<? extends Resource>
                            iterator(int first, int count) {
                            return vc.getResources().listIterator(first);
                        }

                        @Override
                        public IModel<Resource> model(Resource resource) {
                            return new Model<Resource>(resource);
                        }

                        @Override
                        public int size() {
                            return vc.getResources().size();
                        }
                    },
                    64);
        resources.add(resourcesTable);
        resources.setVisible(model.getObject().getType() == Type.EXTENSIONAL);

        final Border generated = new AjaxToggleBorder("generatedByBorder",
                new Model<String>("Intensional Collection Query"), CSS_CLASS);
        add(generated);
        generated.add(new Label("generatedBy.description"));
        generated.add(new Label("generatedBy.uri").add(hideIfEmpty));
        generated.add(new Label("generatedBy.query.profile").add(hideIfEmpty));
        generated.add(new Label("generatedBy.query.value").add(hideIfEmpty));
        generated.setVisible(model.getObject().getType() == Type.INTENSIONAL);
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        hideIfEmpty.hideEmptyComponents();
    }

} // class VirtualCollectionDetailsPage
