package eu.clarin.cmdi.virtualcollectionregistry.rest;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalArgumentExceptionMapper implements
        ExceptionMapper<IllegalArgumentException> {

    @Override
    public Response toResponse(IllegalArgumentException e) {
        RestResponse response = new RestResponse();
        response.setIsSuccess(false);
        response.setInfo(e.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).type(
                MediaType.APPLICATION_XML).entity(response).build();
    }

} // class IllegalArgumentExceptionMapper
