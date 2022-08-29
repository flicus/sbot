package org.schors.sbot.sm.actions;

import lombok.SneakyThrows;
import org.schors.telegram.sm.SMAction;
import org.schors.telegram.sm.SMActionBase;
import org.springframework.statemachine.StateContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@SMAction(source = "initial", target = "textEntered", event = "text")
@SMAction(source = "search", target = "textEntered", event = "text")
@SMAction(source = "page", target = "textEntered", event = "text")
public class AskAboutText extends SMActionBase {

    @SneakyThrows
    @Override
    public void execute(StateContext<String, String> context) {
        context(context)
                .storeMessageText()
                .sendMessageAndStoreId(SendMessage.builder()
                        .text("Это автор или книга?")
                        .replyMarkup(InlineKeyboardMarkup.builder()
                                .keyboardRow(List.of(
                                        InlineKeyboardButton.builder()
                                                .text("Автор")
                                                .callbackData("authors")
                                                .build(),
                                        InlineKeyboardButton.builder()
                                                .text("Книга")
                                                .callbackData("books")
                                                .build()))
                                .build())
                        .chatId(chatId(context))
                        .build());
    }
}
