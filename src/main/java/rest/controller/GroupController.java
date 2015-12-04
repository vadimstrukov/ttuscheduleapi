package rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rest.client.TTUSchedule;

import java.util.Set;

@RestController
@RequestMapping("/groups")
public class GroupController {
    @Autowired
    private TTUSchedule ttuSchedule;

    @RequestMapping
    public Set<String> getAllGroups(){
        return ttuSchedule.getAllGroups();
    }
}
