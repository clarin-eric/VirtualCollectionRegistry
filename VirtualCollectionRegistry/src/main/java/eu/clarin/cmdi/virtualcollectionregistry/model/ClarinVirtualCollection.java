package eu.clarin.cmdi.virtualcollectionregistry.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.clarin.cmdi.virtualcollectionregistry.model.mapper.DateAdapter;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(namespace = "urn:x-vcr:clarin-virtualcollection",
		 propOrder = { "header", "resources", "components" })
@XmlRootElement(name = "CMD")
public class ClarinVirtualCollection {
	@XmlAccessorType(XmlAccessType.NONE)
	@XmlType(namespace = "urn:x-vcr:clarin-virtualcollection:header",
			propOrder = { "creator", "creationDate", "selfLink", "profile" })
	public static class Header {
		private ClarinVirtualCollection cvc;

		private Header() {
		}

		private Header(ClarinVirtualCollection cvc) {
			this.cvc = cvc;
		}
		
		@XmlElement(name = "MdCreator")
		public String getCreator() {
			return cvc.getVirtualCollection().getOwner().getName();
		}

		@XmlElement(name = "MdCreationDate")
		@XmlJavaTypeAdapter(DateAdapter.class)
		public Date getCreationDate() {
			return cvc.getVirtualCollection().getCreatedDate();
		}

		@XmlElement(name = "MdSelfLink")
		public URI getSelfLink() {
			return cvc.createHandleURI(cvc.getVirtualCollection().getPid());
		}

		@XmlElement(name = "MdProfile")
		public String getProfile() {
			return "http://url/to/mdprofile";
		}
	} // inner class Header

	@XmlAccessorType(XmlAccessType.NONE)
	@XmlType(namespace = "urn:x-vcr:clarin-virtualcollection:resources")
	public static class Resources {
		@XmlAccessorType(XmlAccessType.NONE)
		@XmlType(namespace = "urn:x-vcr:clarin-virtualcollection:resources:proxy",
				 propOrder = { "type", "ref" })
		public static class Proxy {
			private ClarinVirtualCollection cvc;
			private Resource resource;

			private Proxy() {
			}

			private Proxy(ClarinVirtualCollection cvc, Resource resource) {
				this.cvc = cvc;
				this.resource = resource;
			}

			@XmlAttribute(name = "id")
			public String getId() {
				return resource.getIdForXml();
			}

			@XmlElement(name = "ResourceType")
			public ResourceType getType() {
				return resource.getType();
			}

			@XmlElement(name = "ResourceRef")
			public String getRef() {
				if (resource instanceof ResourceMetadata) {
					return cvc.createHandleURI(
							((ResourceMetadata) resource).getPid()).toString();
				} else {
					return ((ResourceProxy) resource).getRef();
				}
			}
		} // inner class Proxy
		private List<Proxy> proxies = new ArrayList<Proxy>();

		private Resources() {
		}

		private Resources(ClarinVirtualCollection cvc) {
			for (Resource r : cvc.getVirtualCollection().getResources()) {
				proxies.add(new Proxy(cvc, r));
			}
		}

		@XmlElementWrapper(name = "ResourceProxyList")
		@XmlElements( { @XmlElement(name = "ResourceProxy") })
		public List<Proxy> getResourceProxyList() {
			return proxies;
		}
	} // inner class Resources

	@XmlAccessorType(XmlAccessType.NONE)
	@XmlType(namespace = "urn:x-vcr:clarin-virtualcollection:components")
	public static class Components {
		@XmlAccessorType(XmlAccessType.NONE)
		@XmlType(namespace = "urn:x-vcr:clarin-virtualcollection:components:vc",
				propOrder = { "name", "description", "creationDate", "visibility", "origin", "creator", "copyOfResourceMetadata" })
		public static class VC {
			@XmlAccessorType(XmlAccessType.NONE)
			public static class CopyOfResources {
				private List<Resource> copyRefs = new ArrayList<Resource>();

				private void init(VirtualCollection vc) {
					for (Resource resource : vc.getResources()) {
						if (resource instanceof ResourceMetadata) {
							copyRefs.add(resource);
						}
					}
				}

				@XmlIDREF
				@XmlAttribute(name = "ref")
				public List<Resource> getRefs() {
					return copyRefs;
				}
				
				public boolean isEmpty() {
					return copyRefs.isEmpty();
				}
			} // inner class CopyOfResources

			private CopyOfResources copyRefs = new CopyOfResources();
			private VirtualCollection vc;

			private void init(VirtualCollection vc) {
				this.vc = vc;
				this.copyRefs.init(vc);
			}
			
			@XmlElement(name = "Name")
			public String getName() {
				return vc.getName();
			}
			
			@XmlElement(name = "Description")
			public String getDescription() {
				return vc.getDescription();
			}
			
			@XmlElement(name = "CreationDate")
			@XmlJavaTypeAdapter(DateAdapter.class)
			public Date getCreationDate() {
				return vc.getCreationDate();
			}
			
			@XmlElement(name = "Visibility")
			public VirtualCollection.Visibility getVisibility() {
				return vc.getVisibility();
			}
			
			@XmlElement(name = "Origin")
			public String getOrigin() {
				return vc.getOrigin();
			}
			
			@XmlElement(name = "Creator")
			public Creator getCreator() {
				return vc.getCreator();
			}

			
			@XmlElement(name = "CopyOfResourceMetadata")
			public CopyOfResources getCopyOfResourceMetadata() {
				return copyRefs.isEmpty() ? null : copyRefs;
			}
		}
		private ClarinVirtualCollection cvc;
		private VC vc = new VC();
		
		private Components() {
		}

		private Components(ClarinVirtualCollection cvc) {
			this.cvc = cvc;
			this.vc.init(this.cvc.getVirtualCollection());
		}

		@XmlElement(name = "VirtualCollection")
		public VC getVirtualCollection() {
			return vc;
		}
	}

	private URI handleBaseUri;
	private VirtualCollection vc;
	private Header header;
	private Resources resources;
	private Components components;

	@SuppressWarnings("unused")
	private ClarinVirtualCollection() {
		super();
	}

	public ClarinVirtualCollection(VirtualCollection vc, URI handleBaseUri) {
		if (vc == null) {
			throw new NullPointerException("vc == null");
		}
		this.vc = vc;
		this.handleBaseUri = handleBaseUri;
		this.header = new Header(this);
		this.resources = new Resources(this);
		this.components = new Components(this);
	}

	URI createHandleURI(String pid) {
		return URI.create(handleBaseUri.toString() + "/" + pid);
	}

	VirtualCollection getVirtualCollection() {
		return vc;
	}

	@XmlElement(name = "Header")
	public Header getHeader() {
		return header;
	}

	@XmlElement(name = "Resources")
	public Resources getResources() {
		return resources;
	}

	@XmlElement(name = "Components")
	public Components getComponents() {
		return components;
	}
}
