package com.datadelivery.WorldCupPool2018;

import com.datadelivery.WorldCupPool2018.service.DataService;
import com.google.common.collect.Lists;
import com.mongodb.client.*;
import org.bson.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
public class UserController {

    DataService dataService;

    @Autowired
    public void set(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/addUser")
    public String addUser() {
        return "user";
    }

    @PostMapping("/saveUser")
    public String saveDetails(@RequestParam("name") String name,
                              @RequestParam("team") String team,
                              @RequestParam("password") String password, Model model) {
        // write your code to save details

        MongoDatabase database =  dataService.initConnection();
        if (!name.isEmpty() && !team.isEmpty() && !password.isEmpty()) {
            MongoCollection userCollection = database.getCollection("Users");

            Document newUser = new Document();
            newUser.append("name", name);


            // Check for existing user

            FindIterable<Document> iterDoc = userCollection.find(newUser);

            if (!iterDoc.iterator().hasNext()){
                newUser.append("team", team);
                newUser.append("password", password);
                userCollection.insertOne(newUser);
                System.out.println("Added new user: " + name + ".");
                model.addAttribute("comment", "Added new user: " + name + ".");
            }
            else {
                model.addAttribute("comment", name + " already exists!");
            }
        }

        return addUser();
    }

    @GetMapping("/viewUser")
    public String viewUser(Model result) {

        MongoDatabase database = dataService.initConnection();
        MongoCollection userCollection = database.getCollection("Users");

        MongoCursor<Document> cursor = userCollection.find().iterator();
        List<Document> userDetail = Lists.newArrayList();
        try {
            while (cursor.hasNext()) {
                userDetail.add(cursor.next());
            }
            result.addAttribute("userDetail", userDetail);
        }
        finally {
            cursor.close();
        }
        return "userDetail";
    }
}
