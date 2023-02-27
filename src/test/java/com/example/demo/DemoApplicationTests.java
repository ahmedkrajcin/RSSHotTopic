package com.example.demo;

import com.example.demo.repositories.HotTopicRepository;
import com.example.demo.repositories.RSSFeedRepository;
import com.example.demo.repositories.entities.HotTopic;
import com.example.demo.repositories.entities.RSSFeedEntity;
import com.example.demo.services.RSSFeedService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
class DemoApplicationTests {
	@Autowired
	RSSFeedService rssFeedService;

	@Autowired
	RSSFeedRepository rssFeedRepository;

	@Autowired
	HotTopicRepository hotTopicRepository;

	@Test
	void testSaveThreeDifferentWords() throws ExecutionException, InterruptedException {
		// Set up test data
		List<String> urls = new ArrayList<>();
		urls.add("https://news.yahoo.com/rss");
		urls.add("https://news.google.com/news?cf=all&hl=en&pz=1&ned=us&output=rss");

		String id =rssFeedService.process(urls);
		// Verify that the result is not null
		assertNotNull(id);

		List<RSSFeedEntity> result =rssFeedRepository.findByIdentifier(id);
		// Verify that the feed result has 3 entries
		assertEquals(3, result.size());

		List<HotTopic> result2 = hotTopicRepository.findByIdentifier(id);
		assertEquals(3,result2.size());
		assertTrue(result2.get(0).getName() != result2.get(1).getName());
		assertTrue(result2.get(1).getName() != result2.get(2).getName());
		assertTrue(result2.get(0).getName() != result2.get(2).getName());
	}

	@Test
	void testComonWordsFromFeeds(){
		Map<String,Integer> feed1 = new HashMap<>();
		feed1.put("putin",2);
		feed1.put("biden",3);
		feed1.put("ukraine",1);
		feed1.put("rocket",2);

		Map<String,Integer> feed2 = new HashMap<>();
		feed2.put("putin",2);
		feed2.put("biden",3);
		feed2.put("ukraine",1);
		feed2.put("democracy",2);

		Map<String, Integer>[] wordOccurrencesFromFeeds = new Map[2];
		wordOccurrencesFromFeeds[0]=feed1;
		wordOccurrencesFromFeeds[1]=feed2;



		Set<String> result =rssFeedService.getCommonWords(wordOccurrencesFromFeeds);
		assertEquals(3 ,result.size());
		Set<String> words = new HashSet<>();
		words.add("putin");
		words.add("biden");
		words.add("ukraine");
		assertEquals(result, words);



	}

}
