package rest;

import com.google.common.collect.Lists;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import rest.client.TTUSchedule;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private TTUSchedule ttuSchedule;

    @RequestMapping(method=RequestMethod.GET)
    public @ResponseBody
    List<Calendar> getSchedule(@RequestParam(value="group", required=true) String group) throws IOException, ParserException {
        ttuSchedule.sync(Lists.newArrayList(group));
        return ttuSchedule.getCalendars();
    }

}
