package com.datadelivery.WorldCupPool2018;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;

@SpringBootApplication
public class WorldCupPool2018Application {

	public static void main(String[] args) {

		SpringApplication.run(WorldCupPool2018Application.class, args);

	}
}
