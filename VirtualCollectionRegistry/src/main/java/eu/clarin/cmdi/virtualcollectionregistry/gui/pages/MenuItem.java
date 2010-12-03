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
import org.apache.wicket.util.lang.Classes;

import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;

@SuppressWarnings("serial")
public class MenuItem<T extends WebPage> extends Panel {
    private static final String ENABLE = "ENABLE";
    private final String pageClassName;

    public MenuItem(final IModel<String> title, final Class<T> pageClass) {
        super("menuitem");
        this.pageClassName = pageClass.getName();
        setRenderBodyOnly(true);
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
        };
        link.add(new Label("title", title).setRenderBodyOnly(true));
        add(link);
    }

    @Override
    protected void onBeforeRender() {
        final IAuthorizationStrategy strategy =
            getApplication().getSecuritySettings()
                .getAuthorizationStrategy();
        final Class<T> pageClass = getPageClass();
        if (!strategy.isInstantiationAuthorized(pageClass)) {
            boolean visible = true;
            AuthorizeAction action =
                pageClass.getAnnotation(AuthorizeAction.class);
            if ((action != null) && ENABLE.equalsIgnoreCase(action.action())) {
                final Application app = (Application) getApplication();
                if (app.hasAnyRole(action.deny())) {
                    visible = false;
                } else {
                    visible = app.hasAnyRole(action.roles());
                }
            }
            setVisible(visible);
        }
        super.onBeforeRender();
    }

    private Class<T> getPageClass() {
        return Classes.resolveClass(pageClassName);
    }

} // class MenuItem