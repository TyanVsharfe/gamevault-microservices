package com.gamevault.usergameservice.db.repository;

import com.gamevault.usergameservice.db.model.UserCache;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserCacheRepository extends CrudRepository<UserCache, UUID> {
}
