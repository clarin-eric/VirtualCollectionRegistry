package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import eu.clarin.cmdi.virtualcollectionregistry.QueryOptions;

@SuppressWarnings("serial")
final class CreatedDateFilter extends Panel {
    private static final List<QueryOptions.Relation> RELATION_VALUES =
        Arrays.asList(QueryOptions.Relation.LT,
                      QueryOptions.Relation.LE,
                      QueryOptions.Relation.EQ,
                      QueryOptions.Relation.GE,
                      QueryOptions.Relation.GT);
    private final DropDownChoice<QueryOptions.Relation> relation;
    private final DateTextField created;
    
    public CreatedDateFilter(String id, IModel<FilterState> state, FilterForm<?> form) {
        super(id);
        relation = new DropDownChoice<QueryOptions.Relation>("relation",
                new PropertyModel<QueryOptions.Relation>(state,
                        "createdRelation"), RELATION_VALUES,
                        new EnumChoiceRenderer<QueryOptions.Relation>(this));
        relation.setEscapeModelStrings(false);
        add(relation);
        created = DateTextField.forDatePattern("created",
                new PropertyModel<Date>(state, "created"), "yyyy-MM-dd");
        add(created);
    }

} // class DateFilter
