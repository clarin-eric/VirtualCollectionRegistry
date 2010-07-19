package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

import java.util.regex.Pattern;

import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.Argument;

final class ArgumentDate implements Argument {
    private static final Pattern dateRegEx =
        Pattern.compile("\\d{4}-\\d{2}-\\d{2}(T\\d{2}:\\d{2}:\\d{2}Z)?");
    private final String name;
    
    public ArgumentDate(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean checkArgument(String value) {
        return dateRegEx.matcher(value).matches();
    }

} // class ArgumentDate
