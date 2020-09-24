/*
 * Copyright (C) 2020 CLARIN
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wilelb
 */
public class SubmitHandlerFactory {
    
    private final List<SubmissionHandler> handlers = new ArrayList<>();
    
    public SubmitHandlerFactory() {
        handlers.add(new SubmitVirtualCollectionPageV1_0());
        handlers.add(new SubmitVirtualCollectionPageV1_1());
    }
    
    public List<SubmissionHandler> getHandlers() {
        return handlers;
    }
}
