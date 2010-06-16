package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection_;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIException;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream;
import eu.clarin.cmdi.virtualcollectionregistry.oai.OAIOutputStream.NamespaceDecl;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.OAIRepository;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.RecordList;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.SetSpecDesc;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.MetadataConstants;

class VirtualColletionRegistryOAIRepository implements OAIRepository {
    private static final Logger logger =
        LoggerFactory.getLogger(VirtualColletionRegistryOAIRepository.class);

    private static class DCMetadataFormat implements MetadataFormat {
        private final static List<NamespaceDecl> dc = Arrays.asList(
                new NamespaceDecl(MetadataConstants.NS_OAI_DC, "oai_dc",
                                  MetadataConstants.NS_OAI_DC_SCHEMA_LOCATION),
                new NamespaceDecl(MetadataConstants.NS_DC, "dc"));

        @Override
        public String getPrefix() {
            return "oai_dc";
        }

        @Override
        public String getNamespaceURI() {
            return "http://www.openarchives.org/OAI/2.0/oai_dc/";
        }

        @Override
        public String getSchemaLocation() {
            return "http://www.openarchives.org/OAI/2.0/oai_dc.xsd";
        }

        @Override
        public boolean canWriteClass(Class<?> clazz) {
            return true;
        }

        @Override
        public void writeObject(OAIOutputStream stream, Object item)
                throws OAIException {
            final VirtualCollection vc = (VirtualCollection) item;
            stream.writeStartElement(MetadataConstants.NS_OAI_DC, "dc", dc);
            stream.writeStartElement(MetadataConstants.NS_DC, "title");
            stream.writeCharacters(vc.getName());
            stream.writeEndElement(); // dc:title element

            stream.writeStartElement(MetadataConstants.NS_DC, "identifier");
            stream.writeCharacters(vc.getPersistentIdentifier().createURI());
            stream.writeEndElement(); // dc:identifier

            stream.writeStartElement(MetadataConstants.NS_DC, "date");
            // XXX: be sure to use correct date format
            stream.writeDate(vc.getCreationDate());
            stream.writeEndElement(); // dc:date

            if (vc.getCreator() != null) {
                stream.writeStartElement(MetadataConstants.NS_DC, "creator");
                stream.writeCharacters(vc.getCreator().getName());
                stream.writeEndElement(); // dc:creator element
            }

            if (vc.getDescription() != null) {
                stream
                        .writeStartElement(MetadataConstants.NS_DC,
                                "description");
                stream.writeCharacters(vc.getDescription());
                stream.writeEndElement(); // dc:description element
            }
            stream.writeEndElement(); // oai_dc:dc element
        }
    } // class OAIMetadataFormat

    private static class CMDIMetadataFormat implements MetadataFormat {
        @Override
        public String getPrefix() {
            return "cmdi";
        }

        @Override
        public String getNamespaceURI() {
            return "http://www.clarin.eu/cmd";
        }

        @Override
        public String getSchemaLocation() {
            // FIXME: for now minimal-cmdi.xsd
            return "http://www.clarin.eu/cmd/xsd/minimal-cmdi.xsd";
        }

        @Override
        public boolean canWriteClass(Class<?> clazz) {
            return true;
        }

        @Override
        public void writeObject(OAIOutputStream stream, Object item)
                throws OAIException {
            try {
                final VirtualCollectionRegistry registry =
                    VirtualCollectionRegistry.instance();
                final VirtualCollection vc = (VirtualCollection) item;
                registry.getMarshaller().marshalAsCMDI(
                        stream.getXMLStreamWriter(), vc);
            } catch (Exception e) {
                throw new OAIException("error writing object", e);
            }
        }
    } // class CMDIMetadataFormat

    private final VirtualCollectionRegistry registry;

    VirtualColletionRegistryOAIRepository(VirtualCollectionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getId() {
        return "virtualcollectionregistry.cmdi.clarin.eu";
    }

    @Override
    public String getName() {
        return "CLARIN Virtual Collection Registry";
    }

    @Override
    public String getDescription() {
        return "The virtual collection registry is a component of the " +
            "CLARIN metadata initiative.";
    }

    @Override
    public Date getEarliestTimestamp() {
        return new Date();
    }

    @Override
    public DeletedNotion getDeletedNotion() {
        return DeletedNotion.TRANSIENT;
    }

    @Override
    public Granularity getGranularity() {
        return Granularity.SECONDS;
    }

    @Override
    public int getCompressionMethods() {
        return 0 /* COMPRESSION_METHOD_GZIP|COMPRESSION_METHOD_DEFLATE */;
    }

    @Override
    public Set<String> getAdminAddreses() {
        Set<String> addresses = new HashSet<String>();
        addresses.add("vcr-admin@clarin.eu");
        return addresses;
    }

    @Override
    public Object getSampleRecordLocalId() {
        return new Long(23);
    }

    @Override
    public Set<MetadataFormat> getMetadataFormats() {
        Set<MetadataFormat> formats = new HashSet<MetadataFormat>();
        formats.add(new DCMetadataFormat());
        formats.add(new CMDIMetadataFormat());
        return formats;
    }

    @Override
    public Set<SetSpecDesc> getSetDescs() {
        Set<SetSpecDesc> setspecs = new HashSet<SetSpecDesc>();
        setspecs.add(new SetSpecDesc("odd", "Odd virtual collections"));
        setspecs.add(new SetSpecDesc("even", "even virtual collections",
                                     "With a description"));
        return setspecs;
    }

    @Override
    public Object parseLocalId(String unparsedLocalId) {
        try {
            long id = Long.parseLong(unparsedLocalId);
            if (id > 0) {
                return new Long(id);
            }
        } catch (NumberFormatException e) {
            /* FALL-THROUGH */
        }
        return null;
    }

    @Override
    public String unparseLocalId(Object localId) {
        return ((Long) localId).toString();
    }

    @Override
    public Record getRecord(Object localId, boolean headerOnly)
            throws OAIException {
        try {
            long id = (Long) localId;
            VirtualCollection vc = registry.retrieveVirtualCollection(id);
            return createRecord(vc, headerOnly);
        } catch (VirtualCollectionNotFoundException e) {
            return null;
        } catch (VirtualCollectionRegistryException e) {
            throw new OAIException("error", e);
        }
    }

    @Override
    public RecordList getRecords(String prefix, Date from, Date until,
            String set, int offset, boolean headerOnly) throws OAIException {
        try {
            EntityManager em = registry.getDataStore().getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaQuery<Long> cq1 = cb.createQuery(Long.class);
            Root<VirtualCollection> root1 = cq1.from(VirtualCollection.class);
            cq1.select(cb.count(root1));
            cq1.where(buildWhere(cb, cq1, root1, from, until));

            CriteriaQuery<VirtualCollection> cq2 =
                cb.createQuery(VirtualCollection.class);
            Root<VirtualCollection> root2 = cq2.from(VirtualCollection.class);
            cq2.select(root2);
            cq2.where(buildWhere(cb, cq2, root2, from, until));
            cq2.orderBy(cb.asc(root2.get(VirtualCollection_.modifedDate)));
            
            TypedQuery<Long> q1 = em.createQuery(cq1);
            TypedQuery<VirtualCollection> q2 = em.createQuery(cq2);
            
            em.getTransaction().begin();
            List<VirtualCollection> vcs = null;
            long count = q1.getSingleResult();
            if (count > 0) {
                if (offset > 0) {
                    q2.setFirstResult(offset);
                }
                q2.setMaxResults(100);
                vcs = q2.getResultList();
            }
            em.getTransaction().commit();

            if ((vcs != null) && !vcs.isEmpty()) {
                List<Record> records = new ArrayList<Record>(vcs.size());
                for (VirtualCollection vc : vcs) {
                    records.add(createRecord(vc, headerOnly));
                }
                boolean hasMore = (offset + vcs.size()) < count;
                return new RecordList(records, offset, hasMore, (int) count);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new OAIException("error fetcing records", e);
        } finally {
            registry.getDataStore().closeEntityManager();
        }
    }

    private Predicate buildWhere(CriteriaBuilder cb, CriteriaQuery<?> cq,
            Root<VirtualCollection> root, Date from, Date until) {
        Predicate where =
                cb.equal(root.get(VirtualCollection_.state),
                                  VirtualCollection.State.PUBLIC);
        if ((from != null) && (until != null)) {
            where = cb.and(where, cb.between(root
                    .get(VirtualCollection_.modifedDate), from, until));
        } else if (from != null) {
            where = cb.and(where, cb.greaterThanOrEqualTo(root
                    .get(VirtualCollection_.modifedDate), from));
        } else if (until != null) {
            where = cb.and(where, cb.lessThanOrEqualTo(root
                    .get(VirtualCollection_.modifedDate), until));
        }
        return where;
    }

    private static class RecordFullImpl implements Record {
        final VirtualCollection vc;

        RecordFullImpl(VirtualCollection vc) {
            this.vc = vc;
        }

        @Override
        public Object getLocalId() {
            return vc.getId();
        }

        @Override
        public Date getDatestamp() {
            return vc.getModifiedDate();
        }

        @Override
        public boolean isDeleted() {
            return vc.getState() == VirtualCollection.State.DELETED;
        }

        @Override
        public List<String> getSetSpecs() {
            List<String> specs = null;
            if (vc.getId() % 2 == 0) {
                specs = Arrays.asList("even");
            } else {
                specs = Arrays.asList("odd");
            }
            return specs;
        }

        @Override
        public Object getItem() {
            return vc;
        }

        @Override
        public Class<?> getItemClass() {
            return VirtualCollection.class;
        }
    }

    private static class RecordHeaderImpl implements Record {
        final Long id;
        final Date datestamp;
        final boolean deleted;

        RecordHeaderImpl(VirtualCollection vc) {
            this.id = vc.getId();
            this.datestamp = vc.getModifiedDate();
            this.deleted = (vc.getState() == VirtualCollection.State.DELETED);
        }

        @Override
        public Object getLocalId() {
            return id;
        }

        @Override
        public Date getDatestamp() {
            return datestamp;
        }

        @Override
        public boolean isDeleted() {
            return deleted;
        }

        @Override
        public List<String> getSetSpecs() {
            List<String> specs = null;
            if (id % 2 == 0) {
                specs = Arrays.asList("even");
            } else {
                specs = Arrays.asList("odd");
            }
            return specs;
        }

        @Override
        public Object getItem() {
            return null;
        }

        @Override
        public Class<?> getItemClass() {
            return VirtualCollection.class;
        }
    }

    private Record createRecord(VirtualCollection vc, boolean headerOnly) {
        if (vc != null) {
            if (headerOnly) {
                logger.debug("creating header-only wrapper for vc {}",
                        vc.getId());
                return new RecordHeaderImpl(vc);
            } else {
                logger.debug("creating full wrapper for vc {}", vc.getId());
                return new RecordFullImpl(vc);
            }
        }
        return null;
    }
} // VirtualColletionRegistryOAIRepository
