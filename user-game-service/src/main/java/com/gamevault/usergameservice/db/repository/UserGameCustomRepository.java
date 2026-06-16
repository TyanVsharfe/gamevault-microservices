package com.gamevault.usergameservice.db.repository;

import com.gamevault.usergameservice.dto.input.UserGamesFilterParams;
import com.gamevault.usergameservice.dto.output.GameListReference;
import com.gamevault.usergameservice.db.model.UserGame;
import com.gamevault.usergameservice.dto.output.UserModeDto;
import com.gamevault.usergameservice.dto.output.db.UserGameBaseData;
import com.gamevault.usergameservice.dto.output.db.UserGameBatchData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface UserGameCustomRepository {
    Optional<UserGameBaseData> getUserGameBaseData(Long igdbId, UUID user);
    List<UserGameBatchData> getUserGamesBaseDataBatch(UUID user, Set<Long> igdbIds);
    Page<UserGame> findGamesWithFilters(UserGamesFilterParams params, UUID user, Pageable pageable);
    Map<Long, List<GameListReference>> getGameListsMap(UUID user, Set<Long> igdbIds);
    List<UserModeDto> findUserModes(Long userGameId);
    Double calculateAverageRating(UUID user);
}
