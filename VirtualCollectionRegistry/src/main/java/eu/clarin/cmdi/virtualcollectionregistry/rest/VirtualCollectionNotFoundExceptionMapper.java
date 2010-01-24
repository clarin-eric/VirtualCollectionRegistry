package eu.clarin.cmdi.virtualcollectionregistry.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionNotFoundException;

@Provider
public class VirtualCollectionNotFoundExceptionMapper implements
		ExceptionMapper<VirtualCollectionNotFoundException> {

	@Override
	public Response toResponse(VirtualCollectionNotFoundException e) {
		RestResponse response = new RestResponse();
		response.setIsSuccess(false);
		response.setInfo(e.getMessage());
		return Response.status(Response.Status.NOT_FOUND)
			.entity(e.getMessage()).type(MediaType.APPLICATION_XML).entity(response).build();
	}

} // class VirtualCollectionNotFoundExceptionMapper
