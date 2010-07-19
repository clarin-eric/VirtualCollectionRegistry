package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.regex.Pattern;

public final class ArgumentMetadataPrefix implements Argument {
    private static final Pattern metadataPrefixRegEx =
        Pattern.compile("[\\w\\.!~\\*'\\(\\)]+", Pattern.CASE_INSENSITIVE);

    @Override
    public String getName() {
        return DefaultArguments.ARG_METADATAPREFIX;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean checkArgument(String value) {
        return metadataPrefixRegEx.matcher(value).matches();
    }

} // class ArgumentMetadataPrefix
