package com.datadelivery.WorldCupPool2018.service;

import com.datadelivery.WorldCupPool2018.DBConnectionHelper;
import com.datadelivery.WorldCupPool2018.object.GraphColumn;
import com.datadelivery.WorldCupPool2018.object.GraphResponseBody;
import com.datadelivery.WorldCupPool2018.object.GraphRow;
import com.datadelivery.WorldCupPool2018.object.GraphRowValue;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
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
    MongoCollection userMatchCollection = database.getCollection("UserMatches");

    MongoCursor<Document> userCursor = userCollection.find().iterator();
    int id = 0;
    while (userCursor.hasNext()) {
      id++;
      Document obj = userCursor.next();
      players.add(new GraphColumn(Integer.toString(id), obj.getString("name")));
    }

    MongoCursor<String> matchCursor = userMatchCollection.distinct("match", String.class).iterator();
    MongoCursor<Document> userMatchCursor = null;

    List<GraphRow> balance = Lists.newArrayList();
    try {
      while (matchCursor.hasNext()) {
        String matchId = matchCursor.next();
        BasicDBObject query = new BasicDBObject();
        query.put("match", matchId);

        GraphRow match = new GraphRow();
        List<GraphRowValue> value = Lists.newArrayList();
        value.add(new GraphRowValue(Float.valueOf(matchId)));
        userMatchCursor = userMatchCollection.find(query).iterator();
        while (userMatchCursor.hasNext()) {
          Document um = userMatchCursor.next();
          if (um.get("totalBalance") != null) {
            value.add(new GraphRowValue(new Float(um.getDouble("totalBalance"))));
          }
        }
        match.setC(value);
        balance.add(match);
      }

      result.setCols(players);
      result.setRows(balance);
    }
    finally {
      userMatchCursor.close();
      matchCursor.close();
    }
    return result;
  }

  public String getFixtures() {
    RestTemplate restTemplate = new RestTemplate();
    String result = restTemplate.getForObject("http://api.football-data.org/v1/competitions/{id}/fixtures", String.class, "467");
    System.out.println("Fixtures: " + result);
    return result;
  }

  public static void main(final String[] arg) {
    try {
      DataService serv = new DataService();
        serv.getBalance();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
