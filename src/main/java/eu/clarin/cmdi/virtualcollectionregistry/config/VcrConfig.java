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
package eu.clarin.cmdi.virtualcollectionregistry.config;

import java.util.Locale;

/**
 *
 * @author wilelb
 */
public interface VcrConfig {
    public String getDownloadEndpoint();
    public String getDownloadEndpointPreferedPidType();
    public String getProcessEndpointPreferedPidType();
    public String getProcessEndpoint();
    public boolean isProcessPopupEnabled();
    public boolean isProcessEnabledForResources();
    public boolean isProcessEnabledForCollections();
    public boolean isDownloadEnabledForCollections();

    public String getLogoutMode();
    public boolean isLogoutEnabled();
    public String getLocaleString();
    public Locale getLocale();
    public String logConfig();
    public boolean isForkingEnabled();

    public String getMode();
    public boolean isProductionMode();
    public boolean isBetaMode();
    public boolean isAlphaMode();

    public int getHttpTimeout();
    public int getHttpRedirects();

    public boolean isHttpReferenceScanningEnabled();
    int getResourceScanAgeTresholdMs();

}
