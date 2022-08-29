package org.schors.sbot.services;


import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SendMessageList {
    private List<StringBuilder> list = new ArrayList<>();
    private int max = 2048;
    private StringBuilder current = new StringBuilder();

    public SendMessageList() {
    }

    public SendMessageList(int max) {
        this.max = max;
    }

    public SendMessageList append(String msg) {
        if ((current.length() + msg.length()) > max) {
            updateList();
        }
        current.append(msg);
        return this;
    }

    public List<String> getParts() {
        updateList();
        return list.stream()
                .map(stringBuilder -> stringBuilder.toString())
                .collect(Collectors.toList());
    }

    public List<SendMessage> getMessages() {
        updateList();
        return list.stream()
                .map(stringBuilder -> {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText(stringBuilder.toString());
                    sendMessage.enableHtml(true);
                    return sendMessage;
                })
                .collect(Collectors.toList());
    }

    private void updateList() {
        list.add(current);
        current = new StringBuilder();
    }

}
