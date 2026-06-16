package com.gamevault.usergameservice.dto.input;

import com.gamevault.enums.GameStatus;
import com.gamevault.enums.IgdbGameType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class UserGamesFilterParams {
    private GameStatus status;
    private Integer minRating;
    private Integer maxRating;
    private Boolean hasReview;
    private Boolean dlcOnly;
    private Boolean isFullyCompleted;
    private IgdbGameType gameType;
    private String title;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdAfter;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate  createdBefore;
}
