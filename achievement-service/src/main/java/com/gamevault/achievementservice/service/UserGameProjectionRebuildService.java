package com.gamevault.achievementservice.service;

import com.gamevault.achievementservice.db.model.UserGameCache;
import com.gamevault.achievementservice.db.repository.UserGameCacheRepository;
import com.gamevault.dto.usergame.UserGameSnapshotItem;
import com.gamevault.enums.GameStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserGameProjectionRebuildService {
    private final UserGameSnapshotClient snapshotClient;
    private final AchievementProcessorService achievementProcessorService;
    private final TransactionOperations transactionOperations;
    private final UserGameCacheRepository userGameCacheRepository;

    public UserGameProjectionRebuildService(UserGameSnapshotClient snapshotClient,
                                            AchievementProcessorService achievementProcessorService,
                                            TransactionOperations transactionOperations,
                                            UserGameCacheRepository userGameCacheRepository) {
        this.snapshotClient = snapshotClient;
        this.achievementProcessorService = achievementProcessorService;
        this.transactionOperations = transactionOperations;
        this.userGameCacheRepository = userGameCacheRepository;
    }

    public void rebuildAndRecalculate(UUID userId, int batchSize) {
        List<UserGameCache> snapshot = fetchFullSnapshots(userId, batchSize);

        transactionOperations.executeWithoutResult(status -> {
            userGameCacheRepository.deleteByUser(userId);
            userGameCacheRepository.saveAll(snapshot);
        });

        achievementProcessorService.processAchievementCompletion(userId);
        log.info("Rebuilt user-game projection for user {}. Items: {}", userId, snapshot.size());
    }

    private List<UserGameCache> fetchFullSnapshots(UUID userId, int batchSize) {
        List<UserGameCache> result = new ArrayList<>();
        int page = 0;

        while(true) {
            List<UserGameSnapshotItem> items = snapshotClient.fetchUserGames(userId, batchSize, page);

            items.stream()
                    .map(item -> new UserGameCache(
                            item.userId(),
                            item.gameId(),
                            item.status() == null ? GameStatus.NONE : item.status())
                    ).forEach(result::add);

            if (items.size() < batchSize) {
                return result;
            }

            page++;
        }
    }
}
