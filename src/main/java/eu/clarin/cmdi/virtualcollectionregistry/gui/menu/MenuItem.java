package eu.clarin.cmdi.virtualcollectionregistry.gui.menu;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

public interface MenuItem {

    public IModel<String> getLabel();

    public String getCssClass();

    public AbstractLink newLink(String componentId);

    public MenuItem setVisible(boolean visible);

    public boolean isVisible();

    public MenuItem setEnabled(boolean enabled);

    public boolean isEnabled();

} // interface MenuItem
