package eu.clarin.cmdi.virtualcollectionregistry.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

	public Response toResponse(IllegalArgumentException e) {
		RestResponse response = new RestResponse();
		response.setIsSuccess(false);
		response.setInfo(e.getMessage());
		return Response
				.status(Response.Status.BAD_REQUEST)
				.type(MediaType.APPLICATION_XML)
				.entity(response)
				.build();
	}

} // class IllegalArgumentExceptionMapper
