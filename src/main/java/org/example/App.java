package org.example;

import org.example.config.BotConfig;
import org.example.scheduler.NasaDailyScheduler;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

	public static void main(String[] args) {
		try {
			startHealthServer();
			startTelegramBot();
			Thread.currentThread().join();
		} catch (Exception e) {
			log.error("Failed to start bot", e);
		}
	}

	private static void startTelegramBot() throws TelegramApiException {
		TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
		Bot bot = new Bot();
		botsApi.registerBot(bot);
		log.info("🤖 Telegram bot registered");
		NasaDailyScheduler.start(bot, bot.getUsers(), BotConfig.nasaApiKey());
	}

	private static void startHealthServer() {
		int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
		HealthServer.start(port);
	}

}
