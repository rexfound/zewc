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

import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;


@Controller
public class UserController {

    private DataService dataService;

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
                              @RequestParam("password") String password,
                              Model model) {
        // write your code to save details

        MongoDatabase database =  dataService.initConnection();
        if (!name.isEmpty() && !team.isEmpty() && !password.isEmpty()) {
            MongoCollection userCollection = database.getCollection("Users");

            Document newUser = new Document();
            newUser.append("name", name);
            newUser.append("team", team);
            newUser.append("password", password);


            // Check for existing user

            FindIterable<Document> iterDoc = userCollection.find(newUser);

            if (!iterDoc.iterator().hasNext()){
                newUser.append("totalBalance", -5.00);
                userCollection.insertOne(newUser);
                model.addAttribute("comment", "Successfully added user: " + name);
                System.out.println("Added new user: " + name + ".");
            }
        }

        return "user";
    }

    @GetMapping("/viewUser")
    public String viewUser(Model result) {

        MongoDatabase database = dataService.initConnection();
        MongoCollection userCollection = database.getCollection("Users");

        MongoCursor<Document> cursor = userCollection.find().sort(orderBy(descending("totalBalance"))).iterator();
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
