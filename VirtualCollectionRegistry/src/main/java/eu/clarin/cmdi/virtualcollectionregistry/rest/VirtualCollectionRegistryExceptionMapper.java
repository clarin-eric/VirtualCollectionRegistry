package eu.clarin.cmdi.virtualcollectionregistry.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.xml.sax.SAXParseException;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;

@Provider
public class VirtualCollectionRegistryExceptionMapper implements
		ExceptionMapper<VirtualCollectionRegistryException> {

	public Response toResponse(VirtualCollectionRegistryException e) {
		RestResponse response = new RestResponse();
		response.setIsSuccess(false);
		response.setInfo("an error occured: " + e.getMessage());
		response.setError(getErrors(e.getCause()));
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
			.entity(e.getMessage()).type(MediaType.APPLICATION_XML).entity(response).build();
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

} // class VirtualCollectionRegistryExceptionMapper
