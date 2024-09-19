package eu.clarin.cmdi.oai.provider.impl;

import java.util.regex.Pattern;

import eu.clarin.cmdi.oai.provider.ext.Argument;

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
