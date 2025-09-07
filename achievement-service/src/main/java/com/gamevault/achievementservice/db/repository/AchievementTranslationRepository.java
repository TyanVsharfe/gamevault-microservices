package com.gamevault.achievementservice.db.repository;

import com.gamevault.achievementservice.db.model.AchievementTranslation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchievementTranslationRepository extends CrudRepository<AchievementTranslation, Long> {
}
