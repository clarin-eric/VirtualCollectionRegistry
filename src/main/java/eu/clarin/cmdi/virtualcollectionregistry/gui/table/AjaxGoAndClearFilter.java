package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

@SuppressWarnings("serial")
final class AjaxGoAndClearFilter extends Panel {

    public AjaxGoAndClearFilter(String id) {
        super(id);
        final AjaxButton goButton = new AjaxButton("filter",
                new ResourceModel("button.filter")) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.addComponent(form);
            }
        };
        add(goButton);
        final AjaxButton clearButton = new AjaxButton("clear",
                new ResourceModel("button.clear")) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                @SuppressWarnings("unchecked")
                final FilterState state =
                    ((Form<FilterState>) form).getModelObject();
                state.clear();
                target.addComponent(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.addComponent(form);
            }
        };
        add(clearButton);
    }

} // class AjaxGoAndClearFilter
