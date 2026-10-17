package com.bhuvan.cricket;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StatsController {

    public record BattingRow(String name, Long innings, Long runs, Long balls,
                             Long dismissals, Double average, Double strikeRate) {}

    public record ImpactRow(String name, Long innings, Long runs, Long balls,
                            Double strikeRate, Double runShare, Double srRatio,
                            Double efficiency, Double impactVolume) {}

    private final DeliveryRepository deliveryRepository;

    public StatsController(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @GetMapping("/batting/leaderboard")
    public List<BattingRow> leaderboard(
            @RequestParam(defaultValue = "2008") int fromSeason,
            @RequestParam(defaultValue = "2026") int toSeason,
            @RequestParam(defaultValue = "1") int fromOver,
            @RequestParam(defaultValue = "20") int toOver,
            @RequestParam(defaultValue = "10") int limit) {
        // the data numbers overs from 0, people say 1-20
        return deliveryRepository
                .battingLeaderboard(fromSeason, toSeason, fromOver - 1, toOver - 1, limit)
                .stream()
                .map(s -> new BattingRow(s.getName(), s.getInnings(), s.getRuns(), s.getBalls(),
                        s.getDismissals(), s.getAverage(), s.getStrikeRate()))
                .toList();
    }

    @GetMapping("/batting/impact")
    public List<ImpactRow> impact(
            @RequestParam(defaultValue = "2008") int fromSeason,
            @RequestParam(defaultValue = "2026") int toSeason,
            @RequestParam(defaultValue = "1") int fromOver,
            @RequestParam(defaultValue = "20") int toOver,
            @RequestParam(defaultValue = "0") int minBalls,
            @RequestParam(defaultValue = "10") int limit) {
        return deliveryRepository
                .impactLeaderboard(fromSeason, toSeason, fromOver - 1, toOver - 1, minBalls, limit)
                .stream()
                .map(s -> new ImpactRow(s.getName(), s.getInnings(), s.getRuns(), s.getBalls(),
                        s.getStrikeRate(), s.getRunShare(), s.getSrRatio(),
                        s.getEfficiency(), s.getImpactVolume()))
                .toList();
    }
}   