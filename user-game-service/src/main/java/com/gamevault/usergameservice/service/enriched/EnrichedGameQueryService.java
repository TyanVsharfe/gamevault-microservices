package com.gamevault.usergameservice.service.enriched;

import com.gamevault.dto.igdb.IgdbGameDto;
import com.gamevault.usergameservice.db.repository.UserGameCustomRepository;
import com.gamevault.dto.GameListReference;
import com.gamevault.dto.db.UserGameBatchData;
import com.gamevault.usergameservice.dto.output.enriched.EnrichedGameSearchDto;
import com.gamevault.usergameservice.service.IgdbClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EnrichedGameQueryService {
    private final IgdbClient igdbClient;
    private final UserGameCustomRepository userGameCustomRepository;

    public EnrichedGameQueryService(IgdbClient igdbClient, UserGameCustomRepository userGameCustomRepository) {
        this.igdbClient = igdbClient;
        this.userGameCustomRepository = userGameCustomRepository;
    }

    @Transactional(readOnly = true)
    public List<EnrichedGameSearchDto> searchGamesWithUserData(String query, UUID userId) {
        List<IgdbGameDto> searchResults = igdbClient.searchGames(query);

        if (searchResults.isEmpty()) {
            return List.of();
        }

        Set<Long> igdbIds = searchResults.stream()
                .map(IgdbGameDto::id)
                .collect(Collectors.toSet());

        if (userId == null) {
            return searchResults.stream()
                    .map(EnrichedGameSearchDto::fromIgdb)
                    .toList();
        }

        Map<Long, UserGameBatchData> userDataByIgdbId =
                userGameCustomRepository.getUserGamesBaseDataBatch(userId, igdbIds)
                        .stream()
                        .collect(Collectors.toMap(
                                UserGameBatchData::getIgdbId,
                                Function.identity()
                        ));

        Map<Long, List<GameListReference>> listsByIgdbId =
                userGameCustomRepository.getGameListsMap(userId, igdbIds);

        listsByIgdbId.forEach((igdbId, lists) -> {
            UserGameBatchData data = userDataByIgdbId.get(igdbId);
            if (data != null) {
                data.setInLists(lists);
            }
        });

        return searchResults.stream()
                .map(game -> EnrichedGameSearchDto.fromIgdb(
                        game,
                        userDataByIgdbId.get(game.id())
                ))
                .toList();
    }
}
