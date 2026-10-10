package com.bhuvan.cricket;

import jakarta.persistence.*;

@Entity
@Table(name="matches")

public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String season;
    private String venue;
    private String date;
    private String team1;
    private String team2;
    private String winner;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
