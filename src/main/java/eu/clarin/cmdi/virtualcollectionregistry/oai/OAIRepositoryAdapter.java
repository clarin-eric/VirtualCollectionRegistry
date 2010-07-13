package eu.clarin.cmdi.virtualcollectionregistry.oai;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.stream.XMLStreamException;

import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.DublinCoreConverter;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.MetadataFormat;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.OAIRepository;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.Record;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.RecordList;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.SetSpecDesc;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.OAIRepository.DeletedNotion;
import eu.clarin.cmdi.virtualcollectionregistry.oai.repository.OAIRepository.Granularity;
import eu.clarin.cmdi.virtualcollectionregistry.oai.verb.Argument;

public class OAIRepositoryAdapter {
    private final OAIProvider provider;
    private final OAIRepository repository;
    private final ThreadLocal<SimpleDateFormat> sdf =
        new ThreadLocal<SimpleDateFormat>() {
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern(getDatePattern(repository.getGranularity()));
            sdf.setLenient(false);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf;
        }
    };
    private final Set<String> adminEmailAddresses;
    private final Set<MetadataFormat> metadataFormats =
        new HashSet<MetadataFormat>();
    private final Set<SetSpecDesc> setSpecs;
    private final Map<Class<?>, Set<MetadataFormat>> metadataFormatsByClass =
        new HashMap<Class<?>, Set<MetadataFormat>>();

    OAIRepositoryAdapter(OAIProvider provider, OAIRepository repository)
            throws OAIException {
        this.provider = provider;
        this.repository = repository;

        this.adminEmailAddresses = repository.getAdminAddreses();
        if (this.adminEmailAddresses == null) {
            throw new NullPointerException("getAdminAddreses() == null");
        }
        if (this.adminEmailAddresses.isEmpty()) {
            throw new OAIException("admin email addresses are empty");
        }

        // handle dublin core and do some sanity checks
        Set<DublinCoreConverter> converters =
            repository.getDublinCoreConverters();
        if (converters == null) {
            throw new NullPointerException("getDublinCoreConverters() == null");
        }
        if (converters.isEmpty()) {
            throw new OAIException("set of Dublin Core converters is empty");
        }
        this.metadataFormats.add(new DublinCoreMetadataFormat(converters));

        // handle metadata custom formats and do some sanity checks
        Set<MetadataFormat> formats = repository.getCustomMetadataFormats();
        if (formats != null) {
            Set<String> prefixes = new HashSet<String>();
            for (MetadataFormat format : formats) {
                String prefix = format.getPrefix();
                if (prefix == null) {
                    throw new NullPointerException("metadata format needs " +
                            "prefix non-null prefix");
                }
                if (prefixes.contains(prefix)) {
                    throw new OAIException("metadata prefix must be unique " +
                            "for a repository: " + prefix);
                }
                if ("oai_dc".equals(prefix)) {
                    throw new OAIException("dublin core metadata format must " +
                            "not be in the set of supported custom metadata " +
                            "formats");
                }
                // add format
                this.metadataFormats.add(format);
            }
        }

        // cache set specs
        Set<SetSpecDesc> tmp = repository.getSetDescs();
        if ((tmp != null) && tmp.isEmpty()) {
            tmp = null;
        }
        this.setSpecs = tmp;
    }

    public OAIProvider getProvider() {
        return provider;
    }

    public String getId() {
        return repository.getId();
    }

    public String getName() {
        return repository.getName();
    }

    public Set<String> getAdminEmailAddresses() {
        return adminEmailAddresses;
    }

    public String getEarliestTimestamp() {
        Date date = repository.getEarliestTimestamp();
        if (date == null) {
            date = new Date();
        }
        return formatDate(date);
    }

    public DeletedNotion getDeletedNotion() {
        return repository.getDeletedNotion();
    }

    public Granularity getGranularity() {
        return repository.getGranularity();
    }

    public boolean isSupportingCompressionMethod(int method) {
        int methods = repository.getCompressionMethods();
        return (methods & method) > 0;
    }

    public String getDescription() {
        String description = repository.getDescription();
        if ((description != null) && !description.isEmpty()) {
            return description;
        }
        return null;
    }

    public String getSampleRecordId() {
        return createRecordId(repository.getSampleRecordLocalId());
    }

    public Set<MetadataFormat> getMetadataFormats() {
        return metadataFormats;
    }

    public Set<MetadataFormat> getMetadataFormats(Record record) {
        Class<?> clazz = record.getItemClass();
        synchronized (metadataFormatsByClass) {
            Set<MetadataFormat> result = metadataFormatsByClass.get(clazz);
            if (result == null) {
                result = new HashSet<MetadataFormat>();
                for (MetadataFormat format : metadataFormats) {
                    if (format.canWriteClass(clazz)) {
                        result.add(format);
                    }
                }
                if (result.isEmpty()) {
                    result = Collections.emptySet();
                }
                metadataFormatsByClass.put(clazz, result);
            }
            return result;
        } // synchronized
    }

    public MetadataFormat getMetadataFormatByPrefix(String prefix) {
        for (MetadataFormat format : metadataFormats) {
            if (prefix.equals(format.getPrefix())) {
                return format;
            }
        }
        return null;
    }

    public Set<SetSpecDesc> getSetSpecs() {
        return setSpecs;
    }

    public boolean isUsingSets() {
        return setSpecs != null;
    }

    public Object parseArgument(String name, String value) {
        Object result = null;
        if (name.equals(Argument.ARG_IDENTIFIER)) {
            String localId = extractLocalId(value);
            if (localId != null) {
                result = repository.parseLocalId(localId);
            }
        } else if (name.equals(Argument.ARG_FROM)
                || name.equals(Argument.ARG_UNTIL)) {
            /*
             * First try to parse date format in repository default format. If
             * this fails and repository supports SECONDS granularity try also
             * DAYS granularity
             */
            SimpleDateFormat parser = sdf.get();
            ParsePosition pos = new ParsePosition(0);
            Date date = parser.parse(value, pos);
            if ((date == null) &&
                (repository.getGranularity() == Granularity.SECONDS)) {
                // re-try with DAYS granularity
                pos.setIndex(0);
                parser.applyPattern(getDatePattern(Granularity.DAYS));
                date = parser.parse(value, pos);
                // reset pattern, success check if done below
                parser.applyPattern(getDatePattern(Granularity.SECONDS));
            }

            // make sure input has not been parsed partly
            if ((date != null) && (pos.getIndex() == value.length())) {
                result = date;
            }
        } else {
            result = value;
        }
        return result;
    }

    public String createRecordId(Object localId) {
        StringBuilder sb = new StringBuilder("oai:");
        sb.append(repository.getId());
        sb.append(":");
        sb.append(repository.unparseLocalId(localId));
        return sb.toString();
    }

    public String formatDate(Date date) {
        return sdf.get().format(date);
    }

    public Record getRecord(Object localId, boolean headerOnly)
            throws OAIException {
        return repository.getRecord(localId, headerOnly);
    }

    public RecordList getRecords(String prefix, Date from, Date until,
            String set, int offset, boolean headerOnly) throws OAIException {
        return repository.getRecords(prefix, from, until, set, offset,
                headerOnly);
    }

    public ResumptionToken createResumptionToken() {
        return provider.createResumptionToken(-1);
    }

    public ResumptionToken getResumptionToken(String id) {
        return provider.getResumptionToken(id, -1);
    }

    public void writeRecordHeader(OAIOutputStream out, Record record)
            throws OAIException {
        out.writeStartElement("header");
        if (record.isDeleted()) {
            out.writeAttribute("status", "deleted");
        }
        out.writeStartElement("identifier");
        out.writeCharacters(createRecordId(record.getLocalId()));
        out.writeEndElement(); // identifier element
        out.writeStartElement("datestamp");
        out.writeDate(record.getDatestamp());
        out.writeEndElement(); // datestamp element
        List<String> setSpecs = record.getSetSpecs();
        if ((setSpecs != null) && !setSpecs.isEmpty()) {
            for (String setSpec : setSpecs) {
                out.writeStartElement("setSpec");
                out.writeCharacters(setSpec);
                out.writeEndElement(); // setSpec element
            }
        }
        out.writeEndElement(); // header element
    }

    public void writeRecord(OAIOutputStream out, Record record,
            MetadataFormat format) throws OAIException {
        try {
            out.writeStartElement("record");
            writeRecordHeader(out, record);
            if (!record.isDeleted()) {
                out.writeStartElement("metadata");
                format.writeObject(out.getXMLStreamWriter(), record.getItem());
                out.writeEndElement(); // metadata element
            }
            out.writeEndElement(); // record element
        } catch (XMLStreamException e) {
            throw new OAIException("error writing record", e);
        }
    }

    public void writeResumptionToken(OAIOutputStream out, ResumptionToken token)
            throws OAIException {
        out.writeStartElement("resumptionToken");
        out.writeAttribute("expirationDate",
                formatDate(token.getExpirationDate()));
        if (token.getCursor() >= 0) {
            out.writeAttribute("cursor", Integer.toString(token.getCursor()));
        }
        if (token.getCompleteListSize() > 0) {
            out.writeAttribute("completeListSize",
                    Integer.toString(token.getCompleteListSize()));
        }
        out.writeCharacters(token.getId());
        out.writeEndElement(); // resumptionToken element
    }

    private String extractLocalId(String identifier) {
        int pos1 = identifier.indexOf(':');
        if (pos1 != -1) {
            int pos2 = identifier.indexOf(':', pos1 + 1);
            if (pos2 != -1) {
                // check of repository id matches
                String id = repository.getId();
                if (identifier.regionMatches(pos1 + 1, id, 0, id.length())) {
                    return identifier.substring(pos2 + 1);
                }
            }
        }
        return null;
    }

    private static String getDatePattern(Granularity granularity) {
        switch (granularity) {
        case DAYS:
            return "yyyy-MM-dd";
        default:
            return "yyyy-MM-dd'T'HH:mm:ss'Z'";
        }
    }

} // class OAIRepositoryAdapter
