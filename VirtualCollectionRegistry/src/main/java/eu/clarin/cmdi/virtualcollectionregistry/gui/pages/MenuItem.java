package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;

@SuppressWarnings("serial")
public class MenuItem<T extends WebPage> extends Panel {

    public MenuItem(final IModel<String> title, final Class<T> pageClass) {
        super("menuitem");
        final Link<T> link = new BookmarkablePageLink<T>("link", pageClass) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (linksTo(getPage())) {
                    tag.setName("span");
                    tag.getAttributes().remove("href");
                    tag.getAttributes().put("class", "active");
                }
            }

            @Override
            public boolean isVisible() {
                boolean visible = true;
                final IAuthorizationStrategy strategy =
                    getApplication().getSecuritySettings()
                        .getAuthorizationStrategy();
                if (!strategy.isInstantiationAuthorized(pageClass)) {
                    AuthorizeAction action =
                        pageClass.getAnnotation(AuthorizeAction.class);
                    if ((action != null) && "ENABLE".equals(action.action())) {
                        final Application app = (Application) getApplication();
                        if (app.hasAnyRole(action.deny())) {
                            visible = false;
                        } else {
                            visible = app.hasAnyRole(action.roles());
                        }
                    }
                }
                return visible;
            } 
        };
        link.add(new Label("title", title));
        add(link);
    }

} // class MenuItem
