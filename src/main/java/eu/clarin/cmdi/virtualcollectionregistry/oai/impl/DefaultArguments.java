package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.Argument;


final class DefaultArguments {
    public static final String ARG_FROM            = "from";
    public static final String ARG_IDENTIFIER      = "identifier";
    public static final String ARG_METADATAPREFIX  = "metadataPrefix";
    public static final String ARG_RESUMPTIONTOKEN = "resumptionToken";
    public static final String ARG_SET             = "set";
    public static final String ARG_UNTIL           = "until";
    public static final Argument FROM =
        new ArgumentDate(ARG_FROM);
    public static final Argument METADATAPREFIX =
        new ArgumentMetadataPrefix();
    public static final Argument RESUMPTIONTOKEN =
        new ArgumentResumptionToken();
    public static final Argument SET =
        new ArgumentSet();
    public static final Argument UNTIL =
        new ArgumentDate(ARG_UNTIL);
} // class DefaultArguments
