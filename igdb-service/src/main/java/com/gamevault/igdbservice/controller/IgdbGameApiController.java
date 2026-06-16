package com.gamevault.igdbservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamevault.dto.igdb.IgdbGameDto;
import com.gamevault.igdbservice.service.IgdbGameService;
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

    @PostMapping("/games")
    public JsonNode gamesIGDB(@RequestBody String search) {
        return igdbGameService.gamesIGDB(search);
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
}
