package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.net.URI;
import java.net.URISyntaxException;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("I")
public class InternalPersistentIdentifier extends PersistentIdentifier {
	private static String uri_prefix = null;
	
	@SuppressWarnings("unused")
	private InternalPersistentIdentifier() {
		super();
	}

	InternalPersistentIdentifier(VirtualCollection vc) {
		super(vc, "VC-" + Long.toHexString(vc.getId()));
	}

	public URI createURI() {
		return URI.create(uri_prefix + collection.getId());
	}

	static void initBaseURI(String baseURI) {
		if (baseURI == null) {
			throw new NullPointerException("baseURI == null");
		}
		baseURI = baseURI.trim();
		if (baseURI.isEmpty()) {
			throw new IllegalArgumentException("empty baseURI is not allowed");
		}
		if (!baseURI.endsWith("/")) {
			baseURI = baseURI + "/";
		}
		baseURI = baseURI + "service/clarin-virtualcollection/";
		try {
			URI pfx =  new URI(baseURI);
			uri_prefix = pfx.toString();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("baseURI is invalid", e);
		}
	}

} // class InternalPersistentIdentifier
