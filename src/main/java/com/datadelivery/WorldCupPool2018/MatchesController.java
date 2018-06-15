package com.datadelivery.WorldCupPool2018;

import com.datadelivery.WorldCupPool2018.service.DataService;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class MatchesController {

    DataService dataService;
    Pattern URL_PATT =Pattern.compile("http.*\\/fixtures\\/(\\d+)");
    private final double BET_AMOUNT = 2.00;

    @Autowired
    public void set(DataService dataService) {
        this.dataService = dataService;
    }


    @GetMapping("/showMatches")
    public String showMatches(Model result) {
        result.addAttribute("matches", getFixtures());
        return "matches";
    }

    @GetMapping("/betDetails")
    public String betDetails(Model result) {
        MongoDatabase database = dataService.initConnection();
        MongoCollection userCollection = database.getCollection("Users");

        MongoCursor<Document> cursor = userCollection.find().iterator();
        List<Document> userDetail = Lists.newArrayList();
        try {
            while (cursor.hasNext()) {
                userDetail.add(cursor.next());
            }
            result.addAttribute("users", userDetail);
        }
        finally {
            cursor.close();
        }
        return "bet";
    }

    @PostMapping("/placeBet")
    public String placeBet(@RequestParam("seqID") int seqID,
                            @RequestParam("matchID") String matchID, @RequestParam("name") String name,
                              @RequestParam("team") String team, @RequestParam("password") String password, Model model) {
        // write your code to save details
        MongoDatabase database =  dataService.initConnection();
        if (!name.isEmpty() && !team.isEmpty() && !password.isEmpty()) {
            MongoCollection userCollection = database.getCollection("Users");
            MongoCollection userMatchesCollection = database.getCollection("UserMatches");

            Document newPick = new Document();
            newPick.append("seqID", seqID);
            newPick.append("match", matchID);
            newPick.append("user", name);

            // Check password
            BasicDBObject userDocument = new BasicDBObject();
            userDocument.append("password", password);
            userDocument.append("name", name);

            if (userCollection.find(userDocument).iterator().hasNext()) {
                System.out.println("Authenticated: " + name);

                // Check for existing pick

                FindIterable<Document> iterDoc = userMatchesCollection.find(newPick);

                if (iterDoc.iterator().hasNext()){
                    userMatchesCollection.deleteOne(newPick);
                    newPick.append("teamPick", team);
                    newPick.append("betAmount", BET_AMOUNT);
                    newPick.append("totalBalance", null);
                    userMatchesCollection.insertOne(newPick);
                    model.addAttribute("comment", "Successfully updated " + team + " under " + name);
                }
                else {
                    newPick.append("teamPick", team);
                    newPick.append("betAmount", BET_AMOUNT);
                    newPick.append("totalBalance", null);
                    userMatchesCollection.insertOne(newPick);
                    System.out.println("Added new pick: " + name + ".");
                    model.addAttribute("comment", "Successfully picked " + team + " under " + name);
                }
            }
            else {
                System.out.println("Authentication failed");
                model.addAttribute("comment", "Incorrect Password");
            }

        }
        return showMatches(model);
    }

    public List<Object> getFixtures() {
        RestTemplate restTemplate = new RestTemplate();

        // Create the request body as a MultiValueMap
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();

        body.add("X-Auth-Token", "d1a78d030eac433ebf8f7a14c1eb6e73");

        // Note the body object as first parameter!
        HttpEntity<?> httpEntity = new HttpEntity<Object>(body);

        ResponseEntity<String> result = restTemplate.exchange(
                "http://api.football-data.org/v1/competitions/{id}/fixtures", HttpMethod.GET, httpEntity, String.class, "467");

        JsonFactory factory = new JsonFactory();

        List<Object> fixtures = new ArrayList<Object>();
        Map<String, String> map = new HashMap<>();

        try {
            JsonParser parser = factory.createParser(result.getBody());
            int sequenceID = 1;
            while(!parser.isClosed()){
                JsonToken jsonToken = parser.nextToken();
                if(JsonToken.FIELD_NAME.equals(jsonToken)){

                    String fieldName = parser.getCurrentName();
                    parser.nextToken();
                    if (fieldName.equals("odds")) {
                        fixtures.add(map);
                        map = new HashMap<>();

                    }
                    else if (fieldName.equals("href")) {
                        if (parser.getValueAsString().contains("fixtures/")) {
                            String matchURL = parser.getValueAsString();
                            Matcher mtr = URL_PATT.matcher(matchURL);
                            if (mtr.matches()) {
                                map.put("MatchID", mtr.group(1));
                                map.put("seqID", Integer.toString(sequenceID));
                                sequenceID++;
                            }
                            map.put("Competition", matchURL);
                        }
                    }
                    else if (fieldName.equals("date") || fieldName.equals("status") ||
                            fieldName.equals("homeTeamName") || fieldName.equals("awayTeamName") ||
                                fieldName.equals("goalsHomeTeam") || fieldName.equals("goalsAwayTeam")) {
                        if (!map.containsKey(fieldName)) {
                            map.put(fieldName, parser.getValueAsString());
                        }
                    }
                }
            }
        }
        catch (IOException ioe) {
            //do nothing
        }

        return fixtures;
    }
}
