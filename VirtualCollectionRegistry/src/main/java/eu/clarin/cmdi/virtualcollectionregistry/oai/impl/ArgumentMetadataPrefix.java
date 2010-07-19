package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

import java.util.regex.Pattern;

import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.Argument;

final class ArgumentMetadataPrefix implements Argument {
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
