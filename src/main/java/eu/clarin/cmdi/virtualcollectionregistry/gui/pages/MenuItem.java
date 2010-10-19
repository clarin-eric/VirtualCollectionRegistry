package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class MenuItem<T extends WebPage> extends Panel {

    public MenuItem(final IModel<String> title, final Class<T> pageClass) {
        super("menuitem");
        Link<T> pageLink = new BookmarkablePageLink<T>("link", pageClass) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (pageClass.equals(getPage().getClass())) {
                    tag.setName("span");
                    tag.getAttributes().remove("href");
                    tag.getAttributes().put("class", "active");
                }
            }
            
        };
        pageLink.add(new Label("title", title));
        add(pageLink);
    }
    
} // class MenuItem
