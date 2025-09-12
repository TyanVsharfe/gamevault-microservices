package com.gamevault.usergameservice.dto.igdb;

public record Company(
        int id,
        String name
) {
    public record Cover(
            int id,
            String url
    ) {}
}
