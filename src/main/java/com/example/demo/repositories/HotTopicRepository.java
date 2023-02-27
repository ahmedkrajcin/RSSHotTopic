package com.example.demo.repositories;

import com.example.demo.repositories.entities.HotTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface HotTopicRepository extends JpaRepository<HotTopic, Long> {
    List<HotTopic> findByIdentifier(String identifier);
}
