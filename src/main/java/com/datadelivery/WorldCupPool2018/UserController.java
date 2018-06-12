package com.datadelivery.WorldCupPool2018;

import com.mongodb.client.*;
import org.bson.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;

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

        if (!name.isEmpty() && !team.isEmpty() && !password.isEmpty()) {
            // Creating a Mongo client
            MongoClient mongo = new MongoClient("localhost", 27017);

            // Creating Credentials
            MongoCredential credential;
            credential = MongoCredential.createCredential("sampleUser", "myDb",
                    "password".toCharArray());
            System.out.println("Connected to the database successfully");

            // Accessing the database
            MongoDatabase database = mongo.getDatabase("WC_2018");

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
}
