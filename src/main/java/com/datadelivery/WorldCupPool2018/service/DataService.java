package com.datadelivery.WorldCupPool2018.service;

import com.datadelivery.WorldCupPool2018.object.GraphColumn;
import com.datadelivery.WorldCupPool2018.object.GraphResponseBody;
import com.datadelivery.WorldCupPool2018.object.GraphRow;
import com.datadelivery.WorldCupPool2018.object.GraphRowValue;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by handy.kestury on 6/12/2018.
 */
@Service
public class DataService {

  public GraphResponseBody getBalance() {

    GraphResponseBody result = new GraphResponseBody();

    List<GraphColumn> players = Lists.newArrayList();
    players.add(new GraphColumn("0", "Match Day"));
    for (int i = 1; i <= 6; i++) {
      players.add(new GraphColumn(Integer.toString(i), "Player " + i));
    }

    List<GraphRow> balance = Lists.newArrayList();
    for (int j = 0; j <= 5; j++) {
      GraphRow match = new GraphRow();
      List<GraphRowValue> value = Lists.newArrayList();
      value.add(new GraphRowValue(new Float(j)));
      for(int i = 1; i <= 6; i++) {
        value.add(new GraphRowValue(new Float(Math.random())));
      }
      match.setC(value);
      balance.add(match);
    }

    result.setCols(players);
    result.setRows(balance);
    return result;
  }




}
