package com.example.demo.controllers;

import com.example.demo.models.FrequencyResponse;
import com.example.demo.services.RSSFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RSSController {

    @Autowired
    private RSSFeedService rssFeedService;

    @PostMapping(value = "/analyse/new")
    @Transactional
    public ResponseEntity<String> analyse(@RequestBody List<String> feedUrls) throws Exception {
        if(feedUrls != null && feedUrls.size() >=2){
            return new ResponseEntity<>(rssFeedService.process(feedUrls), HttpStatus.OK);
        }else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping(value = "/frequency/{id}")
    public ResponseEntity<FrequencyResponse> frequency(@PathVariable String id) throws Exception {
        return new ResponseEntity<>(rssFeedService.getFrequency(id), HttpStatus.OK);


    }

}
