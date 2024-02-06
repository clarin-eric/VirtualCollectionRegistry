package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.StringHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;

public class ErrorConfirmationPage extends BasePage {

    private final IModel<String> mdlConfirmation = Model.of("Thank you for the submision");

    public ErrorConfirmationPage() {
        add(new RedirectBehavior(BrowsePublicCollectionsPage.class, 5));
        add(new Label("message", mdlConfirmation));
 }

    public class RedirectBehavior extends Behavior {

        private final Class<? extends Page> page;
        private final int redirectInSeconds;

        public RedirectBehavior(Class<? extends Page> page, int redirectInSeconds) {
            this.page = page;
            this.redirectInSeconds = redirectInSeconds;
        }

        @Override
        public void renderHead(Component component, IHeaderResponse response) {
            response.render(
                StringHeaderItem.forString(
                    String.format("<meta http-equiv='refresh' content='%d;URL=%s' />",
                        redirectInSeconds,
                        RequestCycle.get().urlFor(page, null)
                    )
                )
            );
        }
    }
}
