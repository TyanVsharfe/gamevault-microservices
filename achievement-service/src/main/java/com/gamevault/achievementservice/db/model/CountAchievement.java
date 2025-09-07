package com.gamevault.achievementservice.db.model;

import com.gamevault.achievementservice.enums.AchievementCategory;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("COUNT")
@Getter
@Setter
@NoArgsConstructor
public class CountAchievement extends Achievement {
    
    private int requiredCount;

    public CountAchievement(AchievementCategory category,
                            int requiredCount, String iconUrl, int experiencePoints) {
        setCategory(category);
        this.requiredCount = requiredCount;
        setIconUrl(iconUrl);
        setExperiencePoints(experiencePoints);
    }
}
