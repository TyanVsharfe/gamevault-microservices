package com.gamevault.achievementservice.controller;

import com.gamevault.achievementservice.db.model.Achievement;
import com.gamevault.achievementservice.dto.input.AchievementForm;
import com.gamevault.achievementservice.dto.output.UserAchievementDTO;
import com.gamevault.achievementservice.enums.AchievementCategory;
import com.gamevault.achievementservice.service.AchievementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/users/achievements")
public class UserAchievementController {
    private final AchievementService achievementService;

    public UserAchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Iterable<Achievement>> getAchievementsByCategory(@PathVariable AchievementCategory category) {
        return ResponseEntity.ok(achievementService.getAchievementsByCategory(category));
    }

    @GetMapping
    public ResponseEntity<Iterable<UserAchievementDTO>> getUserAchievements(HttpServletRequest request,
                                                                            Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        UUID userId = UUID.fromString(jwt.getClaim("userId").toString());
        Locale locale = request.getLocale();
        String lang = locale.getLanguage();
        return ResponseEntity.ok(achievementService.getUserAchievements(userId, lang));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<Achievement> createAchievement(@RequestBody AchievementForm achievement) {
        return ResponseEntity.ok(achievementService.createAchievement(achievement));
    }
}
