package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;
import eu.clarin.cmdi.virtualcollectionregistry.gui.Application;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import java.util.ArrayList;
import org.apache.wicket.markup.html.basic.Label;

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

    public FilterForm(String id, IFilterStateLocator<FilterState> locator, List<String> originValues,
            final DataTable<VirtualCollection, String> table, boolean privateMode, boolean isAdmin) {
        super(id);
        //setRenderBodyOnly(true);

        WebMarkupContainer btnToggle = new WebMarkupContainer("btn-toggle-filter");
        btnToggle.setOutputMarkupId(true);
        if(locator.getFilterState().isCleared()) {
            btnToggle.add(new AttributeModifier("class", "btn btn-default toggle-filter"));
        } else {
            btnToggle.add(new AttributeModifier("class", "btn btn-primary"));
        }
        add(btnToggle);

        List<VirtualCollection.State> states = new ArrayList<>();
        states.addAll(STATE_VALUES);
        if(isAdmin) { //Admins should be able to filter on VCs in error
            states.add(VirtualCollection.State.ERROR);
        }
        
        final IModel<FilterState> model =
            new CompoundPropertyModel<FilterState>(locator.getFilterState());
        final Form<FilterState> form = new Form<FilterState>("form", model);
        form.setOutputMarkupId(true);

        //Name
        final WebMarkupContainer containerName = new WebMarkupContainer("c_name");
        containerName.add(new Label("lbl-name", "Name"));
        final EnumChoiceRenderer<FilterState.SearchMode> searchModeRenderer =
            new EnumChoiceRenderer<FilterState.SearchMode>(this);
        containerName.add(new DropDownChoice<FilterState.SearchMode>("nameMode",
                MODE_VALUES, searchModeRenderer));
        containerName.add(new TextField<String>("name")
                .add(Application.MAX_LENGTH_VALIDATOR));
        form.add(containerName);

        //Description
        final WebMarkupContainer containerDescription = new WebMarkupContainer("c_description");
        containerDescription.add(new Label("lbl-description", "Description"));
        containerDescription.add(new DropDownChoice<FilterState.SearchMode>("descriptionMode",
                MODE_VALUES, searchModeRenderer));
        containerDescription.add(new TextField<String>("description")
                .add(Application.MAX_LENGTH_VALIDATOR));
        form.add(containerDescription);

        //State
        final WebMarkupContainer containerState = new WebMarkupContainer("state");
        containerState.add(new Label("lbl-state", "State"));
        containerState.setRenderBodyOnly(true);
        containerState.add(new ListMultipleChoice("state", states));
        containerState.setVisible(privateMode);
        form.add(containerState);

        //Type
        WebMarkupContainer containerType = new WebMarkupContainer("c_type");
        containerType.add(new Label("lbl-type", "Type"));
        containerType.add(new DropDownChoice<VirtualCollection.Type>("type",
                TYPE_VALUES,
                new EnumChoiceRenderer<VirtualCollection.Type>(this)));
        form.add(containerType);

        //Created
        WebMarkupContainer containerCreated = new WebMarkupContainer("c_created");
        containerCreated.add(new Label("lbl-created", "Created"));
        final DropDownChoice<QueryOptions.Relation> createdRelationChoice =
            new DropDownChoice<QueryOptions.Relation>("createdRelation",
                    CREATED_RELATIONS,
                    new EnumChoiceRenderer<QueryOptions.Relation>(this));
        createdRelationChoice.setEscapeModelStrings(false);
        containerCreated.add(createdRelationChoice);
        containerCreated.add(new DateTextField("created", "yyyy-MM-dd"));
        form.add(containerCreated);

        //Modified
        WebMarkupContainer containerModified = new WebMarkupContainer("c_modified");
        containerModified.add(new Label("lbl-modified", "Modified"));
        final DropDownChoice<QueryOptions.Relation> modifiedRelationChoice =
                new DropDownChoice<QueryOptions.Relation>("modifiedRelation",
                        CREATED_RELATIONS,
                        new EnumChoiceRenderer<QueryOptions.Relation>(this));
        modifiedRelationChoice.setEscapeModelStrings(false);
        containerModified.add(modifiedRelationChoice);
        containerModified.add(new DateTextField("modified", "yyyy-MM-dd"));
        form.add(containerModified);

        //Origin
        WebMarkupContainer containerOrigin = new WebMarkupContainer("origin");
        containerOrigin.add(new Label("lbl-origin", "Origin"));
        containerOrigin.add(new DropDownChoice<String>("input-origin", originValues, new ChoiceRenderer<String>() {
            @Override
            public Object getDisplayValue(String value)
            {
                return value;
            }
        }));
        containerOrigin.setVisible(!originValues.isEmpty());
        form.add(containerOrigin);

        final AjaxButton goButton = new AjaxButton("filter",
                new ResourceModel("button.filter")) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
                target.add(table);
                target.add(btnToggle);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.add(form);
                target.add(btnToggle);
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
                target.add(form);
                target.add(table);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);
                target.add(form);
            }
        };
        form.add(clearButton);
        add(form);
        //form.add(new Label("title", "Filter"));
        /*
        TODO: switch to using BootstrapPanelBuilder:
        add(BootstrapPanelBuilder
                .createCollapsiblePanel("general")
                .setTitle("General")
                .setBody(new GeneralPanel("body", model))
                .build());
        */
    }

} // class FilterForm
