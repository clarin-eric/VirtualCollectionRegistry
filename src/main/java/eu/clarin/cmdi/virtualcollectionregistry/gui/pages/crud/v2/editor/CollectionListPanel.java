package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.editors.ActionablePanel;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.AbstractEvent;
import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.crud.v2.editor.events.EventType;
import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class CollectionListPanel extends ActionablePanel {
    
    private final static Logger logger = LoggerFactory.getLogger(CollectionListPanel.class);
    
    public CollectionListPanel(String id, final VirtualCollection collection) {
        super(id);
        
        add(new Label("title", collection.getName()));
        add(new Label("type", collection.getType()));

        ListView authorsListview = new ListView("authors_list", collection.getCreators()) {
            @Override
            protected void populateItem(ListItem item) {
                Creator a = (Creator)item.getModel().getObject();
                WebMarkupContainer wrapper = new WebMarkupContainer("wrapper1");
                wrapper.add(new Label("name", a.getPerson()));
                wrapper.add(new Label("email", a.getEMail()));
                wrapper.add(new Label("affiliation", a.getOrganisation()));
                item.add(wrapper);
            }
        };
        add(authorsListview);

        ListView referencesListview = new ListView("references_list", collection.getResources()) {
            @Override
            protected void populateItem(ListItem item) {
                Resource r = (Resource)item.getModel().getObject();
                WebMarkupContainer wrapper = new WebMarkupContainer("wrapper2");
                ExternalLink link = new ExternalLink("ref_link", r.getRef());
                link.add(new Label("title", r.getLabel()));
                wrapper.add(link);
                wrapper.add(new Label("description", r.getDescription()));
                item.add(wrapper);
            }
        };
        add(referencesListview);
        
        add(new AjaxFallbackLink("btn_edit") {
            @Override
            public void onClick(final AjaxRequestTarget target) {
                logger.info("Fire edit event for collection with id = {}", collection.getId());
                 fireEvent(
                    new AbstractEvent<>(
                        EventType.EDIT,
                        collection, 
                        target));
            }
        }); 
        
        add(new AjaxFallbackLink("btn_remove") {
            @Override
            public void onClick(final AjaxRequestTarget target) {
                logger.info("Fire delete event for collection with id = {}", collection.getId());
                fireEvent(
                    new AbstractEvent<>(
                        EventType.DELETE, 
                        collection, 
                        target));
            }
        });  
    }
}
    