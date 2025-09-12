package com.gamevault.igdbservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamevault.igdbservice.service.IgdbGameService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/igdb")
public class IgdbGameApiController {
    private final IgdbGameService igdbGameService;

    public IgdbGameApiController(IgdbGameService igdbGameService) {
        this.igdbGameService = igdbGameService;
    }

    @GetMapping("/games")
    public JsonNode gamesIGDB(@RequestBody String search) {
        return igdbGameService.gamesIGDB(search);
    }

    @GetMapping("/games/{gameId}")
    public JsonNode gameIGDB(@PathVariable String gameId) {
        return igdbGameService.gameIGDB(gameId);
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
