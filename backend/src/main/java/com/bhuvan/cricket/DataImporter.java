package com.bhuvan.cricket;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DataImporter implements CommandLineRunner {

    @Value("${cricket.data-dir}")
    private String dataDir;

    private final MatchRepository matchRepository;

    public DataImporter(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        String sourceId = "335982";   // the file you've been using, without .json

        if (matchRepository.existsBySourceId(sourceId)) {
            System.out.println("Already imported: " + sourceId);
            return;
        }

        File file = new File(dataDir, sourceId + ".json");
        JsonNode info = new ObjectMapper().readTree(file).path("info");

        Match match = new Match();
        match.setSourceId(sourceId);
        match.setSeason(info.path("season").asText());
        match.setVenue(info.path("venue").asText());
        match.setDate(info.path("dates").get(0).asText());
        match.setTeam1(info.path("teams").get(0).asText());
        match.setTeam2(info.path("teams").get(1).asText());
        match.setWinner(info.path("outcome").path("winner").asText(null));

        matchRepository.save(match);
        System.out.println("Imported: " + sourceId);
    }
}
