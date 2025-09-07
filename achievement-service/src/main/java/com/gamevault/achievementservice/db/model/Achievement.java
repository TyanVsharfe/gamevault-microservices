package com.gamevault.achievementservice.db.model;

import com.gamevault.achievementservice.enums.AchievementCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "achievements")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "achievement_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AchievementCategory category;

    private String iconUrl;

    private int experiencePoints;

    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AchievementTranslation> translations = new ArrayList<>();
}
