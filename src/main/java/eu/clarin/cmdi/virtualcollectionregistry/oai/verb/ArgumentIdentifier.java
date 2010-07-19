package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

import java.util.regex.Pattern;

public final class ArgumentIdentifier implements Argument {
    private static final Pattern identifierRegEx =
        Pattern.compile("oai:[a-z][a-z\\d\\-]*(\\.[a-z][a-z\\d\\-]*)+:" +
                        "[\\w\\.!~\\*'\\(\\);/\\?:&=\\+\\$,%]+",
                        Pattern.CASE_INSENSITIVE);
    private final boolean isRequired;
    
    public ArgumentIdentifier(boolean isRequired) {
        this.isRequired = isRequired;
    }

    @Override
    public String getName() {
        return DefaultArguments.ARG_IDENTIFIER;
    }

    @Override
    public boolean isRequired() {
        return isRequired;
    }

    @Override
    public boolean checkArgument(String value) {
        return identifierRegEx.matcher(value).matches();
    }

} // class ArgumentIdentifier
