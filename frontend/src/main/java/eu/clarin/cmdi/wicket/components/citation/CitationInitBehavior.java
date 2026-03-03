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
            const cslStyle = {
                "apa": {"file": "apa.csl"},
                "ieee": {"file": "ieee.csl"},
                "bibtex": {"file": "bibtex.csl"}
            }
                    
            let elementId;
            let elSelectId;
            let vcId;
            let vcCsl; 
                   
            async function initCiteProc(_elementId, _elSelectId, _vcId, _vcCsl) {    
                elementId = _elementId;
                vcId = _vcId;
                vcCsl = _vcCsl;
                
                elSelectId = _elSelectId;
                if(_elSelectId.id) {
                    elSelectId = _elSelectId.id;
                }
                
                const elStyleList = document.getElementById(elSelectId);     
                if(!elStyleList.hasChangeListener) {
                    elStyleList.addEventListener('change', (event) => {
                        updateCiteProc();
                    
                        const source = event.currentTarget;
                        const tooltip = bootstrap.Tooltip.getOrCreateInstance(source);
                        if(tooltip) {
                            tooltip.hide();
                        }
                    });
                    elStyleList.hasChangeListener = true
                }
                    
                await updateCiteProc();
            }
                    
            async function updateCiteProc() {
                let elId = elementId;
                if(elementId.id) {
                    elId = elementId.id;
                }   
                    
                const elStyleList = document.getElementById(elSelectId);
                const style = elStyleList.value.toLowerCase();
                    
                if(!cslStyle.hasOwnProperty(style)) {
                    console.error("Unsupported CSL style: ", style);
                    return;
                }
                let styleFilename = cslStyle[style].file;
                
                const styleAsText = await fetch('/csl/styles/'+styleFilename).then(r => r.text());
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
        
        response.render(
            new FilteredHeaderItem(JavaScriptHeaderItem.forScript(js, "initCiteProcFunction"), 
            BasePage.CSL_FILTER_NAME
        ));
    }
    
}
