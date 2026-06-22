package com.gamevault.dto.igdb;

public record Company(
        int id,
        String name,
        String slug,
        Cover cover
) {}
