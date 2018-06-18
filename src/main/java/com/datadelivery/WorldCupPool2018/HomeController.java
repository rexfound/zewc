package com.datadelivery.WorldCupPool2018;

import com.datadelivery.WorldCupPool2018.service.DataService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

/**
 * Created by handy.kestury on 6/11/2018.
 */
@Controller
public class HomeController {


  DataService dataService;

  @Autowired
  public void set(DataService dataService) {
      this.dataService = dataService;
  }

  @GetMapping("/")
  public String viewSelection(Model result) {

    MongoDatabase database = dataService.initConnection();

    MongoCollection userCollection = database.getCollection("Users");

    MongoCursor<Document> userCursor = userCollection.find().sort(orderBy(ascending("name"))).iterator();

    Map<Integer, Map<String, String>> userSelections = Maps.newHashMap();

    List<String> userNameList = Lists.newArrayList();

    MongoCollection<Document> userMatchCollection = database.getCollection("UserMatches");

    MongoCursor<Integer> matchCursor = userMatchCollection.distinct("seqID", Integer.class).iterator();
    MongoCursor<Document> userMatchCursor = null;

    try {
        while (userCursor.hasNext()) {
            userNameList.add(userCursor.next().getString("name"));
        }
      while (matchCursor.hasNext()) {
          int matchId = matchCursor.next();
        BasicDBObject query = new BasicDBObject();
        query.put("seqID", matchId);

        userMatchCursor = userMatchCollection.find(query).iterator();
          Map<String, String> usrPickMap = Maps.newHashMap();
        while (userMatchCursor.hasNext()) {
          Document usrPick = userMatchCursor.next();
          usrPickMap.put((String) usrPick.get("user"), (String) usrPick.get("teamPick"));
        }
        userSelections.put(matchId, usrPickMap);
      }
        result.addAttribute("users", userNameList);
      result.addAttribute("userSelections", userSelections);
    } finally {
      userMatchCursor.close();
      matchCursor.close();
    }
    return "home";
  }

}
