/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarin.cmdi.virtualcollectionregistry.gui;

import java.sql.Date;
import java.text.ParseException;
import java.util.Locale;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.wicket.util.convert.IConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author twagoo
 */
public class DateConverter implements IConverter {
    
    private final static Logger logger = LoggerFactory.getLogger(DateConverter.class);
    private static final FastDateFormat DF = FastDateFormat.getInstance("yyyy-MM-dd");
    
    @Override
    public String convertToString(Object o, Locale locale) {
        return DF.format((Date) o);
    }
    
    @Override
    public Object convertToObject(String s, Locale locale) {
        try {
            return DF.parse(s);
        } catch (ParseException ex) {
            logger.error("Could not parse date {}", s, ex);
            return null;
        }
    }
}
