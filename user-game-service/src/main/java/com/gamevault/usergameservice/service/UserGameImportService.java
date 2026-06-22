package com.gamevault.usergameservice.service;

import com.gamevault.dto.ImportGameItemResult;
import com.gamevault.dto.ImportGamesResponse;
import com.gamevault.enums.ImportItemStatus;
import com.gamevault.usergameservice.exception.GameNotFoundInIgdbException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserGameImportService {
    private final UserGameImportItemService userGameImportItemService;

    public UserGameImportService(UserGameImportItemService userGameImportItemService) {
        this.userGameImportItemService = userGameImportItemService;
    }

    public ImportGamesResponse importGames(UUID userId, List<Long> igdbIds) {
        List<ImportGameItemResult> items = igdbIds.stream()
                .distinct()
                .map(igdbId -> {
                    try {
                        return userGameImportItemService.addOne(userId, igdbId);
                    } catch (EntityNotFoundException e) {
                        return failed(igdbId, "DLC_PARENT_MISSING", e.getMessage());
                    } catch (GameNotFoundInIgdbException e) {
                        return failed(igdbId, "IGDB_NOT_FOUND", e.getMessage());
                    } catch (Exception e) {
                        return failed(igdbId, "UNKNOWN_ERROR", "Unexpected import error");
                    }
                })
                .toList();

        int added = count(items, ImportItemStatus.ADDED);
        int exists = count(items, ImportItemStatus.ALREADY_EXISTS);
        int failed = count(items, ImportItemStatus.FAILED);

        return new ImportGamesResponse(igdbIds.size(), added, exists, failed, items);
    }

    private static ImportGameItemResult failed(Long igdbId, String code, String message) {
        return new ImportGameItemResult(igdbId, ImportItemStatus.FAILED, code, message);
    }

    private static int count(List<ImportGameItemResult> items, ImportItemStatus status) {
        return (int) items.stream().filter(i -> i.status() == status).count();
    }
}
