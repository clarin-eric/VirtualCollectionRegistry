package eu.clarin.cmdi.virtualcollectionregistry.gui.menu;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;

@SuppressWarnings("serial")
public class AjaxLinkMenuItem<T> implements Serializable, MenuItem {
    private final IModel<String> label;
    private final IModel<T> model;
    private final String cssClass;
    private boolean visible = true;
    private boolean enabled = true;

    public AjaxLinkMenuItem(IModel<String> label, IModel<T> model,
            String cssClass) {
        this.label = label;
        this.model = model;
        this.cssClass = cssClass;
    }

    public AjaxLinkMenuItem(IModel<String> label, IModel<T> model) {
        this(label, model, null);
    }

    public AjaxLinkMenuItem(IModel<String> label, String cssClass) {
        this(label, null, cssClass);
    }

    public AjaxLinkMenuItem(IModel<String> label) {
        this(label, null, null);
    }

    @Override
    public IModel<String> getLabel() {
        return label;
    }

    @Override
    public String getCssClass() {
        return cssClass;
    }

    @Override
    public AbstractLink newLink(String componentId) {
        return new AjaxLink<T>(componentId, model) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                AjaxLinkMenuItem.this.onClick(target, getModel());
            }
        };
    }
    
    protected void onClick(AjaxRequestTarget target, IModel<T> model) {
        onClick(target);
    }

    protected void onClick(AjaxRequestTarget target) {
    }

    @Override
    public MenuItem setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public MenuItem setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

} // class AjaxLinkMenuItem
