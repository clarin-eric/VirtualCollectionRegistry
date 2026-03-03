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

import eu.clarin.cmdi.virtualcollectionregistry.model.citation.Citable;
import eu.clarin.cmdi.wicket.components.BootstrapDialog;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class CitationDialog extends BootstrapDialog {

    private Logger logger = LoggerFactory.getLogger(CitationDialog.class);

    private final static String TITLE = "Citation information";

    private final AbstractBody body;
    
    public CitationDialog(String id, final IModel<Citable> model) {
        this(id, model, null);
    }

    public CitationDialog(String id, final IModel<Citable> model, final IModel<Citable> latestModel) {
        super(id);
        header(Model.of(TITLE));

        if(latestModel == null) {
            body = new Body(BootstrapDialog.CONTENT_PANEL_ID, model.getObject());
        } else {
            body = new VersionsBody(id, model.getObject(), latestModel.getObject());
        }
        add(body);
    }
    
    public String getCslTargetId() {
        return body.getCslTargetId();
    }

    public String getCslSelectTargetId() {
        return body.getCslSelectTargetId();
    }
    
    private abstract class AbstractBody extends Panel {
        public AbstractBody(String id) {
            super(id);
        }
        
        public abstract String getCslTargetId();
        public abstract String getCslSelectTargetId();
    }
    
    private class VersionsBody extends AbstractBody {
        public VersionsBody(String id, Citable cite, Citable latest) {
            super(id);
            add(new Body("current", cite));
            add(new Body("latest", latest));
        }
        
        @Override
        public String getCslTargetId() {
            return "";
        }
        
        @Override
        public String getCslSelectTargetId() {
            return "";
        }
    }

    private class Body extends AbstractBody {
        private final Label lblCsl;
        private final DropDownChoice<Style> styleList;
        
        private final static String defaultCitationStyle = "APA";
        
        public Body(String id, Citable cite) {
            super(id);

            add(new Label("title", cite.getTitle()));
            add(new Label("authors", getAuthorsString(cite)));
            add(new Label("year", cite.getYear()));
            Label lblUri = new Label("link", cite.getUri());
            lblUri.setOutputMarkupId(true);
            add(lblUri);
            
            lblCsl = new Label("bibtex", Model.of(""));
            lblCsl.setOutputMarkupId(true);
            add(lblCsl);
            
            ChoiceRenderer<Style> renderer = new ChoiceRenderer<>("name", "name");
            styleList = new DropDownChoice<>(
                "csl-style-list", 
                Model.of(new Style(defaultCitationStyle)), 
                Arrays.asList(new Style("APA"), new Style("IEEE"), new Style("BibTex")),
                renderer
            );
            styleList.setOutputMarkupId(true);
            add(styleList);
            
            add(new CopyToClipboardButton("btnCopyLinkToClipBoard",lblUri.getMarkupId(),  "Copy link to clipboard", CopyToClipboardButton.TOOLTIP_TOP));
            add(new CopyToClipboardButton("btnCopyCitationToClipBoard", lblCsl.getMarkupId() ,"Copy citation to clipboard", CopyToClipboardButton.TOOLTIP_TOP));
        }
        
        @Override
        public String getCslTargetId() {
            return lblCsl.getMarkupId();
        }
        
        @Override
        public String getCslSelectTargetId() {
            return styleList.getMarkupId();
        }
        
        private String getAuthorsString(Citable cite) {
            List<String> author_list = cite.getAuthorNames();
            String authors = "";
            if(author_list.size() >= 1) {
                authors += author_list.get(0);
                for(int i = 1; i < author_list.size(); i++) {
                    authors += ", "+author_list.get(i);
                }
            }
            return authors;
        }
        
        public class Style implements Serializable {
            private String name;
            
            public Style(String name) {
                this.name = name;
            }

            /**
             * @return the name
             */
            public String getName() {
                return name;
            }

            /**
             * @param name the name to set
             */
            public void setName(String name) {
                this.name = name;
            }
        }
    }
}