package com.datadelivery.WorldCupPool2018;

import com.datadelivery.WorldCupPool2018.object.GraphResponseBody;
import com.datadelivery.WorldCupPool2018.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by handy.kestury on 6/12/2018.
 */
@Controller
public class DataController {

  DataService dataService;

  @Autowired
  public void set(DataService dataService) {
    this.dataService = dataService;
  }

  @GetMapping("/api/getBalanceHistory")
  public ResponseEntity getBalance(){
    GraphResponseBody result = dataService.getBalance();

    return ResponseEntity.ok(result);
  }
}
