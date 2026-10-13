package com.bhuvan.cricket;

import jakarta.persistence.*;

@Entity
@Table(name="innings")

public class Innings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int inningsNumber;
    private String battingTeam;
    private String bowlingTeam;

    @ManyToOne
    private Match match;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
