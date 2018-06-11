package com.datadelivery.WorldCupPool2018;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Created by handy.kestury on 6/11/2018.
 */
@Controller
public class HomeController {

  @GetMapping("/")
  public String progressGraph() {
    return "home";
  }
}
