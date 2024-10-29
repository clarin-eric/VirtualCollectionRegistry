package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin;

import eu.clarin.cmdi.virtualcollectionregistry.core.AdminUsersService;
import eu.clarin.cmdi.virtualcollectionregistry.core.PidProviderService;
import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.core.pid.PersistentIdentifierProvider;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BasePage;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BrowseEditableCollectionsPanel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.TimerManager;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.AdminCollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.User;
import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfig;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Page that allows the admin to select a "space" (public or user private) and
 * show all collections in that space with options to edit/delete/publish
 *
 * @author twagoo
 */
@AuthorizeInstantiation(Roles.ADMIN)
@AuthorizeAction(action = "ENABLE", roles = {Roles.ADMIN})
public class AdminPage extends BasePage {

    private final static Logger logger = LoggerFactory.getLogger(AdminPage.class);

    @SpringBean
    private VirtualCollectionRegistry vcr;

    @SpringBean
    private AdminUsersService adminUsersService;

    @SpringBean
    private PidProviderService pidProviderService;

    @SpringBean
    private VcrConfig vcrConfig;

    public final static User PUBLIC_USER = new User("___PUBLIC___",  "Published collections");
    
    public AdminPage() {
        super();

        final List<User> users = vcr.getUsers();

        add(new Label("lbl_server_config", Model.of("Server Configuration")));
        add(new AdminPanel("pnl_admins", adminUsersService));

        ListView pidProvidersListview = new ListView("pid_list", pidProviderService.getProviders()) {
            @Override
            protected void populateItem(ListItem item) {
                PersistentIdentifierProvider provider = (PersistentIdentifierProvider)item.getModel().getObject();
                item.add(new PidProviderPanel("pid_list_item", provider));
            }
        };
        add(pidProvidersListview);

        add(new Label("lbl_pnl_database", Model.of("Database")));
        add(new DatabasePanel("pnl_database", vcr));
        add(new ConfigPanel("pnl_config", vcrConfig));
        add(new FeatureFlagPanel("pnl_feature_flags", vcrConfig));
        // user model shared between spaces form and the table's provider
        final IModel<User> userModel = new Model<>(null);

        // create form that allows admin to select a space
        final Form spaceSelectForm = new Form("spaceSelectForm");
        final DropDownChoice<User> spacesDropDown = createSpacesDropDown("space", userModel, users);
        spaceSelectForm.add(spacesDropDown);
        add(spaceSelectForm);

        // create table showing the collections in the space
        final AdminCollectionsProvider provider = new AdminCollectionsProvider(userModel);
        add(new BrowseEditableCollectionsPanel("collections", provider, true, getPageReference(), timerManager));

        ReferenceValidationPanel pnl = new ReferenceValidationPanel("pnl_reference_validation", getScans());
        pnl.setOutputMarkupId(true);
        add(pnl);

        timerManager.addTarget(null, new TimerManager.Update() {
            @Override
            public boolean onUpdate(AjaxRequestTarget target) {
                pnl.update(getScans());
                return true;
            }

            @Override
            public List<Component> getComponents() {
                List<Component> result = new ArrayList<>();
                result.add(pnl);
                return result;
            }
        });
    }

    private List<ResourceScan> getScans() {
        List<ResourceScan> scans = new ArrayList<>();
        try {
            scans = vcr.getAllResourceScans();
        } catch(Exception ex) {
            logger.error("Failed to fetch resource scans", ex);
        }
        return scans;
    }

    private DropDownChoice<User> createSpacesDropDown(String id, final IModel<User> userModel, final List<User> users) {
        final IModel<List<User>> usersModel = new LoadableDetachableModel<List<User>>() {
            @Override
            protected List<User> load() {
                final List<User> spaces = new ArrayList<>(users.size() + 1);
                spaces.add(PUBLIC_USER);
                spaces.addAll(users);
                return spaces;
            }
        };
        final IChoiceRenderer<User> choiceRenderer = new IChoiceRenderer<User>() {

            @Override
            public Object getDisplayValue(User user) {
                final String displayName = user.getDisplayName();
                if (displayName == null) {
                    return user.getName();
                } else {
                    return displayName;
                }
            }

            @Override
            public String getIdValue(User user, int index) {
                return user.getName();
            }

            @Override
            public User getObject(String id, IModel<? extends List<? extends User>> choices) {
                for(User user : choices.getObject()) {
                    if(user.getName().equals(id)) {
                        return user;
                    }
                }
                throw new IllegalStateException("User ["+id+"] not found in list of choices.");
            }

        };
        //final DropDownChoice<User> spacesDropDown = new DropDownChoice<>(id, userModel, usersModel, choiceRenderer);
        //spacesDropDown.setNullValid(true);
        return new DropDownChoice<>(id, userModel, usersModel, choiceRenderer);
    }

} // class AdminPage
