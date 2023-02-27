package com.example.demo.repositories.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class RSSFeedEntity {
    @Id
    @GeneratedValue
    private Integer id;
    @Column
    private String identifier;
    @Column(nullable = false, length = 1000)
    private String link;
    @Column(nullable = false, length = 1000)
    private String title;

}
