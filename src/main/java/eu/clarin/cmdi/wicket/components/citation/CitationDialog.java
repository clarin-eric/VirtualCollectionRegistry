/*
 * Copyright (C) 2018 CLARIN
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
package eu.clarin.cmdi.wicket.components.citation;

import eu.clarin.cmdi.wicket.components.BaseInfoDialog;
import java.util.List;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author wilelb
 */
public class CitationDialog extends BaseInfoDialog {
    
    private final static String TITLE = "Citation information";
    
    private final IModel<Citable> model;
    
    public CitationDialog(String id, final IModel<Citable> model) {
        super(id, TITLE);
        this.model = model;
        this.build();
    }
    
    private void build() {
        buildContent(TITLE, new Body(getContentWicketId()));
    }
    
    private class Body extends Panel {
        public Body(String id) {
            super(id);
            Citable cite = model.getObject();
            add(new Label("title", cite.getTitle()));
            add(new Label("authors", getAuthorsString(cite)));
            add(new Label("year", cite.getYear()));
            add(new Label("link", cite.getUri()));
            add(new Label("bibtex", getBibTexString(cite)));
        }
        
        private String getAuthorsString(Citable cite) {
            List<String> author_list = cite.getAuthors();
            String authors = "";
            if(author_list.size() >= 1) {
                authors += author_list.get(0);
                for(int i = 1; i < author_list.size(); i++) {
                    authors += ", "+author_list.get(0);
                }
            }
            return authors;
        }
        
        private String getBibTexString(Citable cite) {
            String bib = "";
            bib += String.format("@misc{Rub1,\n"); //TODO: how to generate this id?
                bib += String.format("author = {%s},\n", getAuthorsString(cite));
                bib += String.format("title = {%s},\n", cite.getTitle());
                bib += String.format("url = {%s},\n", cite.getUri());
                bib += String.format("year = {%s}\n", cite.getYear());
            bib += String.format("}");
            return bib;
        }
        
    }
    
}
