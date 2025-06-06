package eu.clarin.cmdi.virtualcollectionregistry.gui.table;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.VirtualCollectionDetailsPage;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.Model;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("serial")
final class ColumnName extends AbstractColumn<VirtualCollection, String> {
    
    private final VirtualCollectionTable table;

    private final Map<Long, Boolean> collapsedState= new HashMap<>();
    
    private final class ItemCell extends Panel {
        private final WebMarkupContainer nameColumn;

        public ItemCell(String id, IModel<VirtualCollection> model) {
            super(id);

            final VirtualCollection vc = model.getObject();
            if(!collapsedState.containsKey(vc.getId())) {
                collapsedState.put(vc.getId(), false);
            }

            setRenderBodyOnly(true);

            nameColumn = new WebMarkupContainer("nameColumn");
            nameColumn.setOutputMarkupId(true);

            final WebMarkupContainer details = new WebMarkupContainer("details");
            details.setOutputMarkupId(true);
            if(!collapsedState.get(vc.getId())) {
                details.add(new AttributeModifier("class", Model.of("details col-xs-12 collapse")));
            } else {
                details.add(new AttributeModifier("class", Model.of("details col-xs-12")));
            }
            WebMarkupContainer problems = new WebMarkupContainer("problems");
            Label lblProblems = new Label("lbl_problems", Model.of(vc.getProblemDetails() == null ? "Errors: No details available" : "Errors: "+vc.getProblemDetails()));
            problems.setVisible(vc.getState() == VirtualCollection.State.ERROR);
            problems.add(lblProblems);
            details.add(problems);

            final String desc = vc.getDescription();
            String htmlValue = "";
            if(desc != null) {
                MutableDataSet options = new MutableDataSet();
                Parser parser = Parser.builder(options).build();
                HtmlRenderer renderer = HtmlRenderer.builder(options).build();
                Node document = parser.parse(desc);
                htmlValue = renderer.render(document);
            }
            final MultiLineLabel descLabel = new MultiLineLabel("desc", htmlValue);
            descLabel.setEscapeModelStrings(false);
            if (desc == null) {
                descLabel.setVisible(false);
            }
            details.add(descLabel);
            
            AjaxFallbackLink<AjaxRequestTarget> toggleLink = new AjaxFallbackLink<>("toggle-link") {
                @Override
                public void onClick(Optional<AjaxRequestTarget> target) {
                    collapsedState.put(vc.getId(), !collapsedState.get(vc.getId()));
                    if(target.get() != null) {
                        target.get().add(table);
                    }
                }
            };
            if(!collapsedState.get(vc.getId())) {
                toggleLink.add(new AttributeModifier("class", Model.of("hover collapsed")));
            } else {
                toggleLink.add(new AttributeModifier("class", Model.of("hover")));
            }

            AjaxLink citeButton = new AjaxLink( "name", new Model<String>("") ){ 
            @Override
            public void onClick( AjaxRequestTarget target ) {
                if(vc.getState() != VirtualCollection.State.DELETED) {
                    setResponsePage(
                        VirtualCollectionDetailsPage.class, 
                        VirtualCollectionDetailsPage.createPageParameters(
                            vc, 
                            table.getPageReference(), 
                            VirtualCollectionDetailsPage.BackPage.PUBLIC_LISTING));
                    } 
                }
            };
            citeButton.add(new Label("label", vc.getName()));
            
            nameColumn.add(toggleLink);
            nameColumn.add(citeButton);            
            nameColumn.add(details);
            add(nameColumn);
        }
    } // class ColumnName.ItemCell

    ColumnName(VirtualCollectionTable table) {
        super(new ResourceModel("column.name", "Name"), "name");
        this.table = table;
        this.table.setOutputMarkupId(true);
    }

    @Override
    public void populateItem(Item<ICellPopulator<VirtualCollection>> item,
            String componentId, IModel<VirtualCollection> model) {
        item.add(new ItemCell(componentId, model));
    }

    @Override
    public String getCssClass() {
        return "name";
    }
} // class ColumnName
