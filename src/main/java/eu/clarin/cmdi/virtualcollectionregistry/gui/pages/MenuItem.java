package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeAction;

@SuppressWarnings("serial")
public class MenuItem<T extends WebPage> extends Panel {

    private static final String ENABLE = "ENABLE";
    private final Class<T> pageClass;
    
    public MenuItem(final IModel<String> title, final Class<T> pageClass) {
        super("menuitem");
        this.pageClass = pageClass;
        final Link<T> link = new BookmarkablePageLink<T>("link", pageClass)
                .setAutoEnable(false);
        link.add(new Label("title", title).setRenderBodyOnly(true));
        add(link);

        add(new AttributeModifier("class", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                if (pageClass.equals(getPage().getClass())) {
                    return "active";
                } else {
                    return null;
                }
            }
        }));
    }

    @Override
    protected void onBeforeRender() {
        final IAuthorizationStrategy strategy
                = getApplication().getSecuritySettings()
                .getAuthorizationStrategy();
        if (!strategy.isInstantiationAuthorized(this.pageClass)) {
            boolean visible = true;
            AuthorizeAction action
                    = pageClass.getAnnotation(AuthorizeAction.class);
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

} // class MenuItem
