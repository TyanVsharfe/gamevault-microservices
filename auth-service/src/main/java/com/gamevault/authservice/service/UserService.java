package com.gamevault.authservice.service;

import com.gamevault.authservice.db.repository.UserRepository;
import com.gamevault.authservice.dto.UserForm;
import com.gamevault.authservice.db.model.User;
import com.gamevault.authservice.dto.UserInfo;
import com.gamevault.events.user.UserEvent;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserEventProducer userEventProducer, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userEventProducer = userEventProducer;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Optional<User> getUser(UUID userId) {
        return userRepository.findById(userId);
    }

    public Page<UUID> getAllUserUUIDs(Pageable pageable) {
        return userRepository.findAllUserUUIDs(pageable);
    }

    public UserInfo add(UserForm user) {
        log.info("Attempting to register user: {}", user.username());

        if (userRepository.findByUsername(user.username()).isPresent()) {
            log.warn("Username '{}' already exists.", user.username());
            throw new EntityExistsException("Username already exists");
        }

        String bcryptPass = passwordEncoder.encode(user.password());
        User newUser = new User(user.username(), bcryptPass, List.of("USER"));
        User saved = userRepository.save(newUser);
        log.info("User '{}' registered successfully.", user.username());

        userEventProducer.InitializeUserAchievements(new UserEvent(
                saved.getId(),
                saved.getUsername(),
                saved.getAvatarUrl(),
                UserEvent.EventType.USER_CREATED
        ));
        return new UserInfo(saved);
    }

    @PostConstruct
    public void initTestUser() {
        if (userRepository.findByUsername("test").isEmpty()) {
            User testUser = new User("test", passwordEncoder.encode("test"), List.of("ROLE_USER"));
            userRepository.save(testUser);
        }
    }
}
