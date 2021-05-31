package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.ApiKeyService;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.auth.LoginPage;
import eu.clarin.cmdi.virtualcollectionregistry.model.ApiKey;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.*;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.security.Principal;
import java.util.*;

public class UserProfilePage extends BasePage {
    private final static Logger logger = LoggerFactory.getLogger(UserProfilePage.class);

    @SpringBean
    private ApiKeyService apiKeyService;

    private final Component componentToUpdate;

    private List<ApiKey> keys = new LinkedList<>();

    private Properties i18n = new Properties();

    private final Label lblNoKeys;
    private final DataTable<ApiKey, String> table;

    private interface MarkupIdGenerator {
        public String getMarkupId(ApiKey key);
    }

    private class CopyMarkupIdGenerator implements MarkupIdGenerator, Serializable {
        private Map<String, String> markupIds = new HashMap<>();
        private long nextId = 0;

        @Override
        public synchronized String getMarkupId(ApiKey key) {
            if(!markupIds.containsKey(key.getValue())) {
                nextId += 1;
                markupIds.put(key.getValue(), "api-key-"+nextId);
            }
            return markupIds.get(key.getValue());
        }
    }

    public UserProfilePage() {
        super();

        i18n.setProperty("btn_new_label", "Generate new API key");
        i18n.setProperty("btn_new_tooltip", "Generate a new API key");
        i18n.setProperty("btn_copy_label", "Copy");
        i18n.setProperty("btn_copy_tooltip", "Copy this API key to clipboard");
        i18n.setProperty("btn_revoke_label", "Revoke");
        i18n.setProperty("btn_revoke_tooltip", "Revoke this API key");

        i18n.setProperty("msg_no_api_keys", "No API keys found");
        i18n.setProperty("msg_null_principal", "Failed to fetch API keys for null principal");

        i18n.setProperty("tbl_header_api_key", "API Key");
        i18n.setProperty("tbl_header_created", "Created At");
        i18n.setProperty("tbl_header_last_used", "Last Used At");
        i18n.setProperty("tbl_header_revoked", "Revoked At");
        i18n.setProperty("tbl_header_actions", "Actions");
        i18n.setProperty("tbl_api_key_active", "Active");

        componentToUpdate = this;
        setOutputMarkupId(true);

        try {
            final Principal principal = getUser();
            if(principal == null) {

            }
            final String username = principal.getName();
            User user = apiKeyService.getUser(username);

            keys = new LinkedList<>(user.getApiKeys());
            Collections.sort(keys);

            lblNoKeys = new Label("no_api_keys", i18n.getProperty("msg_no_api_keys"));
            lblNoKeys.setVisible(keys.isEmpty());
            add(lblNoKeys);

            final MarkupIdGenerator generator = new CopyMarkupIdGenerator();

            final ApiKeyProvider provider = new ApiKeyProvider(keys);
            final List<IColumn<ApiKey, String>> columns = new ArrayList<>();
            columns.add(
                new SimpleTextColumn(i18n.getProperty("tbl_header_api_key"), "value","value")
                    .addCssClass("api-key").setMarkupIdGenerator(generator));
            columns.add(new SimpleTextColumn(i18n.getProperty("tbl_header_created"), "createdAt"));
            columns.add(new SimpleTextColumn(i18n.getProperty("tbl_header_last_used"), "lastUsedAt", "Not used"));
            columns.add(new SimpleTextColumn(i18n.getProperty("tbl_header_revoked"), "revokedAt", "Active"));
            columns.add(new ButtonColumn(generator));

            table = new AjaxFallbackDefaultDataTable<>("api_keys_table",
                            columns, provider, 30);
            table.setVisible(!keys.isEmpty());
            add(table);

            //add(new ClipboardJsBehavior());


            AjaxFallbackLink btnSave = new AjaxFallbackLink("btn_new_api_key") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    apiKeyService.generateNewKeyForUser(username);
                    if (target != null) {
                        target.add(componentToUpdate);
                    }
                }
            };
            btnSave.add(new Label("btn_new_api_key_label", i18n.getProperty("btn_new_label")));
            btnSave.add(new AttributeModifier("title", i18n.getProperty("btn_new_tooltip")));
            add(btnSave);
        } catch(Exception ex) {
            add(new Label("api_keys", i18n.getProperty("msg_null_principal")));
            WebMarkupContainer btnSave = new WebMarkupContainer("btn_new_api_key");
            btnSave.setEnabled(false);
            add(btnSave);

            logger.debug("Redirecting to login page", ex);
            throw new RestartResponseException(LoginPage.class);
        }
    }


    public abstract class BasicColumn extends AbstractColumn<ApiKey, String> {

        public abstract void populateItem(Item<ICellPopulator<ApiKey>> item, String componentId, IModel<ApiKey> model);

        private final String defaultCssClass = "api-key-table-cell-padding";
        private List<String> cssClasses = new LinkedList<>();

        protected MarkupIdGenerator markupIdGenerator = null;

        public BasicColumn(IModel<String> model) {
            super(model);
        }

        public BasicColumn(IModel<String> model, String sortProperty) {
            super(model, sortProperty);
        }

        public BasicColumn addCssClass(String cssClass) {
            cssClasses.add(cssClass);
            return this;
        }

        public BasicColumn setMarkupIdGenerator(MarkupIdGenerator generator) {
            this.markupIdGenerator = generator;
            return this;
        }

        @Override
        public String getCssClass() {
            String cssClass = defaultCssClass;
            for(String nextCssClass : cssClasses) {
                cssClass+=" "+nextCssClass;
            }
            return cssClass;
        }
    }

    public class SimpleTextColumn extends BasicColumn {
        private final String property;
        private final String nullValue;

        SimpleTextColumn(String headingText, String property) {
            this(headingText, property, null);
        }

        SimpleTextColumn(String headingText, String property, String nullValue) {
            super(new ResourceModel("column."+property, headingText), property);
            this.property = property;
            this.nullValue = nullValue;
        }

        @Override
        public void populateItem(Item<ICellPopulator<ApiKey>> item, String componentId, IModel<ApiKey> model) {
            IModel lbl_model = new PropertyModel<>(model.getObject(), property);
            if(lbl_model.getObject() == null && nullValue != null) {
                lbl_model = Model.of(nullValue);
            }
            Label lbl = new Label(componentId, lbl_model);
            if(markupIdGenerator != null) {
                String markupId = markupIdGenerator.getMarkupId(model.getObject());
                lbl.setMarkupId(markupId);
            }
            item.add(lbl);
        }
    }

    public class ButtonColumn extends BasicColumn {
        private final MarkupIdGenerator markupIdGenerator;

        ButtonColumn(final MarkupIdGenerator markupIdGenerator) {
            super(new ResourceModel("key", i18n.getProperty("tbl_header_actions")), "actions");
            this.markupIdGenerator = markupIdGenerator;
        }

        @Override
        public void populateItem(Item<ICellPopulator<ApiKey>> item, String componentId, IModel<ApiKey> model) {
            final ApiKey key = model.getObject();
            item.add(new ActionsPanel(componentId, key, markupIdGenerator));
        }
    }

    @Override
    protected void onBeforeRender() {
        final Principal principal = getUser();
        if(principal != null) {
            User user = apiKeyService.getUser(principal.getName());
            keys.clear();
            if (!user.getApiKeys().isEmpty()) {
                keys.addAll(user.getApiKeys());
                Collections.sort(keys);
            }

            //Update UI component visability
            lblNoKeys.setVisible(keys.isEmpty());
            table.setVisible(!keys.isEmpty());
        }
        super.onBeforeRender();
    }

    public class ApiKeyProvider extends SortableDataProvider<ApiKey, String> {
        private List<ApiKey> keys;

        public ApiKeyProvider(List<ApiKey> keys) {
            this.keys = keys;
        }

        @Override
        public Iterator<? extends ApiKey> iterator(long first, long count) {
            return keys.subList((int)first, (int)count).iterator();
        }

        @Override
        public long size() {
            return keys.size();
        }

        @Override
        public IModel<ApiKey> model(ApiKey object) {
            return new DetachableApiKeyModel(object);
        }
    }

    public class DetachableApiKeyModel extends LoadableDetachableModel<ApiKey> {
        private final ApiKey key;

        public DetachableApiKeyModel(ApiKey key) {
            this.key = key;
        }

        @Override
        protected ApiKey load() {
            return this.key;
        }
    }

    public class ActionsPanel extends Panel {
        public ActionsPanel(String id, ApiKey key, final MarkupIdGenerator markupIdGenerator) {
            super(id);

            WebMarkupContainer btnCopy = new WebMarkupContainer("btn_action_copy");
            btnCopy.setOutputMarkupId(true);
            btnCopy.add(new AttributeModifier("title", i18n.getProperty("btn_copy_tooltip")));
            btnCopy.add(new AttributeModifier("data-clipboard-text", key.getValue()));
            if(key.isRevoked()) {
                btnCopy.setEnabled(false);
                btnCopy.add(new AttributeModifier("disabled", "true"));
            }
            add(btnCopy);

            AjaxLink btnRevoke = new AjaxLink("btn_action_revoke") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    apiKeyService.revokeKey(key.getValue());
                    if (target != null) {
                        target.add(componentToUpdate);
                    }
                }
            };
            btnRevoke.add(new Label("btn_action_revoke_lbl", i18n.getProperty("btn_revoke_label")));
            btnRevoke.add(new AttributeModifier("title", i18n.getProperty("btn_revoke_tooltip")));
            if(key.isRevoked()) {
                btnRevoke.setEnabled(false);
                btnRevoke.add(new AttributeModifier("disabled", "true"));
            }
            add(btnRevoke);
        }
    }
}
