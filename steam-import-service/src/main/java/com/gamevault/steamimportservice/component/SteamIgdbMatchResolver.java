package com.gamevault.steamimportservice.component;

import com.gamevault.dto.igdb.Cover;
import com.gamevault.dto.igdb.IgdbGameDto;
import com.gamevault.steamimportservice.dto.IgdbMatchedGame;
import com.gamevault.steamimportservice.dto.SteamImportPreviewItem;
import com.gamevault.steamimportservice.dto.SteamOwnedGameDto;
import com.gamevault.steamimportservice.enums.SteamMatchStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class SteamIgdbMatchResolver {
    private static final Pattern IGNORED_STEAM_TITLE = Pattern.compile(
            "(?i).*\\b(soundtrack|ost|dedicated server|server|sdk|editor|benchmark|playtest|beta|demo)\\b.*"
    );

    public SteamImportPreviewItem resolve(
            SteamOwnedGameDto steamGame,
            Optional<IgdbGameDto> appIdMatch) {

        if (shouldIgnore(steamGame)) {
            return ignored(steamGame);
        }

        if (appIdMatch.isEmpty()) {
            return notFound(steamGame);
        }

        return matched(steamGame, appIdMatch.get());
    }

    private SteamImportPreviewItem matched(SteamOwnedGameDto steamGame, IgdbGameDto igdbGame) {
        return new SteamImportPreviewItem(
                steamGame.steamAppId(),
                steamGame.title(),
                steamGame.playtimeForever(),
                steamGame.iconUrl(),
                SteamMatchStatus.MATCHED,
                toMatchedGame(igdbGame)
        );
    }

    private SteamImportPreviewItem notFound(SteamOwnedGameDto steamGame) {
        return new SteamImportPreviewItem(
                steamGame.steamAppId(),
                steamGame.title(),
                steamGame.playtimeForever(),
                steamGame.iconUrl(),
                SteamMatchStatus.NOT_FOUND,
                null
        );
    }

    private SteamImportPreviewItem ignored(SteamOwnedGameDto steamGame) {
        return new SteamImportPreviewItem(
                steamGame.steamAppId(),
                steamGame.title(),
                steamGame.playtimeForever(),
                steamGame.iconUrl(),
                SteamMatchStatus.IGNORED,
                null
        );
    }

    private boolean shouldIgnore(SteamOwnedGameDto steamGame) {
        String title = steamGame.title();
        return title != null && IGNORED_STEAM_TITLE.matcher(title).matches();
    }

    private IgdbMatchedGame toMatchedGame(IgdbGameDto game) {
        return new IgdbMatchedGame(
                game.id(),
                game.name(),
                coverUrl(game.cover()),
                releaseYear(game.first_release_date()),
                null
        );
    }

    private String coverUrl(Cover cover) {
        return cover == null ? null : cover.url();
    }

    private Integer releaseYear(long firstReleaseDate) {
        if (firstReleaseDate <= 0) {
            return null;
        }

        return Instant.ofEpochSecond(firstReleaseDate)
                .atZone(ZoneOffset.UTC)
                .getYear();
    }
}