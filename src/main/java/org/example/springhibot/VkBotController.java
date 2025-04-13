package org.example.springhibot;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.stream.Collectors;
import java.util.logging.*;

@RestController
@RequestMapping("/api/vk")
public class VkBotController {

    private static final String ACCESS_TOKEN = "vk1.a.bravU4lLWz6jMbcCVToegeX8XgUEFYFYBoaa2-nAtGqLOGz6i3L5cmEnupBSmr9zhvYs6fweJPJuGxqJDfZrOqc3y9_tHfe3TUp2ULc_TVKohoRFlmlyoIdRu3G6uotgGr1nO4hoKPlxVqkDIQLpE2JDv09-1KcqUi8xox4WWe9gc8YRR-OgTd32oS3-AW9FCdVyvW_aAcTaLWEv3DudaA";
    private static final String CONFIRMATION_CODE = "7daa38f0";
    private static final Logger logger = Logger.getLogger(VkBotController.class.getName());

    @PostMapping("/callback")
    public String handleCallback(@RequestBody String body) {
        logger.info("Получен запрос от VK: " + body);

        try {
            JSONObject json = new JSONObject(body);
            String type = json.getString("type");

            if ("confirmation".equals(type)) {
                logger.info("Запрос подтверждения сервера");
                return CONFIRMATION_CODE;
            }

            if ("message_new".equals(type)) {
                JSONObject message = json.getJSONObject("object").getJSONObject("message");
                String text = message.getString("text");
                int userId = message.getInt("from_id");
                logger.info(String.format("Новое сообщение от %d: %s", userId, text));

                sendMessage(userId, "Вы написали: " + text);
            }

        } catch (JSONException e) {
            logger.severe("Ошибка парсинга JSON: " + e.getMessage());
        } catch (Exception e) {
            logger.severe("Неизвестная ошибка: " + e.getMessage());
        }

        return "ok";
    }

    private void sendMessage(int userId, String text) {
        try {
            String url = "https://api.vk.com/method/messages.send";
            String params = "user_id=" + userId +
                    "&message=" + URLEncoder.encode(text, "UTF-8") +
                    "&access_token=" + ACCESS_TOKEN +
                    "&v=5.199";

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(params.getBytes());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String response = br.lines().collect(Collectors.joining());
                    logger.info("Успешная отправка сообщения. Ответ VK: " + response);
                }
            } else {
                logger.warning("Ошибка HTTP при отправке: " + responseCode);
            }

        } catch (IOException e) {
            logger.severe("Ошибка отправки сообщения: " + e.getMessage());
        }
    }
}