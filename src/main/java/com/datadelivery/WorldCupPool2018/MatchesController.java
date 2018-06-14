package com.datadelivery.WorldCupPool2018;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MatchesController {

    @GetMapping("/showMatches")
    public String showMatches(Model result) {
        result.addAttribute("matches", getFixtures());
        return "matches";
    }

    public List<Object> getFixtures() {
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject("http://api.football-data.org/v1/competitions/{id}/fixtures", String.class, "467");

        JsonFactory factory = new JsonFactory();

        List<Object> fixtures = new ArrayList<Object>();
        Map<String, String> map = new HashMap<>();

        try {
            JsonParser parser = factory.createParser(result);

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
                            map.put("Competition", parser.getValueAsString());
                        }
                    }
                    else if (fieldName.equals("date") || fieldName.equals("status") ||
                            fieldName.equals("homeTeamName") || fieldName.equals("awayTeamName") ||
                                fieldName.equals("goalsHomeTeam") || fieldName.equals("goalsAwayTeam")) {
                        map.put(fieldName, parser.getValueAsString());
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
