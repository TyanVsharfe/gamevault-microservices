package com.gamevault.igdbservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamevault.igdbservice.IgdbTokenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
public class IgdbGameService {

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

    public JsonNode gameIGDB(String gameId) {
        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/games")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue
                        ("fields name,cover.url, release_dates.y, "
                                + "game_type, storyline, summary, genres.name, first_release_date, platforms.abbreviation, "
                                + "collections.name, collections.games.name, "
                                + "collections.games.slug, collections.games.cover.url, "
                                //+ "franchises.name, franchises.slug, franchises.games.name, franchises.games.cover.url, "
                                //+ "franchises.games.platforms.abbreviation, franchises.games.release_dates.y, "
                                + "involved_companies.company.name, involved_companies.developer, involved_companies.supporting, involved_companies.publisher; "
                                + " where id = " + gameId + "; sort franchises.games.release_dates.y desc;"))
                .retrieve()
                .bodyToMono(JsonNode.class).block();
    }

    public JsonNode gameSeries(String seriesTitle) {
        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/franchises")
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
