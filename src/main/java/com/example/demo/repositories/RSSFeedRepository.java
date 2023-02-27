package com.example.demo.repositories;

import com.example.demo.repositories.entities.HotTopic;
import com.example.demo.repositories.entities.RSSFeedEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface RSSFeedRepository extends JpaRepository<RSSFeedEntity, Integer> {
    List<RSSFeedEntity> findByIdentifier(String identifier);

}
