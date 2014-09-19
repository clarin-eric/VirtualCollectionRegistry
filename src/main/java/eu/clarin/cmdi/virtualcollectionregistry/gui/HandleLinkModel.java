/*
 * Copyright (C) 2014 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.model.IModel;

/**
 * Model that takes a link from an inner model and in case of a handle (any link
 * starting with "hdl:"), will replace the scheme with the handle proxy base URL
 *
 * @author twagoo
 */
@SuppressWarnings("serial")
public class HandleLinkModel implements IModel<String> {

    private final IModel<String> linkModel;
    public static final Pattern HANDLE_PATTERN = Pattern.compile("^(hdl|doi):(.*)$", Pattern.CASE_INSENSITIVE);
    public static final String HANDLE_PROXY = "http://hdl.handle.net/";
    public static final String URN_NBN_PREFIX = "urn:nbn";
    public static final String URN_NBN_RESOLVER_URL = "http://www.nbn-resolving.org/redirect/";
    private static final int HANDLE_ID_GROUP = 2;

    public HandleLinkModel(IModel<String> linkModel) {
        this.linkModel = linkModel;
    }

    @Override
    public String getObject() {
        final String link = linkModel.getObject();
        if (link != null) {
            final Matcher handleMatcher = HANDLE_PATTERN.matcher(link);
            if (handleMatcher.matches()) {
                return HANDLE_PROXY + handleMatcher.group(HANDLE_ID_GROUP);
            }
            if (link.toLowerCase().startsWith(URN_NBN_PREFIX)) {
                return URN_NBN_RESOLVER_URL + link;
            }
        }
        return link;
    }

    @Override
    public void setObject(String object) {
        linkModel.setObject(object);
    }

    @Override
    public void detach() {
        linkModel.detach();
    }
}
