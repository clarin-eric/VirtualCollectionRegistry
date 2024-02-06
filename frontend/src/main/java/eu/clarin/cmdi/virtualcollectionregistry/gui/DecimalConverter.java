package eu.clarin.cmdi.virtualcollectionregistry.gui;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.wicket.util.convert.IConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

@SuppressWarnings("serial")
public class DecimalConverter implements IConverter {

        private final static Logger logger = LoggerFactory.getLogger(eu.clarin.cmdi.virtualcollectionregistry.gui.DateConverter.class);
        private static final DecimalFormat DF = new DecimalFormat("#0.0");

        private final DecimalFormat activeDf;

        public DecimalConverter() {
            activeDf = DF;
        }

        public DecimalConverter(DecimalFormat customDf) {
            activeDf = customDf;
        }

        @Override
        public String convertToString(Object o, Locale locale) {
            return DF.format(o);
        }

        @Override
        public Object convertToObject(String s, Locale locale) {
            try {
                return DF.parse(s);
            } catch (ParseException ex) {
                logger.error("Could not parse decimal {}", s, ex);
                return null;
            }
        }
    }