package com.bhuvan.cricket;

import jakarta.persistence.*;

@Entity
@Table(name="delivery")


public class Delivery {
    public Innings getInnings() {
        return innings;
    }

    public void setInnings(Innings innings) {
        this.innings = innings;
    }

    public int getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(int overNumber) {
        this.overNumber = overNumber;
    }

    public int getBallNumber() {
        return ballNumber;
    }

    public void setBallNumber(int ballNumber) {
        this.ballNumber = ballNumber;
    }

    public int getBatterRuns() {
        return batterRuns;
    }

    public void setBatterRuns(int batterRuns) {
        this.batterRuns = batterRuns;
    }

    public int getExtras() {
        return extras;
    }

    public void setExtras(int extras) {
        this.extras = extras;
    }

    public Player getBatter() {
        return batter;
    }

    public void setBatter(Player batter) {
        this.batter = batter;
    }

    public Player getBowler() {
        return bowler;
    }

    public void setBowler(Player bowler) {
        this.bowler = bowler;
    }

    public Player getPlayerOut() {
        return playerOut;
    }

    public void setPlayerOut(Player playerOut) {
        this.playerOut = playerOut;
    }

    public String getExtrasType() {
        return extrasType;
    }

    public void setExtrasType(String extrasType) {
        this.extrasType = extrasType;
    }

    public String getWicketKind() {
        return wicketKind;
    }

    public void setWicketKind(String wicketKind) {
        this.wicketKind = wicketKind;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Innings innings;
    private int overNumber;
    private int ballNumber;
    private int batterRuns;
    private int extras;
    @ManyToOne
    private Player batter;
    @ManyToOne
    private Player bowler;
    @ManyToOne
    private Player playerOut;
    private String extrasType;
    private String wicketKind;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
