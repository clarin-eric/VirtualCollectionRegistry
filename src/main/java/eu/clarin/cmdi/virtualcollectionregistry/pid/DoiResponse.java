package eu.clarin.cmdi.virtualcollectionregistry.pid;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

public class DoiResponse {
    private static final Logger logger = LoggerFactory.getLogger(DoiResponse.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String parseDoiFromResponse(String json) throws JsonProcessingException {
        String pid = null;
        try {
            Container response = mapper.readValue(json, Container.class);
            if(response.data.id == null) {
                throw new NullPointerException("Response data.id field cannot be null");
            }
            pid = response.data.id;
        } catch(JsonProcessingException ex) {
            logger.info("Response JSON: "+json);
            throw ex;
        }
        return pid;
    }

    @XmlRootElement()
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Container {
        public Data data = null;
       // public List<DoiRequest.Error> errors = null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Error {
        public String status = null;
        public String title = null;
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
        public String prefix = null;
        public String suffix = null;
        public String state = null;
        //public List<Identifier> identifiers = null;
        public String url = null;
        //Creators
        public List<Creator> creators = null;
        //Contributors??
        //Titles
        public List<Title> titles = null;
        //Publisher
        public String publisher = "CLARIN Virtual Collection Registry";
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
        public List<String> affiliation = null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Title {
        public String title = null;
        public String titleType = null;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Types {
        public String resourceType = "Virtual Collection";
        public String resourceTypeGeneral= "Collection";
    }
}
