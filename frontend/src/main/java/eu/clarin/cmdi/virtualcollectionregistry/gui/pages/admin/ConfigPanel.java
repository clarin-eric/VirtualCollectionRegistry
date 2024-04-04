package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin;

import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfig;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

public class ConfigPanel extends Panel {

    public ConfigPanel(String id, final VcrConfig config) {
        super(id);
        add(new Label("config", Model.of(config.logConfig())));
    }
}
