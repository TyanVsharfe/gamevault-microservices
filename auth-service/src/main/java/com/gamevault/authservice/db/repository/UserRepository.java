package com.gamevault.authservice.db.repository;

import com.gamevault.authservice.db.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @Query("SELECT u.id FROM User u")
    Page<UUID> findAllUserUUIDs(Pageable pageable);
    Optional<User> findByUsername(String username);
}
