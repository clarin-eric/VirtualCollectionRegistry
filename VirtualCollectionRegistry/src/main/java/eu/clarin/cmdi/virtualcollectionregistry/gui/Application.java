package eu.clarin.cmdi.virtualcollectionregistry.gui;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.session.pagemap.LeastRecentlyAccessedEvictionStrategy;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.HomePage;

public class Application extends WebApplication {

    @Override
    protected void init() {
        super.init();
        getMarkupSettings().setDefaultMarkupEncoding("utf-8");
        getRequestCycleSettings().setResponseRequestEncoding("utf-8");
        getSessionSettings().setMaxPageMaps(3);
        getSessionSettings().setPageMapEvictionStrategy(
                new LeastRecentlyAccessedEvictionStrategy(3));
        if (getConfigurationType() != DEPLOYMENT) {
            getMarkupSettings().setStripWicketTags(true);
        }
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

} // class Application
