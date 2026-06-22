package com.gamevault.usergameservice.service.enriched;

import com.gamevault.dto.igdb.IgdbGameDto;
import com.gamevault.usergameservice.db.model.UserGameList;
import com.gamevault.usergameservice.db.repository.UserGameListRepository;
import com.gamevault.dto.db.UserGameBatchData;
import com.gamevault.usergameservice.dto.output.enriched.EnrichedGameDto;
import com.gamevault.usergameservice.dto.output.enriched.EnrichedGameList;
import com.gamevault.usergameservice.dto.output.enriched.UserGameData;
import com.gamevault.usergameservice.service.IgdbClient;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EnrichedGameService {
    private final IgdbClient igdbClient;
    private final UserGameListRepository userGameListRepository;
    private final UserGameEnrichmentLoader userGameEnrichmentLoader;

    public EnrichedGameService(IgdbClient igdbClient, UserGameListRepository userGameListRepository, UserGameEnrichmentLoader userGameEnrichmentLoader) {
        this.igdbClient = igdbClient;
        this.userGameListRepository = userGameListRepository;
        this.userGameEnrichmentLoader = userGameEnrichmentLoader;
    }

    @Transactional(readOnly = true)
    public EnrichedGameDto getGameWithUserData(Long igdbId, UUID userId) {
        IgdbGameDto igdbGame = igdbClient.getGame(igdbId);

        if (igdbGame == null) {
            return null;
        }

        if (userId == null) {
            return EnrichedGameDto.fromIgdb(igdbGame);
        }

        UserGameData userData = userGameEnrichmentLoader.loadUserGameData(userId, igdbId);
        return EnrichedGameDto.fromIgdb(igdbGame, userData);
    }

    public EnrichedGameList getGameListWithUserData(UUID listId, UUID user) {
        UserGameList list =  userGameListRepository.findByIdWithItems(listId)
                .orElseThrow(() -> new EntityNotFoundException("Game list not found"));

        if (!list.isPublic() && (user == null || !list.isOwnedBy(user))) {
            throw new AccessDeniedException("This list is private");
        }

        if (user == null) {
            return EnrichedGameList.fromUserGameList(list, null);
        }

        Set<Long> igdbGameIds = list.getItems().stream().map(item -> item.getGame().getIgdbId()).collect(Collectors.toSet());
        Map<Long, UserGameBatchData> games = userGameEnrichmentLoader.loadUserGameDataBatch(user, igdbGameIds);

        return EnrichedGameList.fromUserGameList(list, games, user);
    }
}
