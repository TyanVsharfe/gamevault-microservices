package com.gamevault.usergameservice.dto.output.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamevault.enums.GameStatus;
import com.gamevault.usergameservice.dto.output.GameListReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGameBatchData {
        private Long igdbId;
        private Long userGameId;
        private GameStatus status;
        private Double userRating;
        private String review;
        private boolean isFullyCompleted;
        private boolean isOverallRating;
        private boolean isOverallStatus;
        private String userCoverUrl;
        private Instant createdAt;
        private Instant updatedAt;

        @Setter
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private List<GameListReference> inLists;
}
