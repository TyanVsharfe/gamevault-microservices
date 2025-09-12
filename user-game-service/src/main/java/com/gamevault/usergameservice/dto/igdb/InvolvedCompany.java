package com.gamevault.usergameservice.dto.igdb;

public record InvolvedCompany(
        int id,
        boolean developer,
        boolean publisher,
        Company company
) {}
