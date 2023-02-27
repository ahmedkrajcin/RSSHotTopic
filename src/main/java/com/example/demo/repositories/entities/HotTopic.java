package com.example.demo.repositories.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class HotTopic {
    @Id
    @GeneratedValue
    private Integer id;
    @Column
    private String identifier;
    @Column
    private String  name;
    @Column
    private Integer count;

    public HotTopic(String identifier, String name, Integer count) {
        this.identifier = identifier;
        this.name = name;
        this.count = count;
    }
}
