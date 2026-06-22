package com.gamevault.steamimportservice.component;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SteamTitleNormalizer {
    private static final Pattern TRADEMARKS = Pattern.compile("[™®©]");
    private static final Pattern BRACKET_REGION = Pattern.compile(
            "(?i)\\s*[\\[(](ru|cis|row|eu|na|jp|kr|cn|global|region locked|retail|steam|pc|ww)[\\])]\\s*"
    );
    private static final Pattern DASH_SUFFIX = Pattern.compile(
            "(?i)\\s+[-–—]\\s+(deluxe|ultimate|definitive|complete|standard|collector'?s|goty|game of the year|soundtrack|ost|demo|beta|playtest|test server|public test).*?$"
    );
    private static final Pattern EDITION_WORDS = Pattern.compile(
            "(?i)\\b(deluxe|ultimate|definitive|complete|standard|collector'?s|game of the year|goty) edition\\b"
    );
    private static final Pattern NON_TEXT = Pattern.compile("[^\\p{L}\\p{N}]+");

    public String canonical(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }

        String value = Normalizer.normalize(raw, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);

        value = TRADEMARKS.matcher(value).replaceAll(" ");
        value = BRACKET_REGION.matcher(value).replaceAll(" ");
        value = DASH_SUFFIX.matcher(value).replaceAll(" ");
        value = EDITION_WORDS.matcher(value).replaceAll(" ");
        value = NON_TEXT.matcher(value).replaceAll(" ");

        return value.trim().replaceAll("\\s+", " ");
    }

    public Set<String> variants(String raw) {
        Set<String> variants = new LinkedHashSet<>();

        variants.add(canonical(raw));

        if (raw != null) {
            int colon = raw.indexOf(':');
            if (colon > 0) {
                variants.add(canonical(raw.substring(0, colon)));
            }

            int dash = raw.indexOf(" - ");
            if (dash > 0) {
                variants.add(canonical(raw.substring(0, dash)));
            }
        }

        return variants.stream()
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
