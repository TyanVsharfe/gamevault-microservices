package com.gamevault.achievementservice.db.repository;

import com.gamevault.achievementservice.db.model.UserAchievement;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends CrudRepository<UserAchievement, Long> {
    long countByUserId(UUID uuid);
    List<UserAchievement> findByUserId(UUID userId);
    List<UserAchievement> findUserAchievementsByUserId(UUID userId);

    List<UserAchievement> findByUserIdAndIsCompleted(UUID userId, Boolean isCompleted);

    Optional<UserAchievement> findByUserIdAndAchievementId(UUID userId, Long achievementId);

    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.id = :userId AND ua.isCompleted = true")
    Integer countCompletedAchievements(UUID userId);
}
