package com.bhuvan.cricket;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataImporter implements CommandLineRunner {

    @Value("${cricket.data-dir}")
    private String dataDir;

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final InningsRepository inningsRepository;
    private final DeliveryRepository deliveryRepository;

    public DataImporter(MatchRepository matchRepository,
                        PlayerRepository playerRepository,
                        InningsRepository inningsRepository,
                        DeliveryRepository deliveryRepository) {
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.inningsRepository = inningsRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    public void run(String... args) {
        File[] files = new File(dataDir).listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            System.out.println("No files found in " + dataDir);
            return;
        }
        Arrays.sort(files);
        int done = 0;
        for (File f : files) {
            String sourceId = f.getName().replace(".json", "");
            try {
                if (importMatch(sourceId)) {
                    done++;
                    if (done % 50 == 0) System.out.println("Imported " + done + " matches so far");
                }
            } catch (Exception e) {
                System.out.println("FAILED " + sourceId + ": " + e.getMessage());
            }
        }
        System.out.println("Finished. Newly imported: " + done);
    }

    private boolean importMatch(String sourceId) throws Exception {
        if (matchRepository.existsBySourceId(sourceId)) {
            return false;
        }

        JsonNode root = new ObjectMapper().readTree(new File(dataDir, sourceId + ".json"));
        JsonNode info = root.path("info");
        JsonNode registry = info.path("registry").path("people");

        Match match = new Match();
        match.setSourceId(sourceId);
        match.setSeason(info.path("season").asText());
        match.setVenue(info.path("venue").asText());
        match.setDate(info.path("dates").get(0).asText());
        match.setTeam1(info.path("teams").get(0).asText());
        match.setTeam2(info.path("teams").get(1).asText());
        match.setWinner(info.path("outcome").path("winner").asText(null));
        matchRepository.save(match);

        Map<String, Player> cache = new HashMap<>();
        for (int t = 0; t < 2; t++) {
            String team = info.path("teams").get(t).asText();
            for (JsonNode nameNode : info.path("players").path(team)) {
                getPlayer(nameNode.asText(), registry, cache);
            }
        }

        int number = 1;
        for (JsonNode inn : root.path("innings")) {
            String batting = inn.path("team").asText();
            Innings innings = new Innings();
            innings.setMatch(match);
            innings.setInningsNumber(number++);
            innings.setBattingTeam(batting);
            innings.setBowlingTeam(batting.equals(match.getTeam1()) ? match.getTeam2() : match.getTeam1());
            inningsRepository.save(innings);

            List<Delivery> batch = new ArrayList<>();
            for (JsonNode over : inn.path("overs")) {
                int overNumber = over.path("over").asInt();
                int ball = 0;
                for (JsonNode d : over.path("deliveries")) {
                    ball++;
                    Delivery delivery = new Delivery();
                    delivery.setInnings(innings);
                    delivery.setOverNumber(overNumber);
                    delivery.setBallNumber(ball);
                    delivery.setBatter(getPlayer(d.path("batter").asText(), registry, cache));
                    delivery.setBowler(getPlayer(d.path("bowler").asText(), registry, cache));
                    delivery.setBatterRuns(d.path("runs").path("batter").asInt());
                    delivery.setExtras(d.path("runs").path("extras").asInt());

                    JsonNode extras = d.path("extras");
                    if (!extras.isMissingNode()) {
                        List<String> types = new ArrayList<>();
                        extras.propertyNames().forEach(types::add);
                        delivery.setExtrasType(String.join(",", types));
                    }

                    JsonNode wickets = d.path("wickets");
                    if (wickets.size() > 0) {
                        JsonNode w = wickets.get(0);
                        delivery.setWicketKind(w.path("kind").asText());
                        delivery.setPlayerOut(getPlayer(w.path("player_out").asText(), registry, cache));
                    }
                    batch.add(delivery);
                }
            }
            deliveryRepository.saveAll(batch);
        }
        return true;
    }

    private Player getPlayer(String name, JsonNode registry, Map<String, Player> cache) {
        Player cached = cache.get(name);
        if (cached != null) return cached;
        String id = registry.path(name).asText();
        Player player = playerRepository.findById(id).orElseGet(() -> {
            Player p = new Player();
            p.setId(id);
            p.setName(name);
            return playerRepository.save(p);
        });
        cache.put(name, player);
        return player;
    }
}