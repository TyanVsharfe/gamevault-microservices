package com.gamevault.usergameservice.db.repository;

import com.gamevault.usergameservice.db.model.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends CrudRepository<Game, Long> {
    boolean existsByIgdbId(Long IgdbId);
    void deleteByIgdbId(Long id);
}
