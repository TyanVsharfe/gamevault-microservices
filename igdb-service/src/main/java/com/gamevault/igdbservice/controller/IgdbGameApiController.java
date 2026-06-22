package com.gamevault.igdbservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamevault.dto.IgdbSteamMatchDto;
import com.gamevault.dto.igdb.IgdbGameDto;
import com.gamevault.igdbservice.service.IgdbGameService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/igdb")
public class IgdbGameApiController {
    private final IgdbGameService igdbGameService;

    public IgdbGameApiController(IgdbGameService igdbGameService) {
        this.igdbGameService = igdbGameService;
    }

    @GetMapping("/games")
    public List<IgdbGameDto> searchGames(@RequestParam @NotBlank @Size(max = 200) String query) {
        return igdbGameService.searchGames(query);
    }

    @GetMapping("/games/{gameId}")
    public IgdbGameDto gameIGDB(@PathVariable String gameId) {
        return igdbGameService.gameIGDB(gameId);
    }

    @PostMapping("/games/batch")
    public List<JsonNode> gamesByIds(@RequestBody Set<Long> gameIds) {
        return igdbGameService.gamesByIds(gameIds);
    }

    @GetMapping("/series/{seriesTitle}")
    public JsonNode gameSeries(@PathVariable String seriesTitle) {
        return igdbGameService.gameSeries(seriesTitle);
    }

    @GetMapping("/games/release-dates")
    public JsonNode gamesReleaseDates() {
        return igdbGameService.gamesReleaseDates();
    }

    @PostMapping("/steam/matches/by-appids")
    public List<IgdbSteamMatchDto> steamMatchesByAppIds(@RequestBody Set<Long> steamAppIds) {
        return igdbGameService.matchBySteamAppIds(steamAppIds);
    }
}
