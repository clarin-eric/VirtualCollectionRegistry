package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.gui.table.AdminCollectionsProvider;
import eu.clarin.cmdi.virtualcollectionregistry.model.User;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
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
        add(new BrowseEditableCollectionsPanel("collections", provider));
    }

    private DropDownChoice<User> createSpacesDropDown(String id, final IModel<User> userModel) {
        final IModel<List<User>> usersModel = new LoadableDetachableModel<List<User>>() {

            @Override
            protected List<User> load() {
                final List<User> users = vc.getUsers();

                // merge with a 'null' entry to represent the public space
                final List<User> spaces = new ArrayList<>(users.size() + 1);
                spaces.add(null);
                spaces.addAll(users);
                return spaces;
            }
        };
        final IChoiceRenderer<User> choiceRenderer = new IChoiceRenderer<User>() {

            @Override
            public Object getDisplayValue(User user) {
                if (user == null) {
                    return "Published profiles";
                } else {
                    final String displayName = user.getDisplayName();
                    if (displayName == null) {
                        return user.getName();
                    } else {
                        return displayName;
                    }
                }
            }

            @Override
            public String getIdValue(User user, int index) {
                if (user == null) {
                    return "___PUBLIC___";
                } else {
                    return user.getName();
                }
            }
        };
        final DropDownChoice<User> spacesDropDown = new DropDownChoice<>(id, userModel, usersModel, choiceRenderer);
        spacesDropDown.setNullValid(true);
        return spacesDropDown;
    }

} // class AdminPage
