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
package eu.clarin.cmdi.wicket.components.citation;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BasePage;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.filter.FilteredHeaderItem;

/**
 *
 * @author wilelb
 */
public class CitationInitBehavior extends Behavior {
    
    
    public CitationInitBehavior() {}

    @Override
    public void renderHead(Component component, IHeaderResponse response) {        
        String js = """
            async function initCiteProc(elementId, vcId, vcCsl) {
                    
                let elId = elementId;
                if(elementId.id) {
                    elId = elementId.id;
                }   
                    
                console.log("initCiteProc elId=", elId);
                    
                const styleAsText = await fetch('/csl/styles/apa.csl').then(r => r.text());
                const localeAsText = await fetch('/csl/locales/locales-en-US.xml').then(r => r.text());

                const citeprocSys = {
                    retrieveLocale: (lang) => localeAsText,
                    retrieveItem: (id) => vcCsl
                };

                const engine = new CSL.Engine(citeprocSys, styleAsText);

                engine.updateItems([vcId]);
                const bibResult = engine.makeBibliography();

                document.getElementById(elId).innerHTML = bibResult[1].join('');
            }
         """;
        //response.render(OnDomReadyHeaderItem.forScript(js));
        
        response.render(
            new FilteredHeaderItem(JavaScriptHeaderItem.forScript(js, "initCiteProcFunction"), 
            BasePage.CSL_FILTER_NAME
        ));
    }
    
}
