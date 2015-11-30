package rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vadimstrukov on 11/17/15.
 */
@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    @RequestMapping(method=RequestMethod.GET)
    public @ResponseBody
    String getSchedule(@RequestParam(value="group", required=true) String group) throws IOException {
        Map<String, Integer> map = new HashMap<>();
        System.setProperty("https.protocols", "TLSv1,SSLv3,SSLv2Hello");
        Document document = Jsoup.connect("https://ois.ttu.ee/portal/page?_pageid=35,435155&_dad=portal&_schema=PORTAL&i=2&e=-1&e_sem=161&a=1&b=1&c=-1&d=-1&k=&q=neto&g=").timeout(10000).get();//b=1 - stats, b=2 kaug
        Elements spans = document.select("span").select("span:has(a)");
        for (int i = 0; i < spans.size(); i++) {
            Pattern p = Pattern.compile("g=(\\w+)");
            Matcher m = p.matcher(spans.get(i).attr("onclick"));
            m.find();
            map.put(spans.get(i).select("a").html(), Integer.valueOf(m.group(1)));
        }
        String url = String.format("https://ois.ttu.ee/pls/portal/tunniplaan.PRC_EXPORT_DATA?p_page=view_plaan&pn=i&pv=2&pn=e_sem&pv=161&pn=e&pv=-1&pn=b&pv=1&pn=g&pv=%1$d&pn=is_oppejoud&pv=false&pn=q&pv=1", map.get(group));
        URL uri = new URL(url);
        URLConnection urlConnection = uri.openConnection();
        InputStream in = urlConnection.getInputStream();
        return org.apache.commons.io.IOUtils.toString(in, StandardCharsets.UTF_8);
    }
}
