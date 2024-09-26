package eu.clarin.cmdi.virtualcollectionregistry.rest;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXParseException;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionNotFoundException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionValidationException;

@Provider
public class VirtualCollectionRegistryExceptionMapper implements
        ExceptionMapper<VirtualCollectionRegistryException> {

    @Override
    public Response toResponse(VirtualCollectionRegistryException e) {
        Status       status = null;
        List<String> errors = null;
        if (e instanceof VirtualCollectionRegistryUsageException) {
            status = Status.BAD_REQUEST;
            errors = getErrors(e.getCause());
        } else if (e instanceof VirtualCollectionRegistryPermissionException) {
            status = Status.FORBIDDEN;
        } else if (e instanceof VirtualCollectionNotFoundException) {
            status = Status.NOT_FOUND;
        } else {
            if (hasCause(e.getCause(), XMLStreamException.class)) {
                status = Status.BAD_REQUEST;
            } else {
                status = Status.INTERNAL_SERVER_ERROR;
            }
            errors = getErrors(e.getCause());
        }
        RestResponse response = new RestResponse();
        response.setIsSuccess(false);
        response.setInfo(e.getMessage());
        if (errors != null) {
            response.setError(errors);
        }
        return Response.status(status).type(MediaType.APPLICATION_XML)
            .entity(response).build();
    }

    private static List<String> getErrors(Throwable t) {
        ArrayList<String> errors = new ArrayList<String>();
        while (t != null) {
            if (t instanceof SAXParseException) {
                final SAXParseException e = (SAXParseException) t;
                String message = e.getMessage();
                if (message.startsWith("cvc")) {
                    int pos = message.indexOf(':');
                    if (pos != 0) {
                        message = message.substring(pos + 1);
                    }
                }
                errors.add(message.trim() + " [line = " + e.getLineNumber() +
                        ", column = " + e.getColumnNumber() + "]");
            } else if (t instanceof XMLStreamException) {
                final XMLStreamException e = (XMLStreamException) t;
                String message = e.getMessage();
                if (message.startsWith("ParseError")) {
                    int pos = message.indexOf(':');
                    if (pos != -1) {
                        pos = message.indexOf(':', pos + 1);
                        if (pos != -1) {
                            message = message.substring(pos + 1);
                        }
                    }
                }
                if (e.getLocation() != null) {
                    message = message.trim() + " [line = " +
                            e.getLocation().getLineNumber() + ", column = " +
                            e.getLocation().getColumnNumber() + "]";
                }
                errors.add(message);
            } else {
                errors.add(t.getMessage());
            }
            t = t.getCause();
        }
        ;
        return errors;
    }

    private static boolean hasCause(Throwable t, Class<?> clazz) {
        while (t != null) {
            if (clazz.isInstance(t)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

} // class VirtualCollectionRegistryExceptionMapper
