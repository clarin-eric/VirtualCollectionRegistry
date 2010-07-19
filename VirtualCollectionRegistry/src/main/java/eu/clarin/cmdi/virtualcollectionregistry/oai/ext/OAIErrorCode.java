package eu.clarin.cmdi.virtualcollectionregistry.oai.ext;

public enum OAIErrorCode {
    BAD_ARGUMENT,
    BAD_RESUMPTION_TOKEN,
    BAD_VERB,
    CANNOT_DISSERMINATE_FORMAT,
    ID_DOES_NOT_EXIST,
    NO_RECORDS_MATCH,
    NO_METADATA_FORMATS,
    NO_SET_HIERARCHY;

    public static String toXmlString(OAIErrorCode code) {
        switch (code) {
        case BAD_ARGUMENT:
            return "badArgument";
        case BAD_RESUMPTION_TOKEN:
            return "badResumptionToken";
        case BAD_VERB:
            return "badVerb";
        case CANNOT_DISSERMINATE_FORMAT:
            return "cannotDisserminateFormat";
        case ID_DOES_NOT_EXIST:
            return "idDoesNotExist";
        case NO_RECORDS_MATCH:
            return "noRecordsMatch";
        case NO_METADATA_FORMATS:
            return "noMetadataFormats";
        case NO_SET_HIERARCHY:
            return "noSetHierarchy";
        default:
            // cannot happen
            throw new InternalError("invalid OAIErrorCode");
        }
    }

} // enum OAIError
