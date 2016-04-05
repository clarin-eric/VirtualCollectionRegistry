package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.StringValidator;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.markup.html.form.ListMultipleChoice;

@SuppressWarnings("serial")
public class FilterForm extends Panel {
    private static final List<FilterState.SearchMode> MODE_VALUES =
        Arrays.asList(FilterState.SearchMode.values());
    private static final List<VirtualCollection.State> STATE_VALUES =
        Arrays.asList(
                VirtualCollection.State.PUBLIC,
                VirtualCollection.State.PUBLIC_FROZEN,
                VirtualCollection.State.PRIVATE,
                VirtualCollection.State.DELETED,
                VirtualCollection.State.PUBLIC_PENDING,
                VirtualCollection.State.PUBLIC_FROZEN_PENDING);
    private static final List<VirtualCollection.Type> TYPE_VALUES =
        Arrays.asList(VirtualCollection.Type.values());
    private static final List<QueryOptions.Relation> CREATED_RELATIONS =
        Arrays.asList(QueryOptions.Relation.LT,
                QueryOptions.Relation.LE,
                QueryOptions.Relation.EQ,
                QueryOptions.Relation.GE,
                QueryOptions.Relation.GT);

    public FilterForm(String id, IFilterStateLocator<FilterState> locator,
            final DataTable<VirtualCollection> table, boolean privateMode) {
        super(id);
        setRenderBodyOnly(true);

        final IModel<FilterState> model =
            new CompoundPropertyModel<FilterState>(locator.getFilterState());
        final Form<FilterState> form = new Form<FilterState>("form", model);
        form.setOutputMarkupId(true);

        final EnumChoiceRenderer<FilterState.SearchMode> searchModeRenderer =
            new EnumChoiceRenderer<FilterState.SearchMode>(this);
        form.add(new DropDownChoice<FilterState.SearchMode>("nameMode",
                MODE_VALUES, searchModeRenderer));
        form.add(new TextField<String>("name")
                .add(new StringValidator.MaximumLengthValidator(255)));
        form.add(new DropDownChoice<FilterState.SearchMode>("descriptionMode",
                MODE_VALUES, searchModeRenderer));
        form.add(new TextField<String>("description")
                .add(new StringValidator.MaximumLengthValidator(255)));
        
        final WebMarkupContainer state = new WebMarkupContainer("state");
        state.add(new ListMultipleChoice("state", STATE_VALUES));
        state.setVisible(privateMode);
        form.add(state);
        
        form.add(new DropDownChoice<VirtualCollection.Type>("type",
                TYPE_VALUES,
                new EnumChoiceRenderer<VirtualCollection.Type>(this)));
        final DropDownChoice<QueryOptions.Relation> createdRelationChoice =
            new DropDownChoice<QueryOptions.Relation>("createdRelation",
                    CREATED_RELATIONS,
                    new EnumChoiceRenderer<QueryOptions.Relation>(this));
        createdRelationChoice.setEscapeModelStrings(false);
        form.add(createdRelationChoice);
        form.add(new DateTextField("created", "yyyy-MM-dd"));

        final AjaxButton goButton = new AjaxButton("filter",
                new ResourceModel("button.filter")) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(form);
                target.addComponent(table);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.addComponent(form);
            }
        };
        form.add(goButton);
        final AjaxButton clearButton = new AjaxButton("clear",
                new ResourceModel("button.clear")) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                @SuppressWarnings("unchecked")
                final FilterState state =
                    ((Form<FilterState>) form).getModelObject();
                state.clear();
                target.addComponent(form);
                target.addComponent(table);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.addComponent(form);
            }
        };
        form.add(clearButton);
        add(form);
    }

} // class FilterForm
