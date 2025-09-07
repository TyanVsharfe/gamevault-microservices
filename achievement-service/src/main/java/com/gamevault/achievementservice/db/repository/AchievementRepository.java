package com.gamevault.achievementservice.db.repository;

import com.gamevault.achievementservice.db.model.Achievement;
import com.gamevault.achievementservice.enums.AchievementCategory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends CrudRepository<Achievement, Long> {
    List<Achievement> findByCategory(AchievementCategory category);
}
