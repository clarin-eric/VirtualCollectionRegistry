package eu.clarin.cmdi.virtualcollectionregistry;

import java.util.Properties;

public class I18n {

    private static I18n _instance = null;

    public static I18n getInstance() {
        if(_instance == null) {
            _instance = new I18n();
        }
        return _instance;
    }

    private Properties props = new Properties();

    private I18n() {

    }

    public String getProperty(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }
}
