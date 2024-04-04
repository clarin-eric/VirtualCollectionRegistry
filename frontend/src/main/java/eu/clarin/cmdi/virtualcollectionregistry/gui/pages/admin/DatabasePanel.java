package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.admin;

import eu.clarin.cmdi.virtualcollectionregistry.core.VirtualCollectionRegistry;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabasePanel extends Panel {

    private final static String LBL_DATABASE_VERSION = "Schema version:";

    private final static Logger logger = LoggerFactory.getLogger(DatabasePanel.class);

    public DatabasePanel(String id, final VirtualCollectionRegistry vcr) {
        super(id);

        String product = "";
        String productVersion = "";
        String version = "";
        String minorVersion = "";
        Connection c = null;
        try {
            InitialContext ic = new InitialContext();
            Context xmlContext = (Context) ic.lookup("java:comp/env");
            DataSource myDatasource = (DataSource) xmlContext.lookup("jdbc/VirtualCollectionStore");
            c = myDatasource.getConnection();
            product = c.getMetaData().getDatabaseProductName();
            productVersion = c.getMetaData().getDatabaseProductVersion();
            version = String.format(
                "%d.%d",
                c.getMetaData().getDatabaseMajorVersion(),
                c.getMetaData().getDatabaseMinorVersion()
            );
        } catch (NamingException | SQLException ex) {
            logger.warn("Failed to get database info.", ex);
            product = "Unkown";
            productVersion = "Unkown";
        } finally {
            if(c != null) {
                try {
                    c.close();
                } catch(SQLException ex) {
                    logger.warn("Failed to close database connection. This can cause a connection leak.");
                }
            }
        }

        add(new Label("lbl_database_product", "Product:"));
        add(new Label("database_product", product));
        add(new Label("lbl_database_product_version", "Version:"));
        add(new Label("database_product_version", productVersion));
        add(new Label("lbl_database_major_version", ""));
        add(new Label("database_major_version", version));

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
