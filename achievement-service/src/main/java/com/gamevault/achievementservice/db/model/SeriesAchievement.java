package com.gamevault.achievementservice.db.model;

import com.gamevault.achievementservice.enums.AchievementCategory;
import lombok.Getter;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("SERIES")
@Getter
@Setter
@NoArgsConstructor
public class SeriesAchievement extends Achievement {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "series_id")
    private List<SeriesPart> requiredGameIds = new ArrayList<>();

    public SeriesAchievement(AchievementCategory category,
                             List<SeriesPart> requiredGameIds, String iconUrl, int experiencePoints) {
        setCategory(category);
        this.requiredGameIds = requiredGameIds;
        setIconUrl(iconUrl);
        setExperiencePoints(experiencePoints);
    }
}
