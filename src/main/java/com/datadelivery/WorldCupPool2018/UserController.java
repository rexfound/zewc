package com.datadelivery.WorldCupPool2018;

import com.google.common.collect.Lists;
import com.mongodb.client.*;
import org.bson.*;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
public class UserController {

    @GetMapping("/addUser")
    public String addUser() {
        return "user";
    }

    @PostMapping("/saveUser")
    public String saveDetails(@RequestParam("name") String name,
                              @RequestParam("team") String team,
                              @RequestParam("password") String password) {
        // write your code to save details

        MongoDatabase database = DBConnectionHelper.getConnection();
        if (!name.isEmpty() && !team.isEmpty() && !password.isEmpty()) {
            MongoCollection userCollection = database.getCollection("Users");

            Document newUser = new Document();
            newUser.append("name", name);
            newUser.append("team", team);
            newUser.append("password", password);


            // Check for existing user

            FindIterable<Document> iterDoc = userCollection.find(newUser);

            if (!iterDoc.iterator().hasNext()){
                userCollection.insertOne(newUser);
                System.out.println("Added new user: " + name + ".");
            }
        }

        return "user";
    }

    @GetMapping("/viewUser")
    public String viewUser(Model result) {

        MongoDatabase database = DBConnectionHelper.getConnection();
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
