package eu.clarin.cmdi.virtualcollectionregistry.oai.verb;

public interface Argument {

    public abstract String getName();

    public abstract boolean isRequired();

    public abstract boolean checkArgument(String value);

} // interface Argument
