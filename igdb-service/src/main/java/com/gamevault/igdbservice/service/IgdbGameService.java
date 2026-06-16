package com.gamevault.igdbservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamevault.dto.igdb.IgdbGameDto;
import com.gamevault.igdbservice.IgdbTokenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IgdbGameService {
    private static final int IGDB_BATCH_SIZE = 500;

    private final WebClient igdbServiceWebClient;
    private final IgdbTokenManager apiClient;

    public IgdbGameService(WebClient igdbServiceWebClient, IgdbTokenManager apiClient) {
        this.igdbServiceWebClient = igdbServiceWebClient;
        this.apiClient = apiClient;
    }

    public JsonNode gamesIGDB(String search) {
        System.out.println("fields name,cover.url, release_dates.y, " +
                "platforms, platforms.abbreviation, aggregated_rating,"
                + "game_type, first_release_date, category;"
                + "search *\"" + search + "*\";"
                + "where category = (0,8,9) & "
                //+ "platforms = (0,8) & "
                + "version_parent = null;"
                + "limit 200;");
        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/games")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue
                        ("fields name,cover.url, release_dates.y, " +
                                "platforms, platforms.abbreviation, aggregated_rating,"
                                + "game_type, first_release_date, game_type;"
                                + "search *\"" + search + "*\";"
                                + "where game_type = (0,1,4,8,9);"
                                //+ "platforms = (0,8) & "
                                //+ "version_parent = null;"
                                + "limit 200;"))
                .retrieve()
                .bodyToMono(JsonNode.class).block();
    }

    public IgdbGameDto gameIGDB(String gameId) {
        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/games")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue
                        ("fields name,cover.url, release_dates.y, "
                                + "game_type.id, game_type.type, parent_game.name, game_modes.name, game_modes.slug, summary, genres.name, first_release_date, platforms.abbreviation,"
                                + "collections.name, collections.slug, collections.games.name, collections.games.slug, collections.games.cover.url, collections.games.game_type.type,"
              /*                  + "franchises.name, franchises.slug, franchises.games.name, franchises.games.cover.url,"
                                + "franchises.games.platforms.abbreviation, franchises.games.release_dates.y,"*/
                                + "involved_companies.company.name, involved_companies.company.slug, involved_companies.developer, involved_companies.publisher,"
                                + "dlcs.name, dlcs.cover.url, dlcs.game_type.id, dlcs.game_type.type, dlcs.game_status.status, dlcs.summary, dlcs.game_modes.name, dlcs.game_modes.slug, standalone_expansions,"
                                + "expansions.name, expansions.game_type.type, expansions.game_status.status, expansions.cover.url, expansions.summary, expansions.game_modes.name, expansions.game_modes.slug;"
                                + "where id = " + gameId + "; sort franchises.games.release_dates.y desc;"))
                .retrieve()
                .bodyToFlux(IgdbGameDto.class)
                .next().blockOptional()
                .orElse(null);
    }

    public List<JsonNode> gamesByIds(Set<Long> gameIds) {
        if (gameIds.isEmpty()) {
            return List.of();
        }

        List<Long> ids = new ArrayList<>(gameIds);
        List<JsonNode> games = new ArrayList<>();
        for (int from = 0; from < ids.size(); from += IGDB_BATCH_SIZE) {
            int to = Math.min(from + IGDB_BATCH_SIZE, ids.size());
            games.addAll(fetchGameSummaries(ids.subList(from, to)));
        }
        return games;
    }

    private List<JsonNode> fetchGameSummaries(List<Long> gameIds) {
        String ids = gameIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/games")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue(
                        "fields id,name,cover.url,first_release_date;"
                                + "where id = (" + ids + ");"
                                + "limit " + gameIds.size() + ";"
                ))
                .retrieve()
                .bodyToFlux(JsonNode.class)
                .collectList()
                .blockOptional()
                .orElseGet(List::of);
    }

    public JsonNode gameSeries(String seriesTitle) {
        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/collections")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue
                        ("fields name, games, slug,"
                                + "games.name, games.cover.url, games.platforms.abbreviation, games.first_release_date;"
                                + " where slug = \"" + seriesTitle + "\"; sort games.first_release_date desc;"))
                .retrieve()
                .bodyToMono(JsonNode.class).block();
    }

    public JsonNode gamesReleaseDates() {
        long actualDate = System.currentTimeMillis()/1000;

        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/release_dates")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue
                        ("fields *, game.name, game.category, game.cover.url, game.platforms.abbreviation, game.hypes; "
                                + " where date > " + actualDate + " & region = 8;"
                                + "sort date asc;"
                                + "limit 50;"))
                .retrieve()
                .bodyToMono(JsonNode.class).block();
    }

    public JsonNode steamImportGamesIGDB(List<String> steamGamesTitles) {
        StringBuilder titlesString = new StringBuilder("(");
        steamGamesTitles.stream().limit(200).forEach(title -> titlesString.append("\"").append(title).append("\"").append(","));

        titlesString.replace(titlesString.length() - 1, titlesString.length(), ")");

        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/games")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue
                        ("fields name,cover.url, release_dates.y, platforms, platforms.abbreviation," +
                                " aggregated_rating, first_release_date, category;"
                                + "where (name = " + titlesString + " | alternative_names.name = " + titlesString + ")"
                                + " & platforms.abbreviation = \"" + "PC" + "\";"
                                + "limit 300;"))
                .retrieve()
                .bodyToMono(JsonNode.class).block();
    }
}
