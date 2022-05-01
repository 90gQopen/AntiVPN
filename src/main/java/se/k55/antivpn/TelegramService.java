package se.k55.antivpn;

import com.moandjiezana.toml.Toml;

import java.io.IOException;
import java.net.URL;

public final class TelegramService {
    
    private static final String UNFORMATTED_URL = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
    
    private final String token;
    private final String chatId;
    
    public TelegramService(Toml config){
        this.token = config.getString("telegram.token");
        this.chatId = config.getString("telegram.chat-id");
    }
    
    public void send(String message) {
        try {
            new URL(String.format(UNFORMATTED_URL, token, chatId, message)).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
