package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.net.URI;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("G")
public class GWDGPersistentIdentifier extends PersistentIdentifier {

	@SuppressWarnings("unused")
	private GWDGPersistentIdentifier() {
		super();
	}
	
	GWDGPersistentIdentifier(VirtualCollection vc, String identifier) {
		super(vc, identifier);
	}

	public URI createURI() {
		return URI.create("http://hdl.handle.net/" + identifier);
	}

} // class GWDGPersistentIdentifier
