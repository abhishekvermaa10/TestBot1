package org.example.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class BotConfig {

    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();
    private static final String BOT_TOKEN = read("BOT_TOKEN");
    private static final String BOT_USERNAME = read("BOT_USERNAME");
    private static final String NASA_API_KEY = read("NASA_API_KEY");

    public static String botToken() {
        return BOT_TOKEN;
    }

    public static String botUsername() {
        return BOT_USERNAME;
    }

    public static String nasaApiKey() {
        return NASA_API_KEY;
    }

    private static String read(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = DOTENV.get(key);
        }
        if (value == null || value.isBlank()) {
        	log.error("Environment variable {} is not set", key);
            throw new IllegalStateException(
                    "Environment variable " + key + " is not set");
        }
        return value;
    }
    
}
