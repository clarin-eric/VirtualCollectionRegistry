package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.oai.provider.DublinCoreAdapter;
import eu.clarin.cmdi.oai.provider.DublinCoreConverter;
import eu.clarin.cmdi.oai.provider.MetadataFormat;
import eu.clarin.cmdi.oai.provider.OAIException;
import eu.clarin.cmdi.oai.provider.Record;
import eu.clarin.cmdi.oai.provider.RecordList;
import eu.clarin.cmdi.oai.provider.Repository;
import eu.clarin.cmdi.oai.provider.SetSpecDesc;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection;
import eu.clarin.cmdi.virtualcollectionregistry.model.VirtualCollection_;

class VirtualColletionRegistryOAIRepository implements Repository {
    private static final Logger logger =
        LoggerFactory.getLogger(VirtualColletionRegistryOAIRepository.class);

    private final static class CMDIMetadataFormat implements MetadataFormat {
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
        public void writeObject(XMLStreamWriter stream, Object item)
                throws XMLStreamException {
            final VirtualCollectionRegistry registry =
                VirtualCollectionRegistry.instance();
            final VirtualCollection vc = (VirtualCollection) item;
            registry.getMarshaller().marshalAsCMDI(stream, vc);
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
        Date result = null;
        try {
            EntityManager em = registry.getDataStore().getEntityManager();
            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaQuery<Date> cq = cb.createQuery(Date.class);
            Root<VirtualCollection> root = cq.from(VirtualCollection.class);
            cq.select(root.get(VirtualCollection_.modifedDate));
            cq.orderBy(cb.asc(root.get(VirtualCollection_.modifedDate)));

            em.getTransaction().begin();
            TypedQuery<Date> q = em.createQuery(cq);
            q.setMaxResults(1);
            q.setLockMode(LockModeType.READ);
            result = q.getSingleResult();
            em.getTransaction().commit();
        } catch (NoResultException e) {
            /* IGNORE */
        } catch (Exception e) {
            logger.debug("error fetching earliest timestamp", e);
        } finally {
            registry.getDataStore().closeEntityManager();
        }
        return result;
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
        return 0 /* COMPRESSION_GZIP|COMPRESSION_DEFLATE */;
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
    public Set<DublinCoreConverter> getDublinCoreConverters() {
        Set<DublinCoreConverter> converters =
            new HashSet<DublinCoreConverter>();
        converters.add(new DublinCoreAdapter() {
            @Override
            public boolean canProcessResource(Class<?> clazz) {
                return clazz.isAssignableFrom(VirtualCollection.class);
            }

            @Override
            public String getTitle(Object resource) {
                return ((VirtualCollection) resource).getName();
            }

            @Override
            public String getIdentifier(Object resource) {
                return ((VirtualCollection) resource)
                    .getPersistentIdentifier().createURI();
            }

            @Override
            public Date getDate(Object resource) {
                return ((VirtualCollection) resource).getCreationDate();
            }

            @Override
            public String getCreator(Object resource) {
                final VirtualCollection vc = (VirtualCollection) resource;
                if (vc.getCreator() != null) {
                    return vc.getCreator().getName();
                }
                return null;
            }

            @Override
            public String getDescription(Object resource) {
                return ((VirtualCollection) resource).getDescription();
            }
        });
        return converters;
    }

    @Override
    public Set<MetadataFormat> getCustomMetadataFormats() {
        Set<MetadataFormat> formats = new HashSet<MetadataFormat>();
        formats.add(new CMDIMetadataFormat());
        return formats;
    }

    @Override
    public Set<SetSpecDesc> getSetDescs() {
        return null;
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
            
            em.getTransaction().begin();
            TypedQuery<Long> q1 = em.createQuery(cq1);
            TypedQuery<VirtualCollection> q2 = em.createQuery(cq2);
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
                    /*
                     *  XXX: force fetching of resources in case of "cmdi"
                     *  prefix.
                     */
                    if ("cmdi".equals(prefix) && !headerOnly) {
                        vc.getResources().size();
                    }
                }
                boolean hasMore = (offset + vcs.size()) < count;
                return new RecordList(records, offset, hasMore, (int) count);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new OAIException("error fetching records", e);
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

    private final class RecordFullImpl implements Record {
        private final VirtualCollection vc;

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
            return null;
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

    private final class RecordHeaderImpl implements Record {
        private final Long id;
        private final Date datestamp;
        private final boolean deleted;

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
            return null;
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
