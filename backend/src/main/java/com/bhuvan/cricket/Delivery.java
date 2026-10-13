package com.bhuvan.cricket;

import jakarta.persistence.*;

@Entity
@Table(name="delivery")


public class Delivery {
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
