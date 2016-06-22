package eu.clarin.cmdi.virtualcollectionregistry.gui.wizard;

import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.gui.DynamicProxyModel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import eu.clarin.cmdi.virtualcollectionregistry.gui.dialog.ModalEditDialogBase;

@SuppressWarnings("serial")
public abstract class AddKeywordDialog extends ModalEditDialogBase<String> {

    private final class Content extends
            ModalEditDialogBase<String>.ContentPanel {

        private final Form<String> form;
        private final FeedbackPanel feedbackPanel;

        public Content(String id, IModel<String> model) {
            super(id);
            form = new Form<>("addKeywordForm", model);
            final TextField<String> keywordField
                    = new RequiredTextField<>("keyword", new DynamicProxyModel<String>() {

                @Override
                protected IModel<String> getWrappedModel() {
                    return form.getModel();
                }
            });
            keywordField.add(Application.MAX_LENGTH_VALIDATOR);
            form.add(keywordField);
            /*
            WebMarkupContainer tooltip = new WebMarkupContainer("tooltip");
            final Label tooltipText  = new Label("tooltip-text", new Model<String>("Tooltip message"));
            tooltipText.setOutputMarkupId(true);
            tooltipText.setOutputMarkupPlaceholderTag(true);
            final AjaxLink<Creator> toggleLink
                = new AjaxLink<Creator>("show-tooltip") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        tooltipText.setVisible(!tooltipText.isVisible());
                        target.add(tooltipText);
                    }
                };
            
            
            tooltipText.setVisible(false);
            
            tooltip.add(tooltipText);  
            tooltip.add(toggleLink);
            form.add(tooltip);
            */
            
            feedbackPanel = new FeedbackPanel("feedback");
            form.add(feedbackPanel);
            add(form);
            
            
        }

        @Override
        public Form<String> getForm() {
            return form;
        }

        @Override
        public FeedbackPanel getFeedbackPanel() {
            return feedbackPanel;
        }
    } // class EditCreatorDialog.Content

    public AddKeywordDialog(final String id) {
        super(id, new Model<String>("Add Keyword"));
        setInitialWidth(400);
    }

    @Override
    protected ModalEditDialogBase<String>.ContentPanel
            createContentPanel(String id, IModel<String> model) {
        return new Content(id, model);
    }

    @Override
    protected final IModel<String> newInstanceModel() {
        return Model.of("");
    }

    @Override
    protected final IModel<String> createEmptyModel() {
        return new Model<String>();
    }

    @Override
    protected String getCssClass() {
        return "addKeywordDialog";
    }

} // class AddKeywordDialog
