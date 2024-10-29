package eu.clarin.cmdi.virtualcollectionregistry.core.pid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

public class DoiRequest implements PidRequest {

    public final static String DEFAULT_PUBLISHER = "CLARIN";
    public final static String DEFAULT_RESOURCE_TYPE = "Virtual Collection";
    public final static String DEFAULT_GENERAL_RESOURCE_TYPE = "Collection";

    private final ObjectMapper mapper = new ObjectMapper();

    private Container43 request = new Container43();

    public DoiRequest() {
        this.request.data = new Data();
        this.request.data.type = "dois";
        this.request.data.attributes = new Attributes();
        this.request.data.attributes.event = "publish";
    }

    public void setDoi(String doi) { this.request.data.attributes.doi = doi; }
    public void setPrefix(String prefix) {
        this.request.data.attributes.prefix = prefix;
    }
    public void setUrl(String url) {
        this.request.data.attributes.url = url;
    }
    public void setPublicationYear(String year) {
        this.request.data.attributes.publicationYear = year;
    }

    public void addTitle(String title) {
        if(this.request.data.attributes.titles == null) {
            this.request.data.attributes.titles = new LinkedList<>();
        }
        Title t = new Title();
        t.title = title;
        this.request.data.attributes.titles.add(t);
    }

    public void addCreator(String familyName, String givenName) {
        this.addCreator(familyName, givenName, null);
    }

    public void addCreator(String familyName, String givenName, String affiliation) {
        if(this.request.data.attributes.creators == null) {
            this.request.data.attributes.creators = new LinkedList<>();
        }

        Creator c = new Creator();
        c.familyName = familyName;
        c.givenName = givenName;
        c.nameType = "Personal";
        if(affiliation != null) {
            c.affiliation = new LinkedList<>();
            Affiliation a = new Affiliation();
            a.name = affiliation;
            c.affiliation.add(a);
        }
        this.request.data.attributes.creators.add(c);
    }

    @Override
    public String toJsonString() throws JsonProcessingException  {
        return mapper.writeValueAsString(request);
    }

    @XmlRootElement()
    @JsonIgnoreProperties(ignoreUnknown = true)
    //@JsonTypeName("data")
    //@JsonTypeInfo(include= JsonTypeInfo.As.WRAPPER_OBJECT,use= JsonTypeInfo.Id.NAME)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Container43 {
        public Data data = null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        public String id = null;
        public String type = null;
        public Attributes attributes = null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attributes {
        public String doi = null;
        public String event = null;
        public String prefix = null;
        public String suffix = null;
        //public List<Identifier> identifiers = null;
        public String url = null;
        //Creators
        public List<Creator> creators = null;
        //Contributors??
        //Titles
        public List<Title> titles = null;
        //Publisher
        public String publisher = DEFAULT_PUBLISHER;
        //Publication Year
        public String publicationYear;
        //Resource Type
        public Types types = new Types();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Creator {
        public String name = null;
        public String nameType = null;
        public String givenName = null;
        public String familyName = null;
        public List<Affiliation> affiliation = null;
        //Name Identifier
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Affiliation {
        public String name = null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Title {
        public String title = null;
        //public String titleType = null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Types {
        public String resourceType = DEFAULT_RESOURCE_TYPE;
        public String resourceTypeGeneral = DEFAULT_GENERAL_RESOURCE_TYPE;
    }

    /*
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Identifier {
        String identifier;
        String identifierType;
    }
    */
}
