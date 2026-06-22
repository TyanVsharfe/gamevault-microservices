package com.gamevault.usergameservice.service.enriched;

import com.gamevault.usergameservice.db.repository.UserGameCustomRepository;
import com.gamevault.usergameservice.db.repository.UserGameListRepository;
import com.gamevault.dto.GameListReference;
import com.gamevault.usergameservice.dto.output.UserModeDto;
import com.gamevault.dto.db.UserGameBaseData;
import com.gamevault.dto.db.UserGameBatchData;
import com.gamevault.usergameservice.dto.output.enriched.UserGameData;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserGameEnrichmentLoader {
    private final UserGameCustomRepository userGameCustomRepository;
    private final UserGameListRepository userGameListRepository;

    public UserGameEnrichmentLoader(UserGameCustomRepository userGameCustomRepository, UserGameListRepository userGameListRepository) {
        this.userGameCustomRepository = userGameCustomRepository;
        this.userGameListRepository = userGameListRepository;
    }

    public UserGameData loadUserGameData(UUID userId, Long igdbGameId) {
        if (userId == null) {
            return null;
        }

        UserGameBaseData base = userGameCustomRepository
                .getUserGameBaseData(igdbGameId, userId)
                .orElse(null);

        if (base == null) {
            return null;
        }

        List<UserModeDto> modes = userGameCustomRepository.findUserModes(base.userGameId());

        List<GameListReference> lists = loadGameLists(igdbGameId, userId);
        return UserGameData.fromUserGameBase(base, modes, lists);
    }

    public Map<Long, UserGameBatchData> loadUserGameDataBatch(UUID user, Set<Long> igdbIds) {
        if (igdbIds == null || igdbIds.isEmpty()) {
            return Map.of();
        }

        List<UserGameBatchData> baseData = userGameCustomRepository.getUserGamesBaseDataBatch(user, igdbIds);

        if (baseData.isEmpty()) {
            return Map.of();
        }

        Map<Long, UserGameBatchData> gamesMap = baseData.stream()
                .collect(Collectors.toMap(
                        UserGameBatchData::getIgdbId,
                        Function.identity(),
                        (existing, replacement) -> existing
                ));

        Map<Long, List<GameListReference>> listsMap = userGameCustomRepository.getGameListsMap(user, igdbIds);

        listsMap.forEach((igdbId, references) -> {
            UserGameBatchData data = gamesMap.get(igdbId);
            if (data != null) {
                data.setInLists(references);
            }
        });

        return gamesMap;
    }

    public List<GameListReference> loadGameLists(Long userGameId, UUID user) {
        return userGameListRepository.findListsByUserGameIdAndAuthorId(userGameId, user);
    }
}
