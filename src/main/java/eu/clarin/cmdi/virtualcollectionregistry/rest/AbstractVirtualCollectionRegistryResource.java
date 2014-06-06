/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarin.cmdi.virtualcollectionregistry.rest;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionMarshaller.Format;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author twagoo
 */
public abstract class AbstractVirtualCollectionRegistryResource {

    protected String getInputEncoding(HttpHeaders headers) {
        String encoding
                = headers.getMediaType().getParameters().get("encoding");
        return (encoding != null) ? encoding : "utf-8";
    }

    protected Format getInputFormat(HttpHeaders headers) {
        Format format = getMediaType(headers.getMediaType());
        return (format != null) ? format : Format.UNSUPPORTED;
    }

    protected Format getOutputFormat(HttpHeaders headers) {
        for (MediaType type : headers.getAcceptableMediaTypes()) {
            Format format = getMediaType(type);
            if (format != null) {
                return format;
            }
        }
        return Format.UNSUPPORTED;
    }

    protected static Format getMediaType(MediaType type) {
        if (type.isCompatible(MediaType.APPLICATION_XML_TYPE)
                || type.isCompatible(MediaType.TEXT_XML_TYPE)) {
            return Format.XML;
        }
        if (type.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
            return Format.JSON;
        }
        return null;
    }
}
