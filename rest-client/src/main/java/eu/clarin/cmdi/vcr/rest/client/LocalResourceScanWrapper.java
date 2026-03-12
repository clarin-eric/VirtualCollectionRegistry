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
package eu.clarin.cmdi.vcr.rest.client;

import eu.clarin.cmdi.virtualcollectionregistry.model.collection.ResourceScan;

/**
 *
 * @author wilelb
 */
public class LocalResourceScanWrapper extends ResourceScan {
    private final Long id;

    public LocalResourceScanWrapper(long id, String ref, String resolvedRef, String sessionId) {
        super(ref, resolvedRef, sessionId);
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }
}
