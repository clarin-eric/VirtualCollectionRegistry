package eu.clarin.cmdi.virtualcollectionregistry.oai.impl;

import java.util.UUID;

import eu.clarin.cmdi.virtualcollectionregistry.oai.ext.Argument;

final class ArgumentResumptionToken implements Argument {

    @Override
    public String getName() {
        return DefaultArguments.ARG_RESUMPTIONTOKEN;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean checkArgument(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

} // class ArgumentResumptionToken
