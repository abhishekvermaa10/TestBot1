package org.example;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.example.config.BotConfig;
import org.example.question.AbstractQuestion;
import org.example.question.GitQuestion;
import org.example.question.JavaQuestion;
import org.example.question.SQLQuestion;
import org.example.question.UserData;
import org.example.util.NasaUtility;
import org.example.util.PicsumUtility;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Bot extends TelegramLongPollingBot {

	private final String botUsername = BotConfig.botUsername();
	private final String nasaApiKey = BotConfig.nasaApiKey();
	private final AbstractQuestion[] questions = { new JavaQuestion(), new SQLQuestion(), new GitQuestion() };
	private final ConcurrentHashMap<Long, UserData> users = new ConcurrentHashMap<>();

	private static final String CMD_START = "/start";
	private static final String CMD_QUIZ = "/quiz";
	private static final String CMD_RESTART = "🔄 Restart Quiz";
	private static final String CMD_NASA = "🌌 NASA Image of the Day";
	private static final String CMD_PICSUM = "🖼️ Picsum Image of the Day";

	public Bot() {
		super(BotConfig.botToken());
	}

	@Override
	public String getBotUsername() {
		return botUsername;
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (!update.hasMessage() || !update.getMessage().hasText()) {
			return;
		}
		Message message = update.getMessage();
		String text = message.getText();
		Long userId = message.getFrom().getId();
		String displayName = resolveDisplayName(message.getFrom());
		UserData data = users.computeIfAbsent(userId, id -> new UserData());
		try {
			handleMessage(userId, displayName, text, data);
		} catch (Exception e) {
			log.error("Error while processing message", e);
			sendText(userId, "⚠️ Something went wrong. Please try again.");
		}
		log.info("[{}] {}", displayName, text);
	}
	
	public ConcurrentHashMap<Long, UserData> getUsers() {
	    return users;
	}

	private void handleMessage(Long userId, String displayName, String text, UserData data) {
		switch (text) {
		case CMD_START -> handleStart(userId, displayName, data);
		case CMD_QUIZ, CMD_RESTART -> startQuiz(userId, data);
		case CMD_NASA -> sendNasaImage(userId);
		case CMD_PICSUM -> sendPicsumImage(userId);
		default -> handleQuizAnswer(userId, data, text);
		}
	}

	private void handleStart(Long userId, String displayName, UserData data) {
		data.reset();
		sendText(userId,
				"👋 Hi " + displayName + "! I'm JavvyBot. Want to test your Java knowledge? Type /quiz to begin!");
	}

	private void startQuiz(Long userId, UserData data) {
		data.reset();
		sendText(userId, "🧠 Quiz begins!");
		sendText(userId, questions[0].getQuestion());
	}

	private void sendNasaImage(Long userId) {
		try {
			sendText(userId, NasaUtility.getDailyImageUrl("https://api.nasa.gov/planetary/apod?api_key=" + nasaApiKey));
		} catch (IOException e) {
			log.error("Failed to fetch NASA image {}", e.getMessage());
			sendText(userId, "⚠️ Failed to fetch NASA Image of the Day. Please try again later.");
		}
	}
	
	private void sendPicsumImage(Long userId) {
		try {
			sendText(userId, PicsumUtility.getDailyImageUrl("https://picsum.photos/1080/1080"));
		} catch (IOException e) {
			log.error("Failed to fetch Picsum image {}", e.getMessage());
			sendText(userId, "⚠️ Failed to fetch Picsum Image of the Day. Please try again later.");
		}
	}

	private void handleQuizAnswer(Long userId, UserData data, String answer) {
		int current = data.getCurrent();
		if (current >= questions.length) {
			showCompletionMenu(userId, data);
			return;
		}
		AbstractQuestion question = questions[current];
		if (question.checkAnswer(answer)) {
			data.incrementScore();
			sendText(userId, "✅ Correct!");
		} else {
			sendText(userId, "❌ Incorrect. Correct answer: " + question.getCorrectAnswer());
		}
		data.incrementCurrent();
		if (data.getCurrent() < questions.length) {
			sendText(userId, questions[data.getCurrent()].getQuestion());
		} else {
			showCompletionMenu(userId, data);
		}
	}

	private void showCompletionMenu(Long userId, UserData data) {
		ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(
				List.of(new KeyboardRow(List.of(new KeyboardButton(CMD_RESTART))),
						new KeyboardRow(List.of(new KeyboardButton(CMD_NASA))),
						new KeyboardRow(List.of(new KeyboardButton(CMD_PICSUM)))
						));
		keyboard.setResizeKeyboard(true);
		keyboard.setOneTimeKeyboard(false);
		sendMessage(userId, "🎉 Quiz finished! Your score: " + data.getScore() + "/" + questions.length, keyboard);
	}

	public void sendText(Long userId, String text) {
		sendMessage(userId, text, null);
	}

	private void sendMessage(Long userId, String text, ReplyKeyboardMarkup keyboard) {
		SendMessage sm = SendMessage.builder().chatId(userId.toString()).text(text).replyMarkup(keyboard).build();
		try {
			execute(sm);
		} catch (TelegramApiException e) {
			log.error("Failed to send message", e);
		}
	}

	private static String resolveDisplayName(User user) {
		String firstName = user.getFirstName();
		String lastName = user.getLastName();
		String userName = user.getUserName();
		String displayName;
		if (isNotBlank(firstName) && isNotBlank(lastName)) {
			displayName = firstName + " " + lastName;
		} else if (isNotBlank(firstName)) {
			displayName = firstName;
		} else if (isNotBlank(lastName)) {
			displayName = lastName;
		} else if (isNotBlank(userName)) {
			displayName = userName;
		} else {
			displayName = String.valueOf(user.getId());
		}
		return displayName;
	}

	private static boolean isNotBlank(String value) {
		return value != null && !value.trim().isEmpty();
	}

}
