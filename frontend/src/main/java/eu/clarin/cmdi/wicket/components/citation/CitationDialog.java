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
import eu.clarin.cmdi.wicket.components.DialogButton;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
public class CitationDialog extends BaseInfoDialog {

    private Logger logger = LoggerFactory.getLogger(CitationDialog.class);

    private final static String TITLE = "Citation information";
    
    private final IModel<Citable> model;
    private final IModel<Citable> latestModel;

    public CitationDialog(String id, final IModel<Citable> model) {
        super(id, TITLE);
        this.model = model;
        this.latestModel = null;
        this.build();
    }

    public CitationDialog(String id, final IModel<Citable> model, final IModel<Citable> latestModel) {
        super(id, TITLE);
        this.model = model;
        this.latestModel = latestModel;
        this.build();
    }

    private void build() {
        List<DialogButton> buttons = Arrays.asList(
                new DialogButton("Close") {
                    @Override
                    public void handleButtonClick(AjaxRequestTarget target) {
                        CitationDialog.this.close(target);
                    }
                });
        if(latestModel == null) {
            buildContent(TITLE, new Body(getContentWicketId(), model.getObject()), buttons, null);
        } else {
            buildContent(TITLE, new VersionsBody(getContentWicketId(), model.getObject(), latestModel.getObject()), buttons, null);
        }
    }

    private class VersionsBody extends Panel {
        public VersionsBody(String id, Citable cite, Citable latest) {
            super(id);
            add(new Body("current", cite));
            add(new Body("latest", latest));
        }
    }

    private final static String LBL_TITLE_DETAILS = "Details:";
    private final static String LBL_TITLE_BIBTEXT = "Bibtex:";
    private final static String LBL_TITLE = "Title:";
    private final static String LBL_AUTHORS = "Authors:";
    private final static String LBL_YEAR = "Year:";
    private final static String LBL_LINK = "Link:";

    private class Body extends Panel {
        public Body(String id, Citable cite) {
            super(id);

            add(new Label("title_details", Model.of(LBL_TITLE_DETAILS)));
            add(new Label("title_bibtext", Model.of(LBL_TITLE_BIBTEXT)));
            add(new Label("lbl_title", Model.of(LBL_TITLE)));
            add(new Label("lbl_authors", Model.of(LBL_AUTHORS)));
            add(new Label("lbl_year", Model.of(LBL_YEAR)));
            add(new Label("lbl_link", Model.of(LBL_LINK)));

            add(new Label("title", cite.getTitle()));
            add(new Label("authors", getAuthorsString(cite)));
            add(new Label("year", cite.getYear()));
            Label lblUri = new Label("link", cite.getUri());
            lblUri.setOutputMarkupId(true);
            add(lblUri);
            add(new Label("bibtex", getBibTexString(cite)));

            WebMarkupContainer btn = new WebMarkupContainer("btnClipboard");
            btn.add(new AttributeModifier("data-clipboard-target", "#"+lblUri.getMarkupId()));
            btn.add(new AttributeModifier("title", "Copy to clipboard..."));
            btn.add(new AttributeModifier("data-original-title", "Copied!!"));
            add(btn);
        }
        
        private String getAuthorsString(Citable cite) {
            List<String> author_list = cite.getAuthors();
            String authors = "";
            if(author_list.size() >= 1) {
                authors += author_list.get(0);
                for(int i = 1; i < author_list.size(); i++) {
                    authors += ", "+author_list.get(i);
                }
            }
            return authors;
        }
        
        private String getBibTexString(Citable cite) {
            String id ="<put your id here>";
            String bib = "";
            bib += String.format("@misc{%s,\n", id);
                bib += String.format("author = {%s},\n", getAuthorsString(cite));
                bib += String.format("title = {%s},\n", cite.getTitle());
                bib += String.format("url = {%s},\n", cite.getUri());
                bib += String.format("year = {%s}\n", cite.getYear());
            bib += String.format("}");
            return bib;
        }
        
    }
}