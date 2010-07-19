package eu.clarin.cmdi.virtualcollectionregistry.oai.ext;

import java.util.Date;

public interface ResumptionToken {

    public String getId();

    public void setExpirationDate(long expirationDate);

    public Date getExpirationDate();

    public boolean checkExpired(long timestamp);

    public void setCursor(int cursor);

    public int getCursor();

    public void setCompleteListSize(int completeListSize);

    public int getCompleteListSize();

    public void setProperty(String key, Object value);

    public Object getProperty(String key);

} // interface ResumptionToken
