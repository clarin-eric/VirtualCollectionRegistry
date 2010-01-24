package eu.clarin.cmdi.virtualcollectionregistry.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.security.Principal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import eu.clarin.cmdi.virtualcollectionregistry.DataStore;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistry;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryException;
import eu.clarin.cmdi.virtualcollectionregistry.VirtualCollectionRegistryMarshaller.Format;
import eu.clarin.cmdi.virtualcollectionregistry.model.ClarinVirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.Handle;
import eu.clarin.cmdi.virtualcollectionregistry.model.ResourceMetadata;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;

@Path("/")
public class VirtualCollectionRegistryRestService {
	private VirtualCollectionRegistry registry =
		VirtualCollectionRegistry.instance();
	@Context
	SecurityContext security;
	@Context
	UriInfo uriInfo;
	@Context
	HttpHeaders headers;

	@POST
	@Path("/virtualcollection")
	@Consumes({ MediaType.TEXT_XML,
				MediaType.TEXT_XML,
				MediaType.APPLICATION_JSON })
	public Response createVirtualCollection(InputStream input)
			throws VirtualCollectionRegistryException {
		Principal principal = security.getUserPrincipal();
		if (principal == null) {
			throw new IllegalArgumentException("princial == null");
		}
		try {
			Format format = getInputFormat();
			String encoding = getInputEncoding();
			VirtualCollection vc = registry.getMarshaller()
				.unmarshal(input, format, encoding);
			long id = registry.createVirtualCollection(principal, vc);
			RestResponse response = new RestResponse();
			response.setIsSuccess(true);
			response.setInfo("created");
			response.setId(id);
			URI uri = uriInfo.getRequestUriBuilder().path(Long.toString(id))
					.build();
			return Response.created(uri).entity(response).build();
		} catch (Exception e) {
			throw new VirtualCollectionRegistryException("create", e);
		}
	}

	@GET
	@Path("/virtualcollection/{id}")
	@Produces({ MediaType.TEXT_XML,
				MediaType.APPLICATION_XML,
				MediaType.APPLICATION_JSON })
	public Response getVirtualCollection(@PathParam("id") long id)
			throws VirtualCollectionRegistryException {
		final VirtualCollection vc = registry.retrieveVirtualCollection(id);
		StreamingOutput writer = new StreamingOutput() {
			public void write(OutputStream stream) throws IOException,
					WebApplicationException {
				Format format = getOutputFormat();
				registry.getMarshaller().marshal(stream, format, vc);
			}
		};
		return Response.ok(writer).build();
	}

	@PUT
	@Path("/virtualcollection/{id}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
	public Response updateVirtualCollection(@PathParam("id") long id,
			VirtualCollection vc) throws VirtualCollectionRegistryException {
		Principal principal = security.getUserPrincipal();
		if (principal == null) {
			throw new IllegalArgumentException("princial == null");
		}
		registry.updateVirtualCollection(principal, id, vc);
		RestResponse response = new RestResponse();
		response.setIsSuccess(true);
		response.setInfo("updated");
		response.setId(id);
		return Response.ok(response).build();
	}

	@DELETE
	@Path("/virtualcollection/{id}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
	public Response deleteVirtualCollection(@PathParam("id") long id)
			throws VirtualCollectionRegistryException {
		Principal principal = security.getUserPrincipal();
		if (principal == null) {
			throw new IllegalArgumentException("princial == null");
		}
		registry.deleteVirtualCollection(principal, id);
		RestResponse response = new RestResponse();
		response.setIsSuccess(true);
		response.setInfo("deleted");
		response.setId(id);
		return Response.ok(response).build();
	}

	@GET
	@Path("/virtualcollections")
	@Produces({ MediaType.TEXT_XML,
				MediaType.APPLICATION_XML,
				MediaType.APPLICATION_JSON })
	public Response getVirtualCollections()
			throws VirtualCollectionRegistryException {
		final List<VirtualCollection> vcs = registry.getVirtualCollections();
		StreamingOutput writer = new StreamingOutput() {
			public void write(OutputStream stream) throws IOException,
					WebApplicationException {
				Format format = getOutputFormat();
				registry.getMarshaller().marshal(stream, format, vcs);
			}
		};
		return Response.ok(writer).build();
	}
	
	@GET
	@Path("/clarin-virtualcollection/{id}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
	public Response getClarinVirtualCollection(
			@PathParam("id") long id) throws VirtualCollectionRegistryException {
		VirtualCollection vc = registry.retrieveVirtualCollection(id);
		URI handleBaseUri = uriInfo.getBaseUriBuilder().path("handle").build();  
		final ClarinVirtualCollection cvc =
			new ClarinVirtualCollection(vc, handleBaseUri);
		StreamingOutput writer = new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				registry.getMarshaller().marshal(output, Format.XML, cvc);
			}
		};
		return Response.ok(writer).build();
	}

	@GET
	@Path("/clarin-metadata/{id}")
	@Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_XML })
	public Response getClarinMetadata(@PathParam("id") long id)
			throws VirtualCollectionRegistryException {
		System.err.println("Metadata!");
		ResourceMetadata md = registry.retrieveMetadataResource(id);
		return Response.ok(md).build();
	}

	@GET
	@Path("/handle/{pid}")
	public Response getHandle(@PathParam("pid") String pid) {
		System.err.println("Pid: " + pid);
		EntityManager em = DataStore.instance().getEntityManager();
		try {
			em.getTransaction().begin();
			Query q = em.createNamedQuery("Handle.findByPid");
			q.setParameter("pid", pid);
			Handle handle = (Handle) q.getSingleResult();
			System.err.println("handle: " + handle.getType());
			URI target;
			switch (handle.getType()) {
			case COLLECTION:
				target = uriInfo.getBaseUriBuilder().path(
						"clarin-virtualcollection/" + handle.getTarget()).build();
				break;
			case METADATA:
				target = uriInfo.getBaseUriBuilder().path(
						"clarin-metadata/" + handle.getTarget()).build();
				break;
			default:
				throw new VirtualCollectionRegistryException("internel error");
			} // switch
			return Response.seeOther(target).build();
		} catch (Exception e) {
			throw new WebApplicationException(e);
		} finally {
			em.getTransaction().commit();
		}
	}

	private Format getInputFormat() {
		Format format = getMediaType(headers.getMediaType());
		return (format != null) ? format : Format.UNSUPPORTED;
	}

	private String getInputEncoding() {
		String encoding =
			headers.getMediaType().getParameters().get("encoding");
		return (encoding != null) ? encoding : "utf-8";
	}

	private Format getOutputFormat() {
		for (MediaType type : headers.getAcceptableMediaTypes()) {
			Format format = getMediaType(type);
			if (format != null) {
				return format;
			}
		}
		return Format.UNSUPPORTED;
	}

	private static Format getMediaType(MediaType type) {
		if (type.isCompatible(MediaType.APPLICATION_XML_TYPE)
				|| type.isCompatible(MediaType.TEXT_XML_TYPE)) {
			return Format.XML;
		}
		if (type.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
			return Format.JSON;
		}
		return null;
	}

} // class VirtualCollectionRegistryRestService
