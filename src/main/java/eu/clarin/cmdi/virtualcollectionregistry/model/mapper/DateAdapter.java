package eu.clarin.cmdi.virtualcollectionregistry.model.mapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {
    private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public Date unmarshal(String date) throws Exception {
        Date utilDate = df.parse(date);
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(utilDate);
        return cal.getTime();
    }

    public String marshal(Date date) throws Exception {
        return df.format(date);
    }

} // class DateAdapter
