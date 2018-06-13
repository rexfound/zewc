package com.datadelivery.WorldCupPool2018.service;

import com.datadelivery.WorldCupPool2018.DBConnectionHelper;
import com.datadelivery.WorldCupPool2018.object.GraphColumn;
import com.datadelivery.WorldCupPool2018.object.GraphResponseBody;
import com.datadelivery.WorldCupPool2018.object.GraphRow;
import com.datadelivery.WorldCupPool2018.object.GraphRowValue;
import com.google.common.collect.Lists;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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
    System.out.println("Fixtures: " + result);
    return result;
  }




}
