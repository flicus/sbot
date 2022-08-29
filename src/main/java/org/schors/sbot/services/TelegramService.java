package org.schors.sbot.services;

import org.schors.telegram.sm.TelegramLongPollingSMBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TelegramService extends TelegramLongPollingSMBot {

    @Value("${sbot.name}")
    private String name;

    @Value("${sbot.token}")
    private String token;


    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

}
