package com.datadelivery.WorldCupPool2018.service;

import com.datadelivery.WorldCupPool2018.DBConnectionHelper;
import com.datadelivery.WorldCupPool2018.object.GraphColumn;
import com.datadelivery.WorldCupPool2018.object.GraphResponseBody;
import com.datadelivery.WorldCupPool2018.object.GraphRow;
import com.datadelivery.WorldCupPool2018.object.GraphRowValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

/**
 * Created by handy.kestury on 6/12/2018.
 */
@Service
public class DataService {

  MongoDatabase database;

  private final static String FINISHED_STATUS = "FINISHED";

  private final static String TIE_STATUS = "Tie";

  public MongoDatabase initConnection() {
    if(database == null) {
      database = DBConnectionHelper.getConnection();
    }
    return database;
  }

  public GraphResponseBody getBalance() {

    GraphResponseBody result = new GraphResponseBody();

    final List<GraphColumn> players = Lists.newArrayList();
    final Map<String, Double> playerOrder = Maps.newTreeMap();

    initConnection();
    players.add(new GraphColumn("0", "Match Day"));
    final MongoCollection userCollection = database.getCollection("Users");
    final MongoCollection userMatchCollection = database.getCollection("UserMatches");

    final MongoCursor<Document> userCursor = userCollection.find().sort(orderBy(ascending("name"))).iterator();
    int id = 0;
    while (userCursor.hasNext()) {
      id++;
      Document obj = userCursor.next();
      players.add(new GraphColumn(Integer.toString(id), obj.getString("name")));
    }

    BasicDBObject seQIDQuery = new BasicDBObject();
    BasicDBObject notNull = new BasicDBObject("$ne", null);
    seQIDQuery.put("totalBalance", notNull);

    MongoCursor<Document> matchCursor = userMatchCollection.find(seQIDQuery).iterator();

    MongoCursor<Document> userMatchCursor = null;

    List<GraphRow> balance = Lists.newArrayList();
    Set<Integer> matches = Sets.newTreeSet();
    try {
      while (matchCursor.hasNext()) {
        Integer seqID = matchCursor.next().getInteger("seqID");
        matches.add(seqID);
      }

      for (double seqID: matches) {
        BasicDBObject query = new BasicDBObject();
        query.put("seqID", seqID);

        GraphRow match = new GraphRow();
        List<GraphRowValue> value = Lists.newArrayList();
        value.add(new GraphRowValue(new Float(seqID)));

        userMatchCursor = userMatchCollection.find(query).sort(orderBy(ascending("name"))).iterator();
        while (userMatchCursor.hasNext()) {
          Document um = userMatchCursor.next();
          if(um.get("totalBalance") != null) {
            playerOrder.put(um.getString("user"), um.getDouble("totalBalance"));
          }
        }

        for(Double bal : playerOrder.values()) {
          value.add(new GraphRowValue(new Float(bal)));
        }

        match.setC(value);
        balance.add(match);
      }

      result.setCols(players);
      result.setRows(balance);
    }
    finally {
      if (userMatchCursor != null) {
        userMatchCursor.close();
      }
      if (matchCursor != null) {
        matchCursor.close();
      }
    }
    return result;
  }

  public String getFixtures() {
    RestTemplate restTemplate = new RestTemplate();
    String result = restTemplate.getForObject("http://api.football-data.org/v1/competitions/{id}/fixtures", String.class, "467");
    System.out.println("Fixtures: " + result);
    return result;
  }

  public void updateBalance(final List<Object> fixtures) {
    initConnection();

    MongoCursor<Document> matchCursor = null;
    MongoCursor<Document> userCursor = null;
    try {
      MongoCollection userMatchCollection = database.getCollection("UserMatches");
      MongoCollection userCollection = database.getCollection("Users");

      Map<String, Double> totalBalance = Maps.newHashMap();
      userCursor = userCollection.find().iterator();
      while (userCursor.hasNext()) {
        Document user = userCursor.next();
        totalBalance.put(user.getString("name"), user.getDouble("totalBalance"));
      }

      BasicDBObject query = new BasicDBObject();
      for(int i = 0; i < fixtures.size(); i++) {
        Map<String, String> match = (Map<String, String>) fixtures.get(i);
        if(FINISHED_STATUS.equalsIgnoreCase(match.get("status"))) {
          String matchId = match.get("MatchID");
          query.put("match", matchId);
          query.put("totalBalance", null);
          matchCursor = userMatchCollection.find(query).iterator();

          List<Document> individualMatch = Lists.newArrayList();
          double totalBetPerRound = 0;
          int totalWinner = 0;

          String matchWinner = findWinner(match);

          //Find the total winner
          while (matchCursor.hasNext()) {
            Document userMatch = matchCursor.next();
            individualMatch.add(userMatch);
            totalBetPerRound = totalBetPerRound + userMatch.getDouble("betAmount");
            if(matchWinner.equalsIgnoreCase(userMatch.getString("teamPick"))) {
              totalWinner++;
            }
          }

          for(Document userMatch : individualMatch) {
            double betAmount = null == userMatch.get("betAmount") ? 0 : userMatch.getDouble("betAmount");
            double newBalance = totalBalance.get(userMatch.getString("user"));
            if (totalWinner > 0) {
              newBalance = newBalance - betAmount;
            }

            if(matchWinner.equalsIgnoreCase(userMatch.getString("teamPick"))) {
              newBalance = newBalance + totalBetPerRound/totalWinner;
            }

            BasicDBObject newDocument = new BasicDBObject();
            newDocument.append("$set", new BasicDBObject().append("totalBalance", newBalance));

            BasicDBObject searchUserQuery = new BasicDBObject().append("name", userMatch.getString("user"));
            userCollection.updateOne(searchUserQuery, newDocument);

            BasicDBObject searchUserMatchQuery = new BasicDBObject()
                .append("user", userMatch.getString("user"))
                .append("match", userMatch.getString("match"));

            userMatchCollection.updateOne(searchUserMatchQuery, newDocument);

            totalBalance.put(userMatch.getString("user"), newBalance);
          }
        } else {
          break;
        }
      }
    } finally {
      matchCursor.close();
      userCursor.close();
    }

  }

  private String findWinner(Map<String, String> match) {
    String winner = null;
    if(Integer.parseInt(match.get("goalsHomeTeam")) > Integer.parseInt(match.get("goalsAwayTeam"))) {
      winner = match.get("homeTeamName");
    } else if(Integer.parseInt(match.get("goalsHomeTeam")) < Integer.parseInt(match.get("goalsAwayTeam"))) {
      winner = match.get("awayTeamName");
    } else {
      winner = TIE_STATUS;
    }
    return winner;
  }
}
