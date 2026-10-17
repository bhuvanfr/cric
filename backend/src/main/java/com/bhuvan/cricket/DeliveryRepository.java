package com.bhuvan.cricket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    interface BattingStat {
        String getName();
        Long getInnings();
        Long getRuns();
        Long getBalls();
        Long getDismissals();
        Double getAverage();
        Double getStrikeRate();
    }

    interface ImpactStat {
        String getName();
        Long getInnings();
        Long getRuns();
        Long getBalls();
        Double getStrikeRate();
        Double getRunShare();
        Double getSrRatio();
        Double getEfficiency();
        Double getImpactVolume();
    }

    @Query(value = """
        WITH bat AS (
            SELECT d.batter_id,
                   COUNT(DISTINCT d.innings_id) AS innings,
                   SUM(d.batter_runs) AS runs,
                   COUNT(*) FILTER (WHERE d.extras_type IS NULL
                                    OR d.extras_type NOT LIKE '%wides%') AS balls
            FROM delivery d
            JOIN innings i ON i.id = d.innings_id
            JOIN matches m ON m.id = i.match_id
            WHERE i.innings_number <= 2
              AND m.season_year BETWEEN :fromSeason AND :toSeason
              AND d.over_number BETWEEN :fromOver AND :toOver
            GROUP BY d.batter_id
        ),
        outs AS (
            SELECT d.player_out_id AS player_id, COUNT(*) AS dismissals
            FROM delivery d
            JOIN innings i ON i.id = d.innings_id
            JOIN matches m ON m.id = i.match_id
            WHERE i.innings_number <= 2
              AND m.season_year BETWEEN :fromSeason AND :toSeason
              AND d.over_number BETWEEN :fromOver AND :toOver
              AND d.player_out_id IS NOT NULL
              AND d.wicket_kind NOT IN ('retired hurt', 'retired not out')
            GROUP BY d.player_out_id
        )
        SELECT p.name AS "name", bat.innings AS "innings", bat.runs AS "runs",
               bat.balls AS "balls", COALESCE(outs.dismissals, 0) AS "dismissals",
               CAST(ROUND(CAST(bat.runs AS numeric) / NULLIF(outs.dismissals, 0), 2) AS double precision) AS "average",
               CAST(ROUND(bat.runs * 100.0 / NULLIF(bat.balls, 0), 2) AS double precision) AS "strikeRate"
        FROM bat
        JOIN player p ON p.id = bat.batter_id
        LEFT JOIN outs ON outs.player_id = bat.batter_id
        ORDER BY bat.runs DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<BattingStat> battingLeaderboard(@Param("fromSeason") int fromSeason,
                                         @Param("toSeason") int toSeason,
                                         @Param("fromOver") int fromOver,
                                         @Param("toOver") int toOver,
                                         @Param("limit") int limit);

    @Query(value = """
        WITH filtered AS (
            SELECT d.innings_id, d.batter_id, d.batter_runs, d.extras_type
            FROM delivery d
            JOIN innings i ON i.id = d.innings_id
            JOIN matches m ON m.id = i.match_id
            WHERE i.innings_number <= 2
              AND m.season_year BETWEEN :fromSeason AND :toSeason
              AND d.over_number BETWEEN :fromOver AND :toOver
        ),
        per_innings AS (
            SELECT innings_id, batter_id,
                   SUM(batter_runs) AS runs,
                   COUNT(*) FILTER (WHERE extras_type IS NULL
                                    OR extras_type NOT LIKE '%wides%') AS balls
            FROM filtered
            GROUP BY innings_id, batter_id
        ),
        team_innings AS (
            SELECT innings_id, SUM(runs) AS team_runs, SUM(balls) AS team_balls
            FROM per_innings
            GROUP BY innings_id
        ),
        per_batter AS (
            SELECT pi.batter_id,
                   COUNT(*) AS innings,
                   CAST(SUM(pi.runs) AS bigint) AS runs,
                   CAST(SUM(pi.balls) AS bigint) AS balls,
                   CAST(SUM(ti.team_runs) AS bigint) AS team_runs,
                   CAST(SUM(ti.team_balls) AS bigint) AS team_balls
            FROM per_innings pi
            JOIN team_innings ti ON ti.innings_id = pi.innings_id
            GROUP BY pi.batter_id
            HAVING SUM(pi.balls) >= :minBalls
        ),
        leaders AS (
            SELECT * FROM per_batter ORDER BY runs DESC LIMIT :limit
        ),
        calc AS (
            SELECT l.*,
                   l.runs * 1.0 / NULLIF(l.team_runs, 0) AS run_share,
                   (l.runs * 1.0 / NULLIF(l.balls, 0))
                     / NULLIF((l.team_runs - l.runs) * 1.0
                              / NULLIF(l.team_balls - l.balls, 0), 0) AS sr_ratio
            FROM leaders l
        )
        SELECT p.name AS "name",
               c.innings AS "innings",
               c.runs AS "runs",
               c.balls AS "balls",
               CAST(ROUND(c.runs * 100.0 / NULLIF(c.balls, 0), 2) AS double precision) AS "strikeRate",
               CAST(ROUND(c.run_share, 4) AS double precision) AS "runShare",
               CAST(ROUND(c.sr_ratio, 3) AS double precision) AS "srRatio",
               CAST(ROUND(c.run_share * c.sr_ratio, 4) AS double precision) AS "efficiency",
               CAST(ROUND(c.run_share * c.sr_ratio * c.runs, 2) AS double precision) AS "impactVolume"
        FROM calc c
        JOIN player p ON p.id = c.batter_id
        ORDER BY "impactVolume" DESC NULLS LAST
        """, nativeQuery = true)
    List<ImpactStat> impactLeaderboard(@Param("fromSeason") int fromSeason,
                                       @Param("toSeason") int toSeason,
                                       @Param("fromOver") int fromOver,
                                       @Param("toOver") int toOver,
                                       @Param("minBalls") int minBalls,
                                       @Param("limit") int limit);
}