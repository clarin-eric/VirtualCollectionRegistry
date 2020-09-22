/*
 * Copyright (C) 2019 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.gui.pages.submission;

import eu.clarin.cmdi.virtualcollectionregistry.gui.pages.BasePage;
import org.apache.wicket.markup.html.basic.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilelb
 */
//public class SubmitVirtualCollectionErrorPage extends AbstractErrorPage {
public class SubmitVirtualCollectionErrorPage extends BasePage { 
    
    private static final long serialVersionUID = 1L;
    
    private static final Logger logger = LoggerFactory.getLogger(SubmitVirtualCollectionErrorPage.class);
    
    public SubmitVirtualCollectionErrorPage() {
        this(null);
    }
    
    public SubmitVirtualCollectionErrorPage(Throwable e) {  
        super();  
        logger.info("Created new SubmitVirtualCollectionErrorPage. {}", e == null ? "No exception available" : "Exception: "+e.getMessage());
        add(new Label("error", e == null ? "No exception available" : e.getMessage()));
    }
    
}
