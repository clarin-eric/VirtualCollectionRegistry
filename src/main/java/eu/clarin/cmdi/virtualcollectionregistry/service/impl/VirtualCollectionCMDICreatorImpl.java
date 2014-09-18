package eu.clarin.cmdi.virtualcollectionregistry.service.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.clarin.cmdi.virtualcollectionregistry.model.Creator;
import eu.clarin.cmdi.virtualcollectionregistry.model.GeneratedBy;
import eu.clarin.cmdi.virtualcollectionregistry.model.Resource;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD.Components.VirtualCollection.Creator.Email;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD.Components.VirtualCollection.Creator.Organisation;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD.Components.VirtualCollection.Description;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD.Components.VirtualCollection.GeneratedBy.QueryProfile;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD.Components.VirtualCollection.GeneratedBy.QueryValue;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD.Components.VirtualCollection.Name;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD.Components.VirtualCollection.ReproducabilityNotice;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD.Resources.ResourceProxyList.ResourceProxy;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.CMD.Resources.ResourceProxyList.ResourceProxy.ResourceType;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.ComplextypePurpose1;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.ComplextypeReproducability1;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.ComplextypeStatus1;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.ResourcetypeSimple;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.SimpletypePurpose1;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.SimpletypeReproducability1;
import eu.clarin.cmdi.virtualcollectionregistry.model.cmdi.SimpletypeStatus1;
import eu.clarin.cmdi.virtualcollectionregistry.service.VirtualCollectionCMDICreator;

/**
 * A service implementation that creates a CMDI object hierarchy for a
 * {@link VirtualCollection} object based on the classes generated from the
 * Virtual Collection profile schema by JAXB (as configured in the pom.xml of
 * this project)
 *
 * @author twagoo
 */
@Service
public class VirtualCollectionCMDICreatorImpl implements VirtualCollectionCMDICreator {

    private final static Logger logger = LoggerFactory.getLogger(VirtualCollectionCMDICreatorImpl.class);
    private final DatatypeFactory dataTypeFactory;

    // Collection display name value read from context.xml with a fallback value inserted by Spring
    @Value("${eu.clarin.cmdi.virtualcollectionregistry.collectiondisplayname:CLARIN Virtual Collection Registry}")
    private String collectionDisplayName;

    public VirtualCollectionCMDICreatorImpl() throws DatatypeConfigurationException {
        dataTypeFactory = DatatypeFactory.newInstance();
    }

    ///////// CMDI CONSTANTS ////////////////////
    // These should match up with the schema   
    // the classes in eu.clarin.cmdi.          
    // virtualcollectionregistry.model.cmdi are
    // generated from by JAXB                  
    /////////////////////////////////////////////
    public static final String VIRTUAL_COLLECTION_PROFILE_ID
            = "clarin.eu:cr1:p_1404130561238";
    public static final String VIRTUAL_COLLECTION_PROFILE_SCHEMA_LOCATION
            = "http://www.clarin.eu/cmd/ http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/"
            + VIRTUAL_COLLECTION_PROFILE_ID
            + "/xsd";
    private static final String CMD_VERSION = "1.1";
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd");
    ///////// END OF CMDI CONSTANTS   /////////

    @Override
    public String getSchemaLocation() {
        return VIRTUAL_COLLECTION_PROFILE_SCHEMA_LOCATION;
    }

    @Override
    public CMD createMetadataStructure(VirtualCollection vc) {
        logger.debug("Creating CMD root");
        final CMD cmdRoot = new CMD();
        cmdRoot.setCMDVersion(CMD_VERSION);
        logger.trace("Creating header");
        cmdRoot.setHeader(createHeader(vc));
        logger.trace("Creating resources");
        final CMD.Resources resources = createResources(vc);
        cmdRoot.setResources(resources);
        logger.trace("Creating components");
        cmdRoot.setComponents(createComponents(vc, resources));
        return cmdRoot;
    }

    private CMD.Header createHeader(VirtualCollection vc) {
        final CMD.Header header = new CMD.Header();
        header.setMdCreationDate(dataTypeFactory.newXMLGregorianCalendar(DATE_FORMAT.format(new Date())));
        header.setMdProfile(VIRTUAL_COLLECTION_PROFILE_ID);
        header.getMdCreator().add(vc.getOwner().getName());
        header.setMdSelfLink(vc.getPersistentIdentifier().getURI());
        if (collectionDisplayName != null) {
            header.setMdCollectionDisplayName(collectionDisplayName);
        }
        return header;
    }

    private CMD.Resources createResources(VirtualCollection vc) {
        final CMD.Resources resources = new CMD.Resources();

        final CMD.Resources.ResourceProxyList resourceProxyList = new CMD.Resources.ResourceProxyList();
        resources.setResourceProxyList(resourceProxyList);
        final List<ResourceProxy> proxyList = resourceProxyList.getResourceProxy();

        for (Resource resource : vc.getResources()) {
            final ResourceProxy resourceProxy = new ResourceProxy();
            if (resource.getId() == null) {
                resourceProxy.setId("r" + UUID.randomUUID().toString());
            } else {
                resourceProxy.setId("r" + Long.toString(resource.getId()));
            }
            resourceProxy.setResourceRef(resource.getRef());

            final ResourceType type = new ResourceType();
            if (resource.getType() == Resource.Type.METADATA) {
                type.setValue(ResourcetypeSimple.METADATA);
            } else {
                type.setValue(ResourcetypeSimple.RESOURCE);
            }
            resourceProxy.setResourceType(type);

            proxyList.add(resourceProxy);
        }

        // add empty instance of mandatory journal file proxy list
        resources.setJournalFileProxyList(new CMD.Resources.JournalFileProxyList());
        // add empty instance of mandatory resource relation list
        resources.setResourceRelationList(new CMD.Resources.ResourceRelationList());
        return resources;
    }

    private CMD.Components createComponents(VirtualCollection vc, CMD.Resources resources) {
        final CMD.Components.VirtualCollection virtualCollection = new CMD.Components.VirtualCollection();

        final Name name = new Name();
        name.setValue(vc.getName());
        virtualCollection.setName(name);

        if (vc.getDescription() != null) {
            final Description description = new Description();
            description.setValue(vc.getDescription());
            virtualCollection.setDescription(description);
        }

        virtualCollection.setCreationDate(getCreationDate(vc));
        virtualCollection.setStatus(getStatus(vc));
        virtualCollection.setPurpose(getPurpose(vc));
        virtualCollection.getCreator().add(getCreator(vc));
        virtualCollection.setReproducability(getReproducability(vc));

        if (vc.getReproducibilityNotice() != null) {
            final ReproducabilityNotice reproducabilityNotice = new ReproducabilityNotice();
            reproducabilityNotice.setValue(vc.getReproducibilityNotice());
            virtualCollection.setReproducabilityNotice(reproducabilityNotice);
        }

        for (String keyword : vc.getKeywords()) {
            final CMD.Components.VirtualCollection.Keyword keywordElement = new CMD.Components.VirtualCollection.Keyword();
            keywordElement.setValue(keyword);
            virtualCollection.getKeyword().add(keywordElement);
        }

        for (Resource resource : vc.getResources()) {
            addResource(virtualCollection, resource, resources);
        }

        if (vc.getType() == VirtualCollection.Type.INTENSIONAL && vc.getGeneratedBy() != null) {
            virtualCollection.setGeneratedBy(getGeneratedBy(vc.getGeneratedBy()));
        }

        final CMD.Components components = new CMD.Components();
        components.setVirtualCollection(virtualCollection);
        return components;
    }

    private CMD.Components.VirtualCollection.GeneratedBy getGeneratedBy(GeneratedBy generatedBy) {
        final CMD.Components.VirtualCollection.GeneratedBy component = new CMD.Components.VirtualCollection.GeneratedBy();

        final String uri = generatedBy.getURI();
        if (uri != null) {
            component.setURI(uri);
        }

        final String description = generatedBy.getDescription();
        if (description != null) {
            final CMD.Components.VirtualCollection.GeneratedBy.Description descriptionElement = new CMD.Components.VirtualCollection.GeneratedBy.Description();
            descriptionElement.setValue(description);
            component.setDescription(descriptionElement);
        }

        if (generatedBy.getQuery() != null) {
            final String profile = generatedBy.getQuery().getProfile();
            if (profile != null) {
                final QueryProfile profileElement = new QueryProfile();
                profileElement.setValue(profile);
                component.setQueryProfile(profileElement);
            }

            final String value = generatedBy.getQuery().getValue();
            if (value != null) {
                final QueryValue valueElement = new QueryValue();
                valueElement.setValue(value);
                component.setQueryValue(valueElement);
            }
        }

        return component;
    }

    private XMLGregorianCalendar getCreationDate(VirtualCollection vc) {
        final Date creationDate = vc.getCreationDate();
        return dataTypeFactory.newXMLGregorianCalendar(DATE_FORMAT.format(creationDate));
    }

    private ComplextypeStatus1 getStatus(VirtualCollection vc) {
        // status is a mandatory field
        final ComplextypeStatus1 status = new ComplextypeStatus1();
        switch (vc.getState()) {
            case PUBLIC:
                status.setValue(SimpletypeStatus1.PUBLISHED);
                break;
            case DELETED:
                status.setValue(SimpletypeStatus1.DEPRECATED);
                break;
            case DEAD:
                status.setValue(SimpletypeStatus1.DEPRECATED);
                break;
            default:
                status.setValue(SimpletypeStatus1.DRAFT);
        }
        return status;
    }

    private ComplextypeReproducability1 getReproducability(VirtualCollection vc) {
        if (vc.getReproducibility() == null) {
            return null;
        } else {
            final ComplextypeReproducability1 reproducability = new ComplextypeReproducability1();
            switch (vc.getReproducibility()) {
                //TODO: better mapping
                case FLUCTUATING:
                    reproducability.setValue(SimpletypeReproducability1.FLUCTUATING);
                    break;
                case INTENDED:
                    reproducability.setValue(SimpletypeReproducability1.INTENDED);
                    break;
                case UNTENDED:
                    reproducability.setValue(SimpletypeReproducability1.UNTENDED);
                    break;
            }
            return reproducability;
        }
    }

    private ComplextypePurpose1 getPurpose(VirtualCollection vc) {
        if (vc.getPurpose() == null) {
            return null;
        } else {
            final ComplextypePurpose1 purpose = new ComplextypePurpose1();
            switch (vc.getPurpose()) {
                case FUTURE_USE:
                    purpose.setValue(SimpletypePurpose1.FUTURE_USE);
                    break;
                case REFERENCE:
                    purpose.setValue(SimpletypePurpose1.REFERENCE);
                    break;
                case RESEARCH:
                    purpose.setValue(SimpletypePurpose1.RESEARCH);
                    break;
                case SAMPLE:
                    purpose.setValue(SimpletypePurpose1.SAMPLE);
                    break;
            }
            return purpose;
        }
    }

    private CMD.Components.VirtualCollection.Creator getCreator(VirtualCollection vc) {
        if (vc.getCreators().isEmpty()) {
            return null;
        } else {
            final CMD.Components.VirtualCollection.Creator creator = new CMD.Components.VirtualCollection.Creator();
            final Creator vcCreator = vc.getCreators().get(0);

            // name/creator is a mandatory field
            final CMD.Components.VirtualCollection.Creator.Name name = new CMD.Components.VirtualCollection.Creator.Name();
            name.setValue(vcCreator.getPerson());
            creator.setName(name);

            if (vcCreator.getEMail() != null) {
                final Email email = new Email();
                email.setValue(vcCreator.getEMail());
                creator.setEmail(email);
            }

            if (vcCreator.getOrganisation() != null) {
                final Organisation organisation = new Organisation();
                organisation.setValue(vcCreator.getOrganisation());
                creator.setOrganisation(organisation);
            }
            return creator;
        }
    }

    private void addResource(final CMD.Components.VirtualCollection virtualCollection, Resource resource, CMD.Resources resources) {
        if (!StringUtils.isEmpty(resource.getLabel()) || !StringUtils.isEmpty(resource.getDescription())) {
            final CMD.Components.VirtualCollection.Resource resourceComponent = new CMD.Components.VirtualCollection.Resource();
            if (!StringUtils.isEmpty(resource.getLabel())) {
                // add label
                final CMD.Components.VirtualCollection.Resource.Label label = new CMD.Components.VirtualCollection.Resource.Label();
                label.setValue(resource.getLabel());
                resourceComponent.setLabel(label);
            }
            if (!StringUtils.isEmpty(resource.getDescription())) {
                // add description
                final CMD.Components.VirtualCollection.Resource.Description description = new CMD.Components.VirtualCollection.Resource.Description();
                description.setValue(resource.getDescription());
                resourceComponent.setDescription(description);
            }
            // look up the resource proxy (by URI) and set a reference
            for (ResourceProxy rp : resources.getResourceProxyList().getResourceProxy()) {
                if (rp.getResourceRef().equals(resource.getRef())) {
                    resourceComponent.getRef().add(rp);
                    break;
                }
            }
            virtualCollection.getResource().add(resourceComponent);
        }
    }
}
