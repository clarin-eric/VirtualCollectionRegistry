/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarin.cmdi.virtualcollectionregistry.gui;

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
@SuppressWarnings("serial")
public class DateConverter implements IConverter {

    private final static Logger logger = LoggerFactory.getLogger(DateConverter.class);
    private static final FastDateFormat DF = FastDateFormat.getInstance("yyyy-MM-dd");

    public static final FastDateFormat DF_TIMESTAMP = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    private final FastDateFormat activeDf;

    public DateConverter() {
        activeDf = DF;
    }

    public DateConverter(FastDateFormat customDf) {
        activeDf = customDf;
    }

    @Override
    public String convertToString(Object o, Locale locale) {
        if(o instanceof java.sql.Date) {
            return DF.format((java.util.Date) o);
        }
        return DF.format(o);
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
