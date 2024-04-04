package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin;

import eu.clarin.cmdi.virtualcollectionregistry.core.pid.PersistentIdentifierProvider;
import eu.clarin.cmdi.virtualcollectionregistry.core.pid.PublicConfiguration;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class PidProviderPanel extends Panel {

    public PidProviderPanel(String id, final PersistentIdentifierProvider provider) {
        super(id);

        addLabel("provider_id", "Provider:", provider.getId());
        addLabel("provider_primary", "Primary:", String.valueOf(provider.isPrimaryProvider()));
        addLabel("provider_infix", "Infix: ", provider.getInfix());
        PublicConfiguration cfg = provider.getPublicConfiguration();
        addLabel("provider_url", "Base url:", cfg != null ? cfg.getBaseUrl() : "n/a");
        addLabel("provider_user", "User:", cfg != null ? cfg.getUsername() : "n/a");
        addLabel("provider_prefix", "Base url:", cfg != null ? cfg.getPrefix() : "n/a");
    }

    private void addLabel(String id_key, String label, String value) {
        add(new Label(id_key+"_label", label));
        add(new Label(id_key+"_value", value == null ? "n/a" : value));
    }
}
