package com.gamevault.authservice.controller;

import com.gamevault.authservice.db.model.User;
import com.gamevault.authservice.dto.*;
import com.gamevault.authservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userDetailsService;

    public UserController(UserService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/registration")
    public User register(@RequestBody UserForm user) {
        return userDetailsService.add(user);
    }

    @GetMapping("/uuids")
    public ResponseEntity<List<UUID>> getAllUserUUIDs(@RequestParam(name = "page", defaultValue = "0") int page,
                                                      @RequestParam(name = "size", defaultValue = "1000") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UUID> uuidPage = userDetailsService.getAllUserUUIDs(pageable);
        return ResponseEntity.ok(uuidPage.getContent());
    }
}
