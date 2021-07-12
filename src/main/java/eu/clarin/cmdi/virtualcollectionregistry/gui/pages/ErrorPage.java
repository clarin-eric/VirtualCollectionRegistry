package eu.clarin.cmdi.virtualcollectionregistry.gui.pages;

import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.wicket.components.panel.EmptyPanel;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.feedback.FeedbackCollector;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Page to display errors that are not suitable to be displayed in-line on the page.
 *
 * Example:
 * getSession().error(new ErrorPage.Error("Error title", "Error body"));
 * throw new RestartResponseException(ErrorPage.class);
 */
public class ErrorPage extends BasePage {
    private final static Logger logger = LoggerFactory.getLogger(ErrorPage.class);

    private boolean mailEnabled = false;

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
        add(new MainErrorPanel("main"));
    }

    public ErrorPage(Exception ex) {
        this(null, null, ex);
    }

    public ErrorPage(String title, String message, Exception ex) {
        this.feedback.setVisible(false); //Hide default feedback panel
        add(new MainExceptionPanel("main", title, message, ex));
    }

    public class MainErrorPanel extends Panel {
        public MainErrorPanel(String id) {
            super(id);
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
            add(new EmptyPanel("error-panel"));
        }
    }

    public class MainExceptionPanel extends Panel {
        public MainExceptionPanel(String id, String title, String message, Exception ex) {
            super(id);

            final IModel mdlErrorTitle = Model.of(title == null ? "Unexpected exception" : title);
            final IModel mdlErrorBody = Model.of(message == null ? "The application has thrown an unexpected exception." : message);

            add(new Label("error-title", mdlErrorTitle));
            add(new Label("error-body", mdlErrorBody));
            add(new Label("error-contact", "Please try again and if the issue persists us the button below to contact support"));

            AjaxFallbackLink btnSupport = new AjaxFallbackLink("btn-error-support") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    logger.error("Logging error submitted in UI", ex);

                    if(mailEnabled) {
                        Properties prop = new Properties();
                        prop.put("mail.smtp.auth", true);
                        prop.put("mail.smtp.starttls.enable", "true");
                        prop.put("mail.smtp.host", "smtp.mailtrap.io");
                        prop.put("mail.smtp.port", "25");
                        prop.put("mail.smtp.ssl.trust", "smtp.mailtrap.io");

                        String username = "";
                        String password = "";
                        String fromAdress = "";
                        String toAddress = "vcr@clarin.eu";
                        String subject = "VCR unexpected exception report";

                        Session session = Session.getInstance(prop, new Authenticator() {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(username, password);
                            }
                        });

                        try {
                            Message message = new MimeMessage(session);
                            message.setFrom(new InternetAddress(fromAdress));
                            message.setRecipients(
                                    Message.RecipientType.TO, InternetAddress.parse(toAddress));
                            message.setSubject(subject);

                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            ex.printStackTrace(pw);
                            String msg = sw.toString();

                            MimeBodyPart mimeBodyPart = new MimeBodyPart();
                            mimeBodyPart.setContent(msg, "text/html");

                            Multipart multipart = new MimeMultipart();
                            multipart.addBodyPart(mimeBodyPart);

                            message.setContent(multipart);

                            Transport.send(message);
                        } catch (MessagingException ex) {
                            logger.error("Failed to send email to support", ex);
                        }
                    }
                    throw new RestartResponseException(ErrorConfirmationPage.class);
                }
            };
            btnSupport.add(new Label("btn-error-support-label", "Submit issue to support"));
            add(btnSupport);

            //Show full exception details when not running in production mode
            if(!Application.get().getConfig().isProductionMode()) {
                add(new ExceptionList("error-panel", ex));
            } else {
                add(new EmptyPanel("error-panel"));
            }
        }
    }

    public class ExceptionList extends Panel {
        public ExceptionList(String id, Exception ex) {
            super(id);

            List<Throwable> throwables = new LinkedList<>();
            Throwable current = ex;
            throwables.add(current);
            while(current.getCause() != null) {
                throwables.add(current.getCause());
                current = current.getCause();
            }

            add(new Label("error-panel-title", "Details:"));
            ListView<Throwable> list = new ListView("error-list", throwables) {
                private int index = 0;
                @Override
                protected void populateItem(ListItem item) {
                    Throwable val = (Throwable)item.getModel().getObject();
                    item.add(new ExceptionPanel("error-panel", val, index));
                    index++;
                }
            };
            add(list);
        }
    }

    public class ExceptionPanel extends Panel {
        public ExceptionPanel(String id, Throwable ex, int index) {
            super(id);
            add(new Label("ex_message", String.format("%s%s", index > 0 ? "  Caused by: ": "", ex.getMessage())));

            List<StackTraceElement> stacktrace = Arrays.asList(ex.getStackTrace());
            ListView<StackTraceElement> list = new ListView("ex_list", stacktrace) {
                @Override
                protected void populateItem(ListItem item) {
                    StackTraceElement el = (StackTraceElement)item.getModel().getObject();
                    String val = String.format("at %s.%s(%s:%s)",
                            el.getClassName(), el.getMethodName(), el.getFileName(), el.getLineNumber());
                    item.add(new Label("ex_item", Model.of(val)));
                }
            };
            add(list);
        }
    }
}
