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


@SuppressWarnings("unchecked")
public class ParseICSUtil {

    public void getData(List<Subject> subjects, Context context) throws IOException, ParseException, ParserException {

        DatabaseHandler handler = new DatabaseHandler(context);

        for (Subject subject:subjects) {
            handler.addContact(subject);
        }
    }
}
