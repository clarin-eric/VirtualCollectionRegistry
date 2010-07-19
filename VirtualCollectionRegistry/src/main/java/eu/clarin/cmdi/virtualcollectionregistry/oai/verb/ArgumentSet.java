package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.regex.Pattern;

public final class ArgumentSet implements Argument {
    private static final Pattern setSpecRegEx =
        Pattern.compile("[\\w\\.!~\\*'\\(\\)]+(:[\\w\\.!~\\*'\\(\\)]+)*",
                        Pattern.CASE_INSENSITIVE);

    @Override
    public String getName() {
        return DefaultArguments.ARG_SET;
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean checkArgument(String value) {
        return setSpecRegEx.matcher(value).matches();
    }

} // class ArgumentSet
