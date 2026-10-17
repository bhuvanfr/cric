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
    private String sourceId;
    private Integer seasonYear;

    public Integer getSeasonYear() {
        return seasonYear;
    }

    public void setSeasonYear(Integer seasonYear) {
        this.seasonYear = seasonYear;
    }

    public String getSourceId() {
        return sourceId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getTeam1() {
        return team1;
    }

    public void setTeam1(String team1) {
        this.team1 = team1;
    }

    public String getTeam2() {
        return team2;
    }

    public void setTeam2(String team2) {
        this.team2 = team2;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
