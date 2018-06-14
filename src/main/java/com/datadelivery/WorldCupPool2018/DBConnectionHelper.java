package com.datadelivery.WorldCupPool2018;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoDatabase;

/**
 * Created by handy.kestury on 6/13/2018.
 */
public enum DBConnectionHelper {
;
  public static MongoDatabase getConnection() {
    MongoClient mongo = new MongoClient("10.0.1.145", 27017);

    // Creating Credentials
    MongoCredential credential;
    credential = MongoCredential.createCredential("sampleUser", "myDb",
        "password".toCharArray());
    System.out.println("Connected to the database successfully");

    // Accessing the database
    MongoDatabase database = mongo.getDatabase("WC_2018");
    return database;
  }
}
