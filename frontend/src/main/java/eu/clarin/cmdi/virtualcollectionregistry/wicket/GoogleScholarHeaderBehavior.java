/*
 * Copyright (C) 2026 CLARIN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.clarin.cmdi.virtualcollectionregistry.wicket;

import eu.clarin.cmdi.virtualcollectionregistry.gui.DateConverter;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.collection.VirtualCollection;
import java.util.Date;
import java.util.List;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.MetaDataHeaderItem;
import org.apache.wicket.model.IModel;

/**
 * Inject google scholar citation compatible meta tags into the html head.
 * 
 * References:
 * - https://scholar.google.com/intl/en/scholar/inclusion.html#indexing
 * - List of Highwire Press tags: https://webmasters.stackexchange.com/a/139313
 * 
 * @author wilelb
 */
public class GoogleScholarHeaderBehavior extends Behavior {
    
    private final IModel<VirtualCollection> model;
    
    public GoogleScholarHeaderBehavior(IModel<VirtualCollection> collectionModel) {
        this.model = collectionModel;
    }
    
    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        VirtualCollection vc = model.getObject();
        new TagBuilder(response)
            .add("citation_title", vc.getName())
            .add("citation_year", vc.getPublicationYear())
            .add("citation_date", vc.getPublicationDate())
            .add("citation_publication_date", vc.getPublicationDate())
            .add(vc.getCreators());
    }

    @Override
    public void detach(Component component) {
        super.detach(component);        
    }
    
    private class TagBuilder {
        private final IHeaderResponse response;
        
        public TagBuilder(IHeaderResponse response) {
            this.response = response;
        }
        
        public TagBuilder add(String name, String value) {
            if(value != null) {
                this.response.render(MetaDataHeaderItem.forMetaTag(name, value));
            }
            return this;
        }
        
        public TagBuilder add(String name, Integer value) {
            if(value != null) {
                this.response.render(MetaDataHeaderItem.forMetaTag(name, value.toString()));
            }
            return this;
        }
        
        public TagBuilder add(String name, Date value) {
            if(value != null) {
                this.response.render(MetaDataHeaderItem.forMetaTag(name, DateConverter.DF.format(value)));
            }
            return this;
        }
        
        public TagBuilder add(List<Creator> value) {
            for(Creator author : value) {
                new TagBuilder(this.response)
                    .add("citation_author", author.getGivenName() + " " + author.getFamilyName())
                    .add("citation_author_orcid", author.getExternalAuthorId())
                    .add("citation_author_email", author.getEMail());
            }
            return this;
        }
    }
}
