package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Page to display errors that are not suitable to be displayed in-line on the page.
 *
 * Example:
 * getSession().error(new ErrorPage.Error("Error title", "Error body"));
 * throw new RestartResponseException(ErrorPage.class);
 */
public class ErrorPage extends BasePage {
    private final static Logger logger = LoggerFactory.getLogger(ErrorPage.class);

    public static class Error implements Serializable {
        private final String title;
        private final String body;

        public Error(String title, String body) {
            this.title=title;
            this.body=body;
        }
    }

    public ErrorPage() {
        this.feedback.setVisible(false); //Hide default feedback panel

        Error err = null;
        for(FeedbackMessage msg : new FeedbackCollector(this).collect()) {
            if(msg.getMessage() instanceof Error) {
                err = (Error) msg.getMessage();
            }
        }

        final IModel mdlErrorTitle = Model.of("Unkown error reported");
        final IModel mdlErrorBody = Model.of("No valid error object found.");
        if (err != null) {
            mdlErrorTitle.setObject(err.title);
            mdlErrorBody.setObject(err.body);
        } else {
            logger.warn("Unknown error reported.");
        }
        add(new Label("error-title", mdlErrorTitle));
        add(new Label("error-body", mdlErrorBody));
    }
}
