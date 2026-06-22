package com.gamevault.igdbservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamevault.dto.IgdbSteamMatchDto;
import com.gamevault.dto.igdb.IgdbGameDto;
import com.gamevault.igdbservice.IgdbTokenManager;
import com.gamevault.igdbservice.dto.IgdbExternalGameDto;
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

    public List<IgdbGameDto> searchGames(String query) {
        String safeQuery = query.replace("\\", "\\\\").replace("\"", "\\\"");

        String body = """
                fields name,cover.url, release_dates.y, platforms, platforms.abbreviation, aggregated_rating,
                    game_type, game_type.id, game_type.type, game_modes.name, first_release_date,
                    dlcs.name, dlcs.cover.url, dlcs.game_type.id, dlcs.game_type.type, dlcs.game_status.status, dlcs.game_modes.name, standalone_expansions,
                    expansions.name, expansions.game_type.*, expansions.game_status.status, expansions.cover.url, expansions.game_modes.name;
                search *"%s"*;
                where game_type = (0,1,4,8,9) & version_parent = null;
                limit 200;
                """.formatted(safeQuery);

        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/games")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToFlux(IgdbGameDto.class)
                .collectList()
                .blockOptional()
                .orElseGet(List::of);
    }

    public IgdbGameDto gameIGDB(String gameId) {
        String body = """
                fields name,cover.url, release_dates.y, game_type.id, game_type.type, parent_game.name,
                    game_modes.name, game_modes.slug, summary, genres.name, first_release_date, platforms.abbreviation,
                    collections.name, collections.slug, collections.games.name, collections.games.slug, collections.games.cover.url, collections.games.game_type.type,
                    involved_companies.company.name, involved_companies.company.slug, involved_companies.developer, involved_companies.publisher,
                    dlcs.name, dlcs.cover.url, dlcs.game_type.id, dlcs.game_type.type, dlcs.game_status.status, dlcs.summary, dlcs.game_modes.name, dlcs.game_modes.slug,
                    standalone_expansions,
                    expansions.name, expansions.game_type.type, expansions.game_status.status, expansions.cover.url, expansions.summary, expansions.game_modes.name, expansions.game_modes.slug;
                where id = %s; sort franchises.games.release_dates.y desc;
                """.formatted(gameId);

        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/games")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue(body))
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

    public JsonNode gameSeries(String series) {
        String body = """
                fields name, games, slug,
                    games.game_type.type, games.parent_game.name,
                    games.name, games.cover.url, games.platforms.abbreviation, games.first_release_date;
                where slug = "%s" ; sort games.first_release_date desc;
                """.formatted(series);

        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/collections")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(JsonNode.class).block();
    }

    public JsonNode gamesReleaseDates() {
        long actualDate = System.currentTimeMillis()/1000;

        String body = """
                fields *, game.name, game.game_type, game.category, game.cover.url, game.platforms.abbreviation,
                    platform.abbreviation, game.hypes;
                where date > %s & release_region = 8;
                sort date asc;
                limit 50;
                """.formatted(actualDate);

        return igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/release_dates")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(JsonNode.class).block();
    }

    public List<IgdbSteamMatchDto> matchBySteamAppIds(Set<Long> steamAppIds) {
        if (steamAppIds == null || steamAppIds.isEmpty()) {
            return List.of();
        }

        List<Long> ids = new ArrayList<>(steamAppIds);
        List<IgdbSteamMatchDto> result = new ArrayList<>();

        for (int from = 0; from < ids.size(); from += IGDB_BATCH_SIZE) {
            int to = Math.min(from + IGDB_BATCH_SIZE, ids.size());
            result.addAll(fetchSteamMatches(ids.subList(from, to)));
        }

        return result;
    }

    private List<IgdbSteamMatchDto> fetchSteamMatches(List<Long> steamAppIds) {
        String appIds = steamAppIds.stream()
                .map(String::valueOf)
                .map(id -> "\"" + id + "\"")
                .collect(Collectors.joining(","));

        String body = """
            fields uid,
                   game.id,
                   game.name,
                   game.cover.url,
                   game.first_release_date,
                   game.release_dates.y,
                   game.platforms.abbreviation,
                   game.game_type.id,
                   game.game_type.type;
            where external_game_source = 1 & uid = (%s);
            limit %d;
            """.formatted(appIds, steamAppIds.size());

        List<IgdbExternalGameDto> externalGames = igdbServiceWebClient.post()
                .uri("https://api.igdb.com/v4/external_games")
                .header("Client-ID", apiClient.getClient_id())
                .header("Authorization", "Bearer " + apiClient.getAccess_token())
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToFlux(IgdbExternalGameDto.class)
                .collectList()
                .blockOptional()
                .orElseGet(List::of);

        return externalGames.stream()
                .map(external -> new IgdbSteamMatchDto(parseSteamAppId(external.uid()), external.game()))
                .filter(match -> match.steamAppId() != null && match.game() != null)
                .toList();
    }

    private Long parseSteamAppId(String uid) {
        try {
            return uid == null ? null : Long.parseLong(uid);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
