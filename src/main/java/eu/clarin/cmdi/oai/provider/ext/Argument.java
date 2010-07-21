package eu.clarin.cmdi.oai.provider.ext;

public interface Argument {

    public String getName();

    public boolean isRequired();

    public boolean checkArgument(String value);

} // interface Argument
