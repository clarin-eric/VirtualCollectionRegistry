/*
 * Copyright (C) 2024 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin;

import eu.clarin.cmdi.virtualcollectionregistry.model.config.VcrConfig;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 *
 * @author wilelb
 */
public class FeatureFlagPanel extends Panel {

    public FeatureFlagPanel(String id, final VcrConfig config) {
        super(id);
        
        addLabel("download_id",                 "Download enabled:", config.isDownloadEnabledForCollections());
        addLabel("forking_id",                  "Forking enabled:", config.isForkingEnabled());
        addLabel("process_popup_id",            "Processing popup enabled:", config.isProcessPopupEnabled());
        addLabel("process_collections_id",      "Process collections:", config.isProcessEnabledForCollections());
        addLabel("process_resources_id",        "Process resources:", config.isProcessEnabledForResources());
        addLabel("reference_examples_id",       "Reference examples enable:", config.isReferenceExamplesEnabled());
        addLabel("http_reference_scanning_id",  "HTTP reference scanning enabled:", config.isHttpReferenceScanningEnabled());
        addLabel("logout_id",                   "Logout enabled :", config.isLogoutEnabled());
    }

    private void addLabel(String id_key, String label, Boolean value) {
        add(new Label(id_key+"_label", label));
        add(new Label(id_key+"_value", value == null ? "n/a" : value.toString()));
    }
    
}
