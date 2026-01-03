package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.nasa.Utils;
import org.example.question.AbstractQuestion;
import org.example.question.GitQuestion;
import org.example.question.JavaQuestion;
import org.example.question.SQLQuestion;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Bot extends TelegramLongPollingBot {
	
    private final Dotenv dotenv;
    private final String botToken;
    private final String botUsername;
    private final String nasaApiKey;
    private final AbstractQuestion[] questions;
    private ConcurrentHashMap<Long, UserData> users;

    public Bot() {
        this.dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.botToken = getEnv("BOT_TOKEN", dotenv);
        this.botUsername = getEnv("BOT_USERNAME", dotenv);
        this.nasaApiKey = getEnv("NASA_API_KEY", dotenv);
        questions = new AbstractQuestion[3];
        questions[0] = new JavaQuestion();
        questions[1] = new SQLQuestion();
        questions[2] = new GitQuestion();
        users = new ConcurrentHashMap<>();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void sendText(Long who, String what){
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        Message message = update.getMessage();
        String text = message.getText();
        Long userId = message.getFrom().getId();
        users.putIfAbsent(userId, new UserData());
        UserData data = users.get(userId);

        switch (text) {
            case "/start" :
                data.reset();
                sendText(userId, "👋 Hi! I'm JavvyBot. Want to test your Java knowledge? Type /quiz to begin!");
                break;
            case "/quiz" :
                data.reset();
                sendText(userId, "🧠 Quiz begins!");
                sendText(userId, questions[0].getQuestion());
                break;
            case "🔄 Restart Quiz":
                data.reset();
                sendText(userId, "🧠 Quiz begins!");
                sendText(userId, questions[0].getQuestion());
                break;
            case "🌌 NASA Image of the Day":
                try {
                    sendText(userId, Utils.getUrl("https://api.nasa.gov/planetary/apod?api_key=" + nasaApiKey));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            default :
                int current = data.getCurrent();
                if (current >= questions.length) {
                    sendText(userId, "🎉 Quiz finished! Your score: " + data.getScore() + "/" + questions.length);
                    return;
                }
                boolean result = questions[current].checkAnswer(text);
                if (result) {
                    data.incrementScore();
                    sendText(userId, "✅ Correct!");
                } else {
                    sendText(userId, "❌ Incorrect. Correct answer: " + questions[current].getCorrectAnswer());
                }
                data.incrementCurrent();
                if (data.getCurrent() < questions.length) {
                    sendText(userId, questions[data.getCurrent()].getQuestion());
                } else {
                    sendText(userId, "🎉 Quiz finished! Your score: " + data.getScore() + "/" + questions.length);
                    showCompletionMenu(userId, data);
                }
        }

        System.out.println("[" + userId + "] " + text);
    }

    private void showCompletionMenu(Long userId, UserData data) {
        String message = "🎉 Quiz finished! Your score: " + data.getScore() + "/" + questions.length;
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("🔄 Restart Quiz"));
        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🌌 NASA Image of the Day"));
        keyboard.add(row1);
        keyboard.add(row2);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        SendMessage sm = SendMessage.builder()
                .chatId(userId.toString())
                .text(message)
                .replyMarkup(keyboardMarkup)
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static String getEnv(String key, Dotenv dotenv) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(key + " is not set");
        }
        return value;
    }
    
}
