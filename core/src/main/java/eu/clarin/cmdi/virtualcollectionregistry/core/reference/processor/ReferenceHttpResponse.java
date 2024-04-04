/*
 * Copyright (C) 2024 CLARIN
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
package eu.clarin.cmdi.virtualcollectionregistry.core.reference.processor;

/**
 *
 * @author wilelb
 */
public class ReferenceHttpResponse {
    private int httpResponseCode;
    private String httpResponseMsg;
    private String mimeType;
    private String exception;

    private String nameSuggestion;
    private String descriptionSuggestion;

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public void setHttpResponseCode(int httpResponseCode) {
        this.httpResponseCode = httpResponseCode;
    }

    public String getHttpResponseMsg() {
        return httpResponseMsg;
    }

    public void setHttpResponseMsg(String httpResponseMsg) {
        this.httpResponseMsg = httpResponseMsg;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setException(String exception) { this.exception = exception; }

    public void setException(Exception exception) { this.exception = exception.getMessage(); }

    public String getException() { return exception; }

    public String getNameSuggestion() {
        return nameSuggestion;
    }

    public void setNameSuggestion(String nameSuggestion) {
        this.nameSuggestion = nameSuggestion;
    }

    public String getDescriptionSuggestion() {
        return descriptionSuggestion;
    }

    public void setDescriptionSuggestion(String descriptionSuggestion) {
        this.descriptionSuggestion = descriptionSuggestion;
    }
}
