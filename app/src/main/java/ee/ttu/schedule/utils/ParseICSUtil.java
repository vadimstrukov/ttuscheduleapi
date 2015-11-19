package ee.ttu.schedule.utils;


import android.content.Context;


import ee.ttu.schedule.model.Subject;
import ee.ttu.schedule.service.DatabaseHandler;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * Created by vadimstrukov on 9/30/15.
 */
@SuppressWarnings("unchecked")
public class ParseICSUtil {

    public void getData(String calenderString, Context context) throws IOException, ParseException, ParserException {

        StringReader sin = new StringReader(calenderString);

        CalendarBuilder builder = new CalendarBuilder();

        Calendar calendar = builder.build(sin);

        List<Component> components = calendar.getComponents(Component.VEVENT);

        DatabaseHandler handler = new DatabaseHandler(context);

        for (Component component : components) {

            Subject subject = new Subject();
            subject.setDateStart(new SimpleDateFormat("yyyyMMdd'T'HHmmss").parse(component.getProperty(Property.DTSTART).getValue()));
            subject.setDateEnd(new SimpleDateFormat("yyyyMMdd'T'HHmmss").parse(component.getProperty(Property.DTEND).getValue()));
            subject.setDescr(component.getProperty(Property.DESCRIPTION).getValue());
            subject.setLocation(component.getProperty(Property.LOCATION).getValue());
            subject.setSummary(component.getProperty(Property.SUMMARY).getValue());
            handler.addContact(subject);
        }

    }
}
