package com.example.demo.models;

import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class FrequencyResponse {

    private List<String> hotTopics;
    private List<HotFeedResponse> hotFeeds;

}
