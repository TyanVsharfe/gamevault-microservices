package com.gamevault.usergameservice.service;

import com.gamevault.usergameservice.db.model.Game;
import com.gamevault.usergameservice.db.repository.GameRepository;
import com.gamevault.usergameservice.dto.igdb.IgdbGameDTO;
import com.gamevault.usergameservice.exception.GameNotFoundInIgdbException;
import com.gamevault.usergameservice.exception.IgdbFetchException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    @Value("${igdb.service.url}")
    private String igdbServiceUrl;

    private final GameRepository gameRepository;
    private final WebClient userGameServiceWebClient;

    public GameService(GameRepository gameRepository, WebClient userGameServiceWebClient) {
        this.gameRepository = gameRepository;
        this.userGameServiceWebClient = userGameServiceWebClient;
    }

    public Optional<Game> get(Long id) {
        return gameRepository.findById(id);
    }

    @Transactional
    public Game add(Long igdbId) {
        try {
            List<IgdbGameDTO> igdbGameJson = userGameServiceWebClient.get()
                    .uri(igdbServiceUrl + "/igdb/games/" + igdbId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<IgdbGameDTO>>() {
                    }).block();

            if (igdbGameJson == null || igdbGameJson.isEmpty()) {
                throw new GameNotFoundInIgdbException("Game with id " + igdbId + " not found in IGDB.");
            }

            IgdbGameDTO igdbGameDto = igdbGameJson.get(0);

            Game game = new Game();
            game.setIgdbId(igdbGameDto.id());
            game.setTitle(igdbGameDto.name());
            game.setDescription(igdbGameDto.summary());
            game.setCoverUrl(igdbGameDto.cover().url());

            return gameRepository.save(game);

        } catch (Exception e) {
            throw new IgdbFetchException("Failed to fetch IGDB game", e);
        }
    }

    @Transactional
    public void delete(Long id) {
        gameRepository.deleteById(id);
    }
}
