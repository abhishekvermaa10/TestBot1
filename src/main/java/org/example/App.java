package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
	
	public static void main(String[] args) {
		try {
			int port = Integer.parseInt(
                    System.getenv().getOrDefault("PORT", "8080")
            );
            HealthServer.start(port);
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(new Bot());
			log.info("Bot started successfully!");
			Thread.currentThread().join();
		} catch (Exception e) {
			log.error("Failed to start bot", e);
		}
	}
	
}
