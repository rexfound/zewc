package com.datadelivery.WorldCupPool2018;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class MatchesController {

    @GetMapping("/showMatches")
    public String showMatches() {
        return "matches";
    }
}
