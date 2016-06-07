package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.AdminCollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

/**
 * Page that allows the admin to select a "space" (public or user private) and
 * show all collections in that space with options to edit/delete/publish
 *
 * @author twagoo
 */
@AuthorizeInstantiation(Roles.ADMIN)
@AuthorizeAction(action = "ENABLE", roles = {Roles.ADMIN})
public class AdminPage extends BasePage {

    @SpringBean
    private VirtualCollectionRegistry vc;

    public final static User PUBLIC_USER = new User("___PUBLIC___",  "Published collections");
    
    public AdminPage() {
        super();

        // user model shared between spaces form and the table's provider
        final IModel<User> userModel = new Model<>(null);

        // create form that allows admin to select a space
        final Form spaceSelectForm = new Form("spaceSelectForm");
        final DropDownChoice<User> spacesDropDown = createSpacesDropDown("space", userModel);
        spaceSelectForm.add(spacesDropDown);
        add(spaceSelectForm);

        // create table showing the collections in the space
        final AdminCollectionsProvider provider = new AdminCollectionsProvider(userModel);
        add(new BrowseEditableCollectionsPanel("collections", provider, true));
    }

    private DropDownChoice<User> createSpacesDropDown(String id, final IModel<User> userModel) {
        final IModel<List<User>> usersModel = new LoadableDetachableModel<List<User>>() {

            @Override
            protected List<User> load() {
                final List<User> users = vc.getUsers();
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
