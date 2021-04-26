package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabasePanel extends Panel {

    private final static String LBL_DATABASE_VERSION = "Version:";

    private final static Logger logger = LoggerFactory.getLogger(DatabasePanel.class);

    public DatabasePanel(String id, final VirtualCollectionRegistry vcr) {
        super(id);

        String dbVersion = "";
        try {
            dbVersion = vcr.getDbVersion();
            if(dbVersion == null) {
                dbVersion = "No database version found";
            }
        } catch(Exception ex) {
            logger.error("Failed to read database version from database.", ex);
            dbVersion = "Failed to read database version from database";
        }

        add(new Label("lbl_database_version", Model.of(LBL_DATABASE_VERSION)));
        add(new Label("database_version", Model.of(dbVersion)));
    }
}
