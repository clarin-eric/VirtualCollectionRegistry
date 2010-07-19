package eu.clarin.cmdi.virtualcollectionregistry.oai.ext;

public interface Argument {

    public String getName();

    public boolean isRequired();

    public boolean checkArgument(String value);

} // interface Argument
