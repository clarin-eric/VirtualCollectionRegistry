package eu.clarin.cmdi.virtualcollectionregistry.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXParseException;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionNotFoundException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryUsageException;

@Provider
public class VirtualCollectionRegistryExceptionMapper implements
		ExceptionMapper<VirtualCollectionRegistryException> {

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
		return Response
				.status(status)
				.type(MediaType.APPLICATION_XML)
				.entity(response)
				.build();
	}

	private static List<String> getErrors(Throwable t) {
		ArrayList<String> errors = new ArrayList<String>();
		while (t != null) {
			if (t instanceof SAXParseException) {
				SAXParseException e = (SAXParseException) t;
				errors.add(e.getMessage() + ", line = " + e.getLineNumber()
						+ ", column = " + e.getColumnNumber());
			} else {
				errors.add(t.getMessage());
			}
			t = t.getCause();
		};
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
