package rest.controller;

import com.google.common.collect.Lists;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import rest.client.TTUSchedule;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    @Autowired
    private TTUSchedule ttuSchedule;


    @RequestMapping
    public ResponseEntity<Map<String, Calendar>> Schedules(@RequestParam(value="group", required=true) String[] group) throws IOException, ParserException {
        return new ResponseEntity<>(ttuSchedule.getCalendars(Lists.newArrayList(group)), HttpStatus.OK);
    }
}
