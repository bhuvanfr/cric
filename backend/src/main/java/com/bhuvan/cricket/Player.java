package com.bhuvan.cricket;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Player {

    @Id
    private String id;      // Cricsheet's identifier, a string, not a number
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

    // getters and setters: right-click in the file, Generate, Getters and Setters
