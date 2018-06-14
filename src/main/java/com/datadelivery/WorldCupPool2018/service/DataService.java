package com.datadelivery.WorldCupPool2018.service;

import com.datadelivery.WorldCupPool2018.DBConnectionHelper;
import com.datadelivery.WorldCupPool2018.object.GraphColumn;
import com.datadelivery.WorldCupPool2018.object.GraphResponseBody;
import com.datadelivery.WorldCupPool2018.object.GraphRow;
import com.datadelivery.WorldCupPool2018.object.GraphRowValue;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by handy.kestury on 6/12/2018.
 */
@Service
public class DataService {

  MongoDatabase database;

  public MongoDatabase initConnection() {
    if (database == null) {
      database = DBConnectionHelper.getConnection();
    }
    return database;
  }

  public GraphResponseBody getBalance() {

    GraphResponseBody result = new GraphResponseBody();

    List<GraphColumn> players = Lists.newArrayList();

    initConnection();
    players.add(new GraphColumn("0", "Match Day"));
    MongoCollection userCollection = database.getCollection("Users");


    MongoCursor<Document> cursor = userCollection.find().iterator();
    int id = 0;
    while (cursor.hasNext()) {
      id++;
      Document obj = cursor.next();
      players.add(new GraphColumn(Integer.toString(id), obj.getString("name")));
    }

    List<GraphRow> balance = Lists.newArrayList();
    for (int j = 0; j <= 5; j++) {
      GraphRow match = new GraphRow();
      List<GraphRowValue> value = Lists.newArrayList();
      value.add(new GraphRowValue(new Float(j)));
      for(int i = 1; i <= id; i++) {
        value.add(new GraphRowValue(new Float(Math.random())));
      }
      match.setC(value);
      balance.add(match);
    }

    result.setCols(players);
    result.setRows(balance);
    return result;
  }

  public String getFixtures() {
    RestTemplate restTemplate = new RestTemplate();
    String result = restTemplate.getForObject("http://api.football-data.org/v1/competitions/{id}/fixtures", String.class, "467");

    JsonFactory factory = new JsonFactory();

    List<Object> fixtures = new ArrayList<Object>();

    try {
        JsonParser parser = factory.createParser(result);

        while(!parser.isClosed()){
            JsonToken jsonToken = parser.nextToken();

            if(JsonToken.FIELD_NAME.equals(jsonToken)){
                Map<String, String> map = new HashMap<>();
                String fieldName = parser.getCurrentName();
                parser.nextToken();
                if (fieldName.equals("odds")) {
                    fixtures.add(map);
                    System.out.println("***");
                }
                else if (fieldName.equals("href")) {
                    if (parser.getValueAsString().contains("fixtures/")) {
                        map.put("Competition", parser.getValueAsString());
                        System.out.println(fieldName + ": " + parser.getValueAsString());
                    }
                }
                else if (fieldName.equals("date") || fieldName.equals("status") ||
                        fieldName.equals("homeTeamName") || fieldName.equals("awayTeamName")) {
                    map.put(fieldName, parser.getValueAsString());
                    System.out.println(fieldName + ": " + parser.getValueAsString());
                }
            }
        }
    }
    catch (IOException ioe) {
        //do nothing
    }

    return "[ {     \"name\":   \"Tiger Nixon\",     \"position\":   \"System Architect\",     \"salary\": \"$3,120\",     \"start_date\": \"2011/04/25\",     \"office\": \"Edinburgh\",     \"extn\":   \"5421\" }, {     \"name\":   \"Garrett Winters\",     \"position\":   \"Director\",     \"salary\": \"$5,300\",     \"start_date\": \"2011/07/25\",     \"office\": \"Edinburgh\",     \"extn\":   \"8422\" } ]";
  }




}
