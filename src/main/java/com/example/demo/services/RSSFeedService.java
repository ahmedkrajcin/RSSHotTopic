package com.example.demo.services;

import com.example.demo.models.FrequencyResponse;
import com.example.demo.models.HotFeedResponse;
import com.example.demo.repositories.HotTopicRepository;
import com.example.demo.repositories.RSSFeedRepository;
import com.example.demo.repositories.entities.HotTopic;
import com.example.demo.repositories.entities.RSSFeedEntity;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Service
@Component
public class RSSFeedService {
    Logger logger = LoggerFactory.getLogger(RSSFeedService.class);

    @Value("${laststopwords}")
    private  String stopwords;

    @Autowired
    RSSFeedRepository rssFeedRepository;
    @Autowired
    HotTopicRepository hotTopicRepository;


    private Map<String, String> feedsData;



    public String process(List<String> feedUrls) throws ExecutionException, InterruptedException {
        feedsData=new HashMap<>();
        // Create a thread pool
        ExecutorService executor = Executors.newFixedThreadPool(feedUrls.size());

        // Submit tasks to fetch the set of words with their occurrences from each feed
        List<Future<Map<String, Integer>>> futures = new ArrayList<>();
        for (String feedUrl : feedUrls) {
            Callable<Map<String, Integer>> task = () -> getWordOccurrencesFromFeed(feedUrl);
            Future<Map<String, Integer>> future = executor.submit(task);
            futures.add(future);
        }

        // Wait for all tasks to complete and get the set of words with their occurrences from each feed
        Map<String, Integer>[] wordOccurrencesFromFeeds = new Map[feedUrls.size()];
        for (int i = 0; i < futures.size(); i++) {
            wordOccurrencesFromFeeds[i] = futures.get(i).get();
        }
        executor.shutdown();
        logger.info("Getting data finished for URLs: " + feedUrls);
        // Get the set of words that appear in all feeds
        Set<String> commonWords = getCommonWords(wordOccurrencesFromFeeds);

        // Compute the total occurrences of each common word
        Map<String, Integer> wordOccurrences = new HashMap<>();
        for (String word : commonWords) {
            int count = getWordCount(word, wordOccurrencesFromFeeds);
            wordOccurrences.put(word, count);
        }
        // Sort the common words by their total occurrences in descending order and take first 3
        Map<String, Integer> mostMentionedWords = wordOccurrences.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3).collect(Collectors.toMap(Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1,v2)->v1,
                        LinkedHashMap::new));

        //Get feeds that consist most mentioned words
        Map<String, Integer> finalBestFeeds = new HashMap<>();
        feedsData.entrySet().stream()
                .forEach(feed -> {
                            for (Map.Entry<String, Integer> word : mostMentionedWords.entrySet()) {
                                if (feed.getKey().toLowerCase().contains(word.getKey())) {
                                    finalBestFeeds.merge(feed.getKey(), 1, (a, b) -> a + b);
                                }
                            }
                        });

        List<String>  bestFeeds= finalBestFeeds.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(3).map(Map.Entry::getKey).collect(Collectors.toList());

        //Loop 3 best feeds to save into database
        String id = UUID.randomUUID().toString();
        for (String feed : bestFeeds) {
           RSSFeedEntity feedEntity = new RSSFeedEntity();
           feedEntity.setIdentifier(id);
           feedEntity.setLink(feedsData.get(feed));
           feedEntity.setTitle(feed);
            rssFeedRepository.save(feedEntity);
       }
       for (Map.Entry<String, Integer> word: mostMentionedWords.entrySet()){
           HotTopic topic = new HotTopic(id, word.getKey(), word.getValue());
           hotTopicRepository.save(topic);
       }
        return id;
    }

    public  Set<String> getCommonWords(Map<String, Integer>[] wordOccurrencesFromFeeds) {
        // Get the set of words that appear in all feeds
        return stream(wordOccurrencesFromFeeds)
                .map(Map::keySet)
                .reduce((s1, s2) -> { s1.retainAll(s2); return s1; })
                .orElse(Collections.emptySet());
    }
    private static int getWordCount(String word, Map<String, Integer>[] wordOccurrencesFromFeeds) {
        // Compute the total occurrences of the given word in all feeds
        return stream(wordOccurrencesFromFeeds)
                .mapToInt(wordOccurrences -> wordOccurrences.getOrDefault(word, 0))
                .sum();
    }
    private Map<String, Integer> getWordOccurrencesFromFeed(String feedUrl) throws FeedException, IOException {
        // Fetch the RSS feed and parse it using ROME library
        SyndFeed feed = getSyndFeed(feedUrl);
        //Collect all feeds title and link
        feedsData.putAll(feed.getEntries().stream()
                .collect(Collectors.toMap(SyndEntry::getTitle, SyndEntry::getLink)));

        // Compute the set of words and their occurrences in the feed
        return feed.getEntries().stream()
                .map(feedItem -> Arrays.asList(feedItem.getTitle().split("\\s+")))
                .flatMap(Collection::stream)
                .map(String::toLowerCase).map(StringUtils::stripAccents)
                .filter(word -> StringUtils.isAlpha(word) && !this.stopwords.contains(word))
                .collect(Collectors.toConcurrentMap(word -> word, word -> 1, Integer::sum));
    }

    private SyndFeed getSyndFeed(String feedUrl) {
     try {
         SyndFeedInput input = new SyndFeedInput();
         URL url = new URL(feedUrl);
         SyndFeed feed = input.build(new XmlReader(url));
         return feed;
     } catch (IllegalArgumentException | FeedException | IOException e) {
         logger.error("Error in RSS feed loading: URL:" + feedUrl + e.getLocalizedMessage());
         throw new RuntimeException(("Error in RSS feed loading: URL:" + feedUrl));
     }
    }

    public FrequencyResponse getFrequency(String identifier) {
        logger.info("Loading topics for identifier :" +identifier);
        List<RSSFeedEntity> hotFeeds= rssFeedRepository.findByIdentifier(identifier);
        List<HotTopic> hotTopics = hotTopicRepository.findByIdentifier(identifier);
        FrequencyResponse frequencyResponse = new FrequencyResponse();
        frequencyResponse.setHotTopics(hotTopics.stream().map(HotTopic::getName)
                .collect(Collectors.toList()));
        frequencyResponse.setHotFeeds(hotFeeds.stream().map(p -> new HotFeedResponse(p.getTitle(), p.getLink()))
                .collect(Collectors.toList()));
        return frequencyResponse;
    }
}
