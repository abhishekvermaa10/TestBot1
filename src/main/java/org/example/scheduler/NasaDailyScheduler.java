package org.example.scheduler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.example.Bot;
import org.example.question.UserData;
import org.example.util.NasaUtility;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class NasaDailyScheduler {

	private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread t = new Thread(r);
		t.setName("nasa-daily-scheduler");
		t.setDaemon(true);
		return t;
	});

	public static void start(Bot bot, ConcurrentHashMap<Long, UserData> users, String nasaApiKey) {
		long initialDelay = secondsUntilNextFetch();
		long period = TimeUnit.DAYS.toSeconds(1);
		SCHEDULER.scheduleAtFixedRate(() -> pushDailyImage(bot, users, nasaApiKey), initialDelay, period,
				TimeUnit.SECONDS);
		log.info("🌌 Daily NASA scheduler started");
	}

	private static void pushDailyImage(Bot bot, ConcurrentHashMap<Long, UserData> users, String nasaApiKey) {
		try {
			String imageUrl = NasaUtility.getDailyImageUrl("https://api.nasa.gov/planetary/apod?api_key=" + nasaApiKey);
			users.forEach((userId, data) -> {
				bot.sendText(userId, "🌌 NASA Image of the Day\n" + imageUrl);
			});
		} catch (Exception e) {
			users.forEach((userId, data) -> {
				bot.sendText(userId, "Failed to fetch today's image automatically.");
			});
			log.error("Failed to push daily NASA image {}", e.getMessage());
		}
	}

	private static long secondsUntilNextFetch() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime nextFetchTime = now.withHour(22).withMinute(22).withSecond(22);
		if (now.isAfter(nextFetchTime)) {
			nextFetchTime = nextFetchTime.plusDays(1);
		}
		return Duration.between(now, nextFetchTime).getSeconds();
	}

}
