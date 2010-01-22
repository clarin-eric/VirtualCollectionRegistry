package eu.clarin.cmdi.virtualcollectionregistry.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryPermissionException;

@Provider
public class VirtualCollectionRegistryPermissionExceptionMapper implements
		ExceptionMapper<VirtualCollectionRegistryPermissionException> {

	@Override
	public Response toResponse(VirtualCollectionRegistryPermissionException e) {
		RestResponse response = new RestResponse();
		response.setIsSuccess(false);
		response.setInfo(e.getMessage());
		return Response.status(Response.Status.FORBIDDEN)
			.entity(e.getMessage()).type(MediaType.APPLICATION_XML).entity(response).build();
	}

} // class VirtualCollectionRegistryPermissionExceptionMapper
